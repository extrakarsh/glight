package com.example.glight.data.mapper

import com.example.glight.data.local.entity.ComplaintEntity
import com.example.glight.data.local.entity.PoleEntity
import com.example.glight.domain.model.Complaint
import com.example.glight.domain.model.ComplaintStatus
import com.example.glight.domain.model.Pole
import com.example.glight.domain.model.PoleStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MappersTest {

    @Test
    fun poleEntityMapsInvalidStatusToWorking() {
        val entity = PoleEntity(
            id = "GL-001",
            lat = 12.9716,
            lng = 77.5946,
            status = "BROKEN_VALUE",
            lastMaintenanceDate = null,
            bulbType = "LED 24W"
        )

        val pole = entity.toDomainModel()

        assertEquals("GL-001", pole.id)
        assertEquals(PoleStatus.WORKING, pole.status)
    }

    @Test
    fun complaintEntityParsesTagsAndStatus() {
        val entity = ComplaintEntity(
            id = "GL-123",
            poleId = "GL-002",
            reportedStatus = "BURNING_DAYTIME",
            status = "ASSIGNED",
            timestamp = 1000L,
            reporterId = null,
            aiTags = "Daytime Waste, Switching Error",
            aiSummary = "Check timer control."
        )

        val complaint = entity.toDomainModel()

        assertEquals(PoleStatus.BURNING_DAYTIME, complaint.reportedStatus)
        assertEquals(ComplaintStatus.ASSIGNED, complaint.status)
        assertEquals(listOf("Daytime Waste", "Switching Error"), complaint.aiTags)
        assertNull(complaint.reporterId)
    }

    @Test
    fun complaintRoundTripPreservesAiFields() {
        val complaint = Complaint(
            id = "GL-456",
            poleId = "GL-006",
            reportedStatus = PoleStatus.FUSED,
            status = ComplaintStatus.SUBMITTED,
            timestamp = 2000L,
            reporterId = "anonymous",
            aiTags = listOf("Bulb Fused", "Night Safety"),
            aiSummary = "Replace bulb and test holder."
        )

        val entity = complaint.toEntity()
        val mapped = entity.toDomainModel()

        assertEquals(complaint, mapped)
    }

    @Test
    fun poleRoundTripPreservesCoordinates() {
        val pole = Pole(
            id = "GL-009",
            lat = 12.9,
            lng = 77.5,
            status = PoleStatus.WORKING,
            lastMaintenanceDate = 3000L,
            bulbType = "LED 18W"
        )

        val mapped = pole.toEntity().toDomainModel()

        assertEquals(pole, mapped)
    }
}
