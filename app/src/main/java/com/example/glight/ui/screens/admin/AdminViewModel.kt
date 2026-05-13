package com.example.glight.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glight.domain.model.Complaint
import com.example.glight.domain.model.ComplaintStatus
import com.example.glight.domain.model.PoleStatus
import com.example.glight.domain.repository.ComplaintRepository
import com.example.glight.domain.repository.PoleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val complaints: List<Complaint> = emptyList(),
    val totalComplaints: Int = 0,
    val resolvedCount: Int = 0,
    val resolutionRate: Float = 0f
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository,
    private val poleRepository: PoleRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow<ComplaintStatus?>(null)
    val selectedFilter: StateFlow<ComplaintStatus?> = _selectedFilter

    val uiState: StateFlow<AdminUiState> = combine(
        complaintRepository.getComplaints(),
        _searchQuery,
        _selectedFilter
    ) { complaints, query, filter ->
        val filtered = complaints
            .filter { complaint ->
                if (query.isBlank()) true
                else complaint.id.contains(query, ignoreCase = true) ||
                     complaint.poleId.contains(query, ignoreCase = true)
            }
            .filter { complaint ->
                filter == null || complaint.status == filter
            }

        val resolved = complaints.count { it.status == ComplaintStatus.FIXED }
        AdminUiState(
            complaints = filtered,
            totalComplaints = complaints.size,
            resolvedCount = resolved,
            resolutionRate = if (complaints.isNotEmpty()) resolved.toFloat() / complaints.size else 0f
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdminUiState()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            complaintRepository.refreshComplaints()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: ComplaintStatus?) {
        _selectedFilter.value = filter
    }

    fun updateStatus(complaint: Complaint, status: ComplaintStatus) {
        viewModelScope.launch {
            complaintRepository.updateComplaintStatus(complaint.id, status.name)
            if (status == ComplaintStatus.FIXED) {
                poleRepository.updatePoleStatus(complaint.poleId, PoleStatus.WORKING.name)
            }
        }
    }
}
