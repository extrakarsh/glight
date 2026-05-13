package com.example.glight.data.remote

import com.example.glight.domain.model.Complaint
import com.example.glight.domain.model.Pole

interface RemoteDataSource {
    suspend fun getPoles(): Result<List<Pole>>
    suspend fun getComplaints(): Result<List<Complaint>>
    suspend fun submitComplaint(complaint: Complaint): Result<Unit>
    suspend fun updateComplaintStatus(complaintId: String, status: String): Result<Unit>
    suspend fun updatePoleStatus(poleId: String, status: String): Result<Unit>
    suspend fun uploadPoles(poles: List<Pole>): Result<Unit>
    fun isAvailable(): Boolean
}
