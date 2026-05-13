package com.example.glight.data.repository

import com.example.glight.data.local.dao.ComplaintDao
import com.example.glight.data.mapper.toDomainModel
import com.example.glight.data.mapper.toEntity
import com.example.glight.data.remote.RemoteDataSource
import com.example.glight.data.sync.SyncManager
import com.example.glight.domain.model.Complaint
import com.example.glight.domain.model.ComplaintStatus
import com.example.glight.domain.model.PoleStatus
import com.example.glight.domain.repository.ComplaintRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComplaintRepositoryImpl @Inject constructor(
    private val complaintDao: ComplaintDao,
    private val remoteDataSource: RemoteDataSource,
    private val syncManager: SyncManager
) : ComplaintRepository {

    override fun getComplaints(): Flow<List<Complaint>> {
        return complaintDao.getAllComplaints().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun syncComplaints(): Flow<Unit> = flow {
        refreshComplaints()
        emit(Unit)
    }

    override fun getComplaintsByStatus(status: String): Flow<List<Complaint>> {
        return complaintDao.getComplaintsByStatus(status).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun submitComplaint(
        poleId: String,
        reportedStatus: String,
        reporterId: String?,
        aiTags: List<String>,
        aiSummary: String?
    ): Result<Complaint> {
        return try {
            val now = System.currentTimeMillis()
            val complaintId = generateComplaintId(now)
            val poleStatus = try { PoleStatus.valueOf(reportedStatus) } catch (_: Exception) { PoleStatus.FUSED }

            val newComplaint = Complaint(
                id = complaintId,
                poleId = poleId,
                reportedStatus = poleStatus,
                status = ComplaintStatus.SUBMITTED,
                timestamp = now,
                reporterId = reporterId,
                aiTags = aiTags,
                aiSummary = aiSummary,
                lastUpdated = now
            )

            complaintDao.insertComplaint(newComplaint.toEntity())

            remoteDataSource.submitComplaint(newComplaint).onFailure {
                syncManager.enqueue(
                    entityType = "COMPLAINT",
                    entityId = complaintId,
                    operation = "SUBMIT_COMPLAINT",
                    payload = newComplaint.toSyncPayload()
                )
            }

            Result.success(newComplaint)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateComplaintStatus(complaintId: String, status: String): Result<Unit> {
        return try {
            val current = complaintDao.getComplaintById(complaintId)
                ?: return Result.failure(IllegalArgumentException("Complaint not found"))
            val updatedEntity = current.copy(
                status = status,
                lastUpdated = System.currentTimeMillis()
            )
            complaintDao.insertComplaint(updatedEntity)

            syncManager.enqueue(
                entityType = "COMPLAINT",
                entityId = complaintId,
                operation = "UPDATE_COMPLAINT_STATUS",
                payload = status
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshComplaints() {
        try {
            val result = remoteDataSource.getComplaints()
            result.onSuccess { remoteComplaints ->
                if (remoteComplaints.isNotEmpty()) {
                    complaintDao.insertComplaints(remoteComplaints.map { it.toEntity() })
                }
            }
        } catch (_: Exception) { }
    }

    private fun generateComplaintId(timestamp: Long): String {
        val year = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
        }.get(java.util.Calendar.YEAR)
        val hash = UUID.randomUUID().toString().take(6).uppercase()
        return "GL-$year-$hash"
    }

    private fun Complaint.toSyncPayload(): String {
        return JSONObject()
            .put("id", id)
            .put("poleId", poleId)
            .put("reportedStatus", reportedStatus.name)
            .put("status", status.name)
            .put("timestamp", timestamp)
            .put("reporterId", reporterId.orEmpty())
            .put("aiTags", aiTags.joinToString(","))
            .put("aiSummary", aiSummary.orEmpty())
            .put("lastUpdated", lastUpdated)
            .toString()
    }
}
