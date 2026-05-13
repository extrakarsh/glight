package com.example.glight.ui.screens.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glight.domain.ai.AiProvider
import com.example.glight.domain.model.ComplaintStatus
import com.example.glight.domain.model.PoleStatus
import com.example.glight.domain.repository.ComplaintRepository
import com.example.glight.domain.repository.PoleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class SavingsUiState(
    val daytimeReports: Int = 0,
    val fixedComplaints: Int = 0,
    val estimatedKwhSaved: Double = 0.0,
    val activeComplaints: Int = 0,
    val totalPoles: Int = 0,
    val weeklyTrend: List<Int> = emptyList(),
    val aiSummary: String = "",
    val anomalies: List<String> = emptyList()
)

@HiltViewModel
class SavingsViewModel @Inject constructor(
    complaintRepository: ComplaintRepository,
    poleRepository: PoleRepository,
    private val aiProvider: AiProvider
) : ViewModel() {

    val uiState = combine(
        complaintRepository.getComplaints(),
        poleRepository.getPoles()
    ) { complaints, poles ->
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val monthComplaints = complaints.filter { it.timestamp >= monthStart }
        val daytimeReports = monthComplaints.count { it.reportedStatus == PoleStatus.BURNING_DAYTIME }
        val fixed = monthComplaints.count { it.status == ComplaintStatus.FIXED }
        val estimatedKwh = daytimeReports * DAILY_DAYTIME_WASTE_KWH

        val weeklyTrend = computeWeeklyTrend(complaints)
        val anomalies = aiProvider.detectAnomalies(
            complaints.map { "${it.reportedStatus.name} ${it.poleId}" }
        )

        SavingsUiState(
            daytimeReports = daytimeReports,
            fixedComplaints = fixed,
            estimatedKwhSaved = estimatedKwh,
            activeComplaints = complaints.count { it.status != ComplaintStatus.FIXED },
            totalPoles = poles.size,
            weeklyTrend = weeklyTrend,
            aiSummary = aiProvider.monthlyEnergySummary(daytimeReports, estimatedKwh, fixed),
            anomalies = anomalies
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SavingsUiState(
            aiSummary = aiProvider.monthlyEnergySummary(0, 0.0, 0)
        )
    )

    private fun computeWeeklyTrend(complaints: List<com.example.glight.domain.model.Complaint>): List<Int> {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L
        return (6 downTo 0).map { daysAgo ->
            val dayStart = now - (daysAgo + 1) * dayMs
            val dayEnd = now - daysAgo * dayMs
            complaints.count { it.timestamp in dayStart until dayEnd }
        }
    }

    private companion object {
        const val DAILY_DAYTIME_WASTE_KWH = 1.2
    }
}
