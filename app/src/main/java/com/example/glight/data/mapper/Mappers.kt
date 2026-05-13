package com.example.glight.data.mapper

import com.example.glight.data.local.entity.ComplaintEntity
import com.example.glight.data.local.entity.PoleEntity
import com.example.glight.domain.model.Complaint
import com.example.glight.domain.model.ComplaintStatus
import com.example.glight.domain.model.Pole
import com.example.glight.domain.model.PoleStatus

fun PoleEntity.toDomainModel(): Pole {
    val poleStatus = try {
        PoleStatus.valueOf(this.status)
    } catch (_: Exception) {
        PoleStatus.WORKING
    }
    return Pole(
        id = this.id,
        lat = this.lat,
        lng = this.lng,
        status = poleStatus,
        lastMaintenanceDate = this.lastMaintenanceDate,
        bulbType = this.bulbType
    )
}

fun Pole.toEntity(): PoleEntity {
    return PoleEntity(
        id = this.id,
        lat = this.lat,
        lng = this.lng,
        status = this.status.name,
        lastMaintenanceDate = this.lastMaintenanceDate,
        bulbType = this.bulbType
    )
}

fun ComplaintEntity.toDomainModel(): Complaint {
    val complaintStatus = try {
        ComplaintStatus.valueOf(this.status)
    } catch (_: Exception) {
        ComplaintStatus.SUBMITTED
    }
    val reportedPoleStatus = try {
        PoleStatus.valueOf(this.reportedStatus)
    } catch (_: Exception) {
        PoleStatus.FUSED
    }
    val tagsList = if (this.aiTags.isNotBlank()) {
        this.aiTags.split(",").map { it.trim() }
    } else emptyList()

    return Complaint(
        id = this.id,
        poleId = this.poleId,
        reportedStatus = reportedPoleStatus,
        status = complaintStatus,
        timestamp = this.timestamp,
        reporterId = this.reporterId,
        aiTags = tagsList,
        aiSummary = this.aiSummary,
        lastUpdated = this.lastUpdated
    )
}

fun Complaint.toEntity(): ComplaintEntity {
    return ComplaintEntity(
        id = this.id,
        poleId = this.poleId,
        reportedStatus = this.reportedStatus.name,
        status = this.status.name,
        timestamp = this.timestamp,
        reporterId = this.reporterId,
        aiTags = this.aiTags.joinToString(","),
        aiSummary = this.aiSummary,
        lastUpdated = this.lastUpdated
    )
}
