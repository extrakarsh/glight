package com.example.glight.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poles")
data class PoleEntity(
    @PrimaryKey val id: String,
    val lat: Double,
    val lng: Double,
    val status: String,
    val lastMaintenanceDate: Long?,
    val bulbType: String
)
