package com.example.glight.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey val id: String,
    val poleId: String,
    val reportedStatus: String,
    val status: String,
    val timestamp: Long,
    val reporterId: String?,
    val aiTags: String,
    val aiSummary: String?,
    val lastUpdated: Long = timestamp
)
