package com.example.glight.domain.repository

import com.example.glight.domain.model.Complaint
import kotlinx.coroutines.flow.Flow

interface ComplaintRepository {
    fun getComplaints(): Flow<List<Complaint>>
    fun syncComplaints(): Flow<Unit>
    fun getComplaintsByStatus(status: String): Flow<List<Complaint>>
    suspend fun submitComplaint(
        poleId: String,
        reportedStatus: String,
        reporterId: String?,
        aiTags: List<String> = emptyList(),
        aiSummary: String? = null
    ): Result<Complaint>
    suspend fun updateComplaintStatus(complaintId: String, status: String): Result<Unit>
    suspend fun refreshComplaints()
}
