package com.example.glight.ui.screens.repairs

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.glight.domain.model.ComplaintStatus
import com.example.glight.ui.components.GlassCard
import com.example.glight.ui.components.TimelineStepper
import com.example.glight.ui.theme.PrimaryBlue
import com.example.glight.ui.theme.StatusGreen
import com.example.glight.ui.theme.StatusRed
import com.example.glight.ui.theme.StatusYellow
import com.example.glight.ui.theme.TextPrimary
import com.example.glight.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ComplaintDetailScreen(
    complaintId: String,
    darkAuditMode: Boolean,
    onBack: () -> Unit,
    viewModel: RepairsViewModel = hiltViewModel()
) {
    val complaints by viewModel.complaints.collectAsStateWithLifecycle()
    val complaint = remember(complaints, complaintId) {
        complaints.find { it.id == complaintId }
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 84.dp, bottom = 100.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
            }
            Text("Complaint Detail", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (darkAuditMode) Color.White else TextPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (complaint == null) {
            GlassCard(modifier = Modifier.fillMaxWidth(), darkAuditMode = darkAuditMode) {
                Text("Complaint not found.", color = TextSecondary, fontWeight = FontWeight.Bold)
            }
            return
        }

        val (statusColor, statusLabel) = when (complaint.status) {
            ComplaintStatus.SUBMITTED -> StatusRed to "Submitted"
            ComplaintStatus.ASSIGNED -> StatusYellow to "Assigned"
            ComplaintStatus.FIXED -> StatusGreen to "Fixed"
        }

        // Complaint ID Header
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 28.dp, darkAuditMode = darkAuditMode) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Surface(color = PrimaryBlue.copy(alpha = 0.12f), shape = RoundedCornerShape(999.dp)) {
                        Text(
                            complaint.id,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Surface(color = statusColor.copy(alpha = 0.16f), shape = RoundedCornerShape(999.dp)) {
                        Text(
                            statusLabel,
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Text(
                    "Pole ${complaint.poleId}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (darkAuditMode) Color.White else TextPrimary
                )
                Text(
                    "Reported: ${complaint.reportedStatus.name.replace("_", " ")}",
                    fontSize = 15.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timeline
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp, darkAuditMode = darkAuditMode) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Repair Timeline", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (darkAuditMode) Color.White else TextPrimary)
                TimelineStepper(status = complaint.status)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Details
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp, darkAuditMode = darkAuditMode) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (darkAuditMode) Color.White else TextPrimary)
                DetailRow("Submitted", dateFormat.format(Date(complaint.timestamp)), darkAuditMode)
                DetailRow("Last Updated", dateFormat.format(Date(complaint.lastUpdated)), darkAuditMode)
                complaint.reporterId?.let {
                    DetailRow("Reporter", it, darkAuditMode)
                }
            }
        }

        // AI Insights
        if (complaint.aiTags.isNotEmpty() || complaint.aiSummary != null) {
            Spacer(modifier = Modifier.height(16.dp))
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp, darkAuditMode = darkAuditMode) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("AI Insights", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (darkAuditMode) PrimaryBlue else PrimaryBlue)
                    if (complaint.aiTags.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            complaint.aiTags.forEach { tag ->
                                Surface(
                                    color = PrimaryBlue.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(999.dp)
                                ) {
                                    Text(
                                        tag,
                                        color = PrimaryBlue,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                    complaint.aiSummary?.let {
                        Text(it, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, darkAuditMode: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = if (darkAuditMode) Color.White else TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
