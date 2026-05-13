package com.example.glight.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glight.domain.ai.AiProvider
import com.example.glight.domain.model.Pole
import com.example.glight.domain.repository.ComplaintRepository
import com.example.glight.domain.repository.PoleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportResult(
    val complaintId: String,
    val tags: List<String>,
    val suggestion: String?
)

sealed interface ReportState {
    data object Idle : ReportState
    data object Loading : ReportState
    data class Success(val report: ReportResult) : ReportState
    data class Error(val message: String) : ReportState
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val poleRepository: PoleRepository,
    private val complaintRepository: ComplaintRepository,
    private val aiProvider: AiProvider
) : ViewModel() {

    val poles: StateFlow<List<Pole>> = poleRepository.getPoles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _reportState = MutableStateFlow<ReportState>(ReportState.Idle)
    val reportState: StateFlow<ReportState> = _reportState

    private val _lastReport = MutableStateFlow<ReportResult?>(null)
    val lastReport: StateFlow<ReportResult?> = _lastReport

    init {
        viewModelScope.launch(Dispatchers.IO) {
            poleRepository.refreshPoles()
        }
    }

    fun reportPoleIssue(pole: Pole, reportedStatus: String, notes: String, reporterId: String? = null) {
        viewModelScope.launch {
            _reportState.value = ReportState.Loading
            try {
                val tags = aiProvider.generateTags(notes, reportedStatus)
                val suggestion = aiProvider.suggestFix(notes, reportedStatus)

                poleRepository.updatePoleStatus(pole.id, reportedStatus)
                val result = complaintRepository.submitComplaint(
                    poleId = pole.id,
                    reportedStatus = reportedStatus,
                    reporterId = reporterId,
                    aiTags = tags,
                    aiSummary = suggestion
                )
                result.onSuccess { complaint ->
                    val report = ReportResult(complaint.id, tags, suggestion)
                    _lastReport.value = report
                    _reportState.value = ReportState.Success(report)
                }.onFailure { e ->
                    _reportState.value = ReportState.Error(e.localizedMessage ?: "Report submission failed")
                }
            } catch (e: Exception) {
                _reportState.value = ReportState.Error(e.localizedMessage ?: "Unexpected error")
            }
        }
    }

    fun clearLastReport() {
        _lastReport.value = null
        _reportState.value = ReportState.Idle
    }
}
