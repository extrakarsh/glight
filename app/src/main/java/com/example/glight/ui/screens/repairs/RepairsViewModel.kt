package com.example.glight.ui.screens.repairs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glight.domain.model.Complaint
import com.example.glight.domain.repository.ComplaintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepairsViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository
) : ViewModel() {

    val complaints: StateFlow<List<Complaint>> = complaintRepository.getComplaints()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            complaintRepository.refreshComplaints()
        }
    }
}
