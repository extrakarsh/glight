package com.example.glight.domain.model

import androidx.compose.runtime.Immutable

enum class ComplaintStatus {
    SUBMITTED,
    ASSIGNED,
    FIXED
}

@Immutable
data class Complaint(
    val id: String,
    val poleId: String,
    val reportedStatus: PoleStatus,
    val status: ComplaintStatus,
    val timestamp: Long,
    val reporterId: String? = null,
    val aiTags: List<String> = emptyList(),
    val aiSummary: String? = null,
    val lastUpdated: Long = timestamp
)
