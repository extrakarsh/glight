package com.example.glight.data.remote

import com.example.glight.domain.model.Complaint
import com.example.glight.domain.model.ComplaintStatus
import com.example.glight.domain.model.Pole
import com.example.glight.domain.model.PoleStatus
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class FirebaseRemoteDataSource @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase?
) : RemoteDataSource {

    private val polesRef get() = firebaseDatabase?.getReference("poles")
    private val complaintsRef get() = firebaseDatabase?.getReference("complaints")

    override fun isAvailable(): Boolean = firebaseDatabase != null

    override suspend fun getPoles(): Result<List<Pole>> = runCatching {
        val ref = polesRef ?: return Result.success(emptyList())
        val snapshot = withTimeoutOrNull(5000) { ref.get().await() }
            ?: return Result.success(emptyList())

        snapshot.children.mapNotNull { child ->
            val id = child.key ?: return@mapNotNull null
            val lat = child.child("lat").getValue(Double::class.java) ?: return@mapNotNull null
            val lng = child.child("lng").getValue(Double::class.java) ?: return@mapNotNull null
            val statusStr = child.child("status").getValue(String::class.java) ?: "WORKING"
            val lastMaintenance = child.child("lastMaintenanceDate").getValue(Long::class.java)
            val bulbType = child.child("bulbType").getValue(String::class.java) ?: "LED"
            val status = try { PoleStatus.valueOf(statusStr) } catch (_: Exception) { PoleStatus.WORKING }
            Pole(id, lat, lng, status, lastMaintenance, bulbType)
        }
    }

    override suspend fun getComplaints(): Result<List<Complaint>> = runCatching {
        val ref = complaintsRef ?: return Result.success(emptyList())
        val snapshot = withTimeoutOrNull(5000) { ref.get().await() }
            ?: return Result.success(emptyList())

        snapshot.children.mapNotNull { child ->
            val id = child.key ?: return@mapNotNull null
            val poleId = child.child("poleId").getValue(String::class.java) ?: return@mapNotNull null
            val statusStr = child.child("status").getValue(String::class.java) ?: "SUBMITTED"
            val reportedStatusStr = child.child("reportedStatus").getValue(String::class.java) ?: "FUSED"
            val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
            val reporterId = child.child("reporterId").getValue(String::class.java)
            val aiTags = child.child("aiTags").getValue(String::class.java).orEmpty()
                .split(",").map { it.trim() }.filter { it.isNotBlank() }
            val aiSummary = child.child("aiSummary").getValue(String::class.java)
            val lastUpdated = child.child("lastUpdated").getValue(Long::class.java) ?: timestamp

            Complaint(
                id = id,
                poleId = poleId,
                reportedStatus = try { PoleStatus.valueOf(reportedStatusStr) } catch (_: Exception) { PoleStatus.FUSED },
                status = try { ComplaintStatus.valueOf(statusStr) } catch (_: Exception) { ComplaintStatus.SUBMITTED },
                timestamp = timestamp,
                reporterId = reporterId,
                aiTags = aiTags,
                aiSummary = aiSummary,
                lastUpdated = lastUpdated
            )
        }
    }

    override suspend fun submitComplaint(complaint: Complaint): Result<Unit> = runCatching {
        val ref = complaintsRef ?: return Result.success(Unit)
        val remoteMap = mapOf(
            "id" to complaint.id,
            "poleId" to complaint.poleId,
            "status" to complaint.status.name,
            "timestamp" to complaint.timestamp,
            "reporterId" to complaint.reporterId,
            "reportedStatus" to complaint.reportedStatus.name,
            "aiTags" to complaint.aiTags.joinToString(","),
            "aiSummary" to complaint.aiSummary,
            "lastUpdated" to complaint.lastUpdated
        )
        withTimeoutOrNull(3000) { ref.child(complaint.id).setValue(remoteMap).await() }
            ?: error("Timed out while submitting complaint")
    }

    override suspend fun updateComplaintStatus(complaintId: String, status: String): Result<Unit> = runCatching {
        val ref = complaintsRef ?: return Result.success(Unit)
        val updates = mapOf(
            "status" to status,
            "lastUpdated" to System.currentTimeMillis()
        )
        withTimeoutOrNull(3000) { ref.child(complaintId).updateChildren(updates).await() }
            ?: error("Timed out while updating complaint")
    }

    override suspend fun updatePoleStatus(poleId: String, status: String): Result<Unit> = runCatching {
        val ref = polesRef ?: return Result.success(Unit)
        withTimeoutOrNull(3000) { ref.child(poleId).child("status").setValue(status).await() }
            ?: error("Timed out while updating pole")
    }

    override suspend fun uploadPoles(poles: List<Pole>): Result<Unit> = runCatching {
        val ref = polesRef ?: return Result.success(Unit)
        val data = poles.associateBy({ it.id }) { pole ->
            mapOf(
                "lat" to pole.lat,
                "lng" to pole.lng,
                "status" to pole.status.name,
                "lastMaintenanceDate" to pole.lastMaintenanceDate,
                "bulbType" to pole.bulbType
            )
        }
        withTimeoutOrNull(5000) { ref.setValue(data).await() }
            ?: error("Timed out while uploading poles")
    }
}
