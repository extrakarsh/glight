package com.example.glight.domain.model

import androidx.compose.runtime.Immutable

enum class PoleStatus {
    WORKING,
    FUSED,
    BURNING_DAYTIME
}

@Immutable
data class Pole(
    val id: String,
    val lat: Double,
    val lng: Double,
    val status: PoleStatus,
    val lastMaintenanceDate: Long? = null,
    val bulbType: String = "Unknown"
)
