package com.example.glight.data.sync

import com.example.glight.data.local.dao.SyncQueueDao
import com.example.glight.data.local.entity.SyncQueueEntity
import com.example.glight.data.remote.RemoteDataSource
import com.example.glight.domain.model.Complaint
import com.example.glight.domain.model.ComplaintStatus
import com.example.glight.domain.model.PoleStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val remoteDataSource: RemoteDataSource
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val maxRetries = 5

    fun startSync() {
        scope.launch {
            processPendingItems()
        }
    }

    suspend fun enqueue(
        entityType: String,
        entityId: String,
        operation: String,
        payload: String
    ) {
        val item = SyncQueueEntity(
            id = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            operation = operation,
            payload = payload,
            timestamp = System.currentTimeMillis()
        )
        syncQueueDao.insert(item)
        startSync()
    }

    private suspend fun processPendingItems() {
        if (!remoteDataSource.isAvailable()) return

        val items = syncQueueDao.getPendingItemsList()
        for (item in items) {
            if (item.retryCount >= maxRetries) {
                syncQueueDao.updateStatus(item.id, "FAILED")
                continue
            }

            val result = processItem(item)
            if (result.isSuccess) {
                syncQueueDao.updateStatus(item.id, "COMPLETED")
            } else {
                syncQueueDao.updateStatus(item.id, "PENDING")
                val backoffMs = (1000L * (1 shl item.retryCount.coerceAtMost(4)))
                delay(backoffMs)
            }
        }
        syncQueueDao.clearCompleted()
    }

    private suspend fun processItem(item: SyncQueueEntity): Result<Unit> {
        return when (item.operation) {
            "SUBMIT_COMPLAINT" -> {
                remoteDataSource.submitComplaint(item.payload.toComplaint())
            }
            "UPDATE_COMPLAINT_STATUS" -> {
                remoteDataSource.updateComplaintStatus(item.entityId, item.payload)
            }
            "UPDATE_POLE_STATUS" -> {
                remoteDataSource.updatePoleStatus(item.entityId, item.payload)
            }
            else -> Result.success(Unit)
        }
    }

    fun getPendingCount(): Flow<Int> = syncQueueDao.getPendingCount()

    private fun String.toComplaint(): Complaint {
        val json = JSONObject(this)
        val timestamp = json.getLong("timestamp")
        return Complaint(
            id = json.getString("id"),
            poleId = json.getString("poleId"),
            reportedStatus = PoleStatus.valueOf(json.getString("reportedStatus")),
            status = ComplaintStatus.valueOf(json.getString("status")),
            timestamp = timestamp,
            reporterId = json.optString("reporterId").takeIf { it.isNotBlank() },
            aiTags = json.optString("aiTags")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() },
            aiSummary = json.optString("aiSummary").takeIf { it.isNotBlank() },
            lastUpdated = json.optLong("lastUpdated", timestamp)
        )
    }
}
