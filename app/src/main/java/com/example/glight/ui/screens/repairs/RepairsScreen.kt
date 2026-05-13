package com.example.glight.ui.screens.repairs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RepairsScreen(
    darkAuditMode: Boolean,
    onComplaintClick: (String) -> Unit = {},
    viewModel: RepairsViewModel = hiltViewModel()
) {
    val complaints by viewModel.complaints.collectAsStateWithLifecycle()
    val activeCount by remember(complaints) {
        derivedStateOf { complaints.count { it.status != ComplaintStatus.FIXED } }
    }
    val fixedCount by remember(complaints) {
        derivedStateOf { complaints.count { it.status == ComplaintStatus.FIXED } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 128.dp, bottom = 100.dp)
    ) {
        Text("Repair Tracker", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = if (darkAuditMode) Color.White else TextPrimary)
        Text(
            "Timeline view of every reported streetlight repair.",
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCard("Active", activeCount, PrimaryBlue, darkAuditMode, Modifier.weight(1f))
            SummaryCard("Fixed", fixedCount, StatusGreen, darkAuditMode, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (complaints.isEmpty()) {
            EmptyState(darkAuditMode)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(complaints, key = { it.id }) { complaint ->
                    ComplaintItem(
                        complaint = complaint,
                        darkAuditMode = darkAuditMode,
                        onClick = { onComplaintClick(complaint.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: Int, valueColor: Color, darkAuditMode: Boolean, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, cornerRadius = 22.dp, darkAuditMode = darkAuditMode) {
        Column {
            Text(label, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Text("$value", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun EmptyState(darkAuditMode: Boolean) {
    GlassCard(modifier = Modifier.fillMaxWidth(), darkAuditMode = darkAuditMode) {
        Column {
            Text("No complaints yet", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (darkAuditMode) Color.White else TextPrimary)
            Text("Submitted reports will appear here with their repair status.", color = TextSecondary, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
fun ComplaintItem(complaint: Complaint, darkAuditMode: Boolean, onClick: () -> Unit = {}) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val lastUpdatedFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val (statusColor, statusText) = when (complaint.status) {
        ComplaintStatus.SUBMITTED -> StatusRed to "Submitted"
        ComplaintStatus.ASSIGNED -> StatusYellow to "Assigned"
        ComplaintStatus.FIXED -> StatusGreen to "Fixed"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        cornerRadius = 24.dp,
        darkAuditMode = darkAuditMode
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(color = PrimaryBlue.copy(alpha = 0.12f), shape = RoundedCornerShape(999.dp)) {
                        Text(
                            complaint.id,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Text(
                        "Pole ${complaint.poleId}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = if (darkAuditMode) Color.White else TextPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        "${complaint.reportedStatus.name.replace("_", " ")} - ${dateFormat.format(Date(complaint.timestamp))}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
                Surface(color = statusColor.copy(alpha = 0.16f), shape = RoundedCornerShape(999.dp)) {
                    Text(
                        statusText,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            TimelineStepper(status = complaint.status)

            if (complaint.aiTags.isNotEmpty()) {
                Text("AI: ${complaint.aiTags.joinToString()}", fontSize = 12.sp, color = TextSecondary)
            }

            Text(
                "Updated ${lastUpdatedFormat.format(Date(complaint.lastUpdated))}",
                fontSize = 11.sp,
                color = TextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}
