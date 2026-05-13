package com.example.glight.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.glight.domain.model.Complaint
import com.example.glight.domain.model.ComplaintStatus
import com.example.glight.ui.components.GlassCard
import com.example.glight.ui.components.TimelineStepper
import com.example.glight.ui.theme.PrimaryBlue
import com.example.glight.ui.theme.StatusGreen
import com.example.glight.ui.theme.StatusRed
import com.example.glight.ui.theme.StatusYellow
import com.example.glight.ui.theme.TextPrimary
import com.example.glight.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    darkAuditMode: Boolean,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    var pendingUpdate by remember { mutableStateOf<Pair<Complaint, ComplaintStatus>?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 84.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Panchayat Admin",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (darkAuditMode) Color.White else TextPrimary
            )
            Text(
                "Manage complaints with AI repair suggestions.",
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        item {
            // Dashboard Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatCard("Total", uiState.totalComplaints, PrimaryBlue, darkAuditMode, Modifier.weight(1f))
                AdminStatCard("Resolved", uiState.resolvedCount, StatusGreen, darkAuditMode, Modifier.weight(1f))
                AdminStatCard("Rate", "${(uiState.resolutionRate * 100).toInt()}%", StatusYellow, darkAuditMode, Modifier.weight(1f))
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by pole or complaint ID") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }

        item {
            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { viewModel.setFilter(null) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryBlue.copy(alpha = 0.12f))
                )
                ComplaintStatus.entries.forEach { status ->
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = { viewModel.setFilter(if (selectedFilter == status) null else status) },
                        label = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (status) {
                                ComplaintStatus.SUBMITTED -> StatusRed.copy(alpha = 0.12f)
                                ComplaintStatus.ASSIGNED -> StatusYellow.copy(alpha = 0.12f)
                                ComplaintStatus.FIXED -> StatusGreen.copy(alpha = 0.12f)
                            }
                        )
                    )
                }
            }
        }

        if (uiState.complaints.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), darkAuditMode = darkAuditMode) {
                    Column {
                        Text(
                            "No matching complaints",
                            fontWeight = FontWeight.Bold,
                            color = if (darkAuditMode) Color.White else TextPrimary,
                            fontSize = 20.sp
                        )
                        Text(
                            "Try adjusting your search or filter.",
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        } else {
            items(uiState.complaints, key = { it.id }) { complaint ->
                AdminComplaintCard(
                    complaint = complaint,
                    darkAuditMode = darkAuditMode,
                    onStatusSelected = { pendingUpdate = complaint to it }
                )
            }
        }
    }

    pendingUpdate?.let { (complaint, status) ->
        ModalBottomSheet(
            onDismissRequest = { pendingUpdate = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.Transparent,
            dragHandle = null
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                cornerRadius = 28.dp,
                darkAuditMode = darkAuditMode
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Confirm update", color = if (darkAuditMode) Color.White else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text(
                        "Set ${complaint.id} for pole ${complaint.poleId} to ${status.name.lowercase().replaceFirstChar { it.uppercase() }}?",
                        color = TextSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { pendingUpdate = null }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = { viewModel.updateStatus(complaint, status); pendingUpdate = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (status == ComplaintStatus.FIXED) StatusGreen else PrimaryBlue
                            )
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(label: String, value: Any, color: Color, darkAuditMode: Boolean, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, cornerRadius = 18.dp, darkAuditMode = darkAuditMode) {
        Column {
            Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Text("$value", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun AdminComplaintCard(
    complaint: Complaint,
    darkAuditMode: Boolean,
    onStatusSelected: (ComplaintStatus) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), darkAuditMode = darkAuditMode, cornerRadius = 24.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pole ${complaint.poleId}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = if (darkAuditMode) Color.White else TextPrimary)
                    Text(complaint.reportedStatus.name.replace("_", " "), color = TextSecondary, fontSize = 13.sp)
                }
                StatusChip(complaint.status)
            }

            TimelineStepper(status = complaint.status)

            if (complaint.aiTags.isNotEmpty()) {
                Text("AI: ${complaint.aiTags.joinToString()}", color = PrimaryBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            complaint.aiSummary?.let {
                Text(it, color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp)
            }

            Spacer(modifier = Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { onStatusSelected(ComplaintStatus.ASSIGNED) },
                    modifier = Modifier.weight(1f),
                    enabled = complaint.status != ComplaintStatus.ASSIGNED && complaint.status != ComplaintStatus.FIXED
                ) { Text("Assign") }
                Button(
                    onClick = { onStatusSelected(ComplaintStatus.FIXED) },
                    modifier = Modifier.weight(1f),
                    enabled = complaint.status != ComplaintStatus.FIXED,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                ) { Text("Fixed") }
            }
        }
    }
}

@Composable
private fun StatusChip(status: ComplaintStatus) {
    val (color, label) = when (status) {
        ComplaintStatus.SUBMITTED -> StatusRed to "Submitted"
        ComplaintStatus.ASSIGNED -> StatusYellow to "Assigned"
        ComplaintStatus.FIXED -> StatusGreen to "Fixed"
    }
    Surface(color = color.copy(alpha = 0.18f), shape = RoundedCornerShape(999.dp)) {
        Text(label, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
    }
}
