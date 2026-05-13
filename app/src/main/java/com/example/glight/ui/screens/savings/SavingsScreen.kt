package com.example.glight.ui.screens.savings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.glight.ui.components.AnimatedCounter
import com.example.glight.ui.components.CircularGauge
import com.example.glight.ui.components.GlassCard
import com.example.glight.ui.components.WeeklyBarChart
import com.example.glight.ui.theme.PrimaryBlue
import com.example.glight.ui.theme.StatusGreen
import com.example.glight.ui.theme.StatusRed
import com.example.glight.ui.theme.StatusYellow
import com.example.glight.ui.theme.TextPrimary
import com.example.glight.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SavingsScreen(
    darkAuditMode: Boolean,
    viewModel: SavingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80)
        visible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 128.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Energy Dashboard",
            color = if (darkAuditMode) Color.White else TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Live monthly savings from daytime burn reports.",
            color = TextSecondary,
            lineHeight = 22.sp
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { it / 3 }
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth(), darkAuditMode = darkAuditMode, cornerRadius = 30.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularGauge(
                        value = (state.estimatedKwhSaved / 40.0).toFloat(),
                        label = "monthly goal"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        AnimatedCounter(state.estimatedKwhSaved.toInt())
                        Text(" kWh saved", color = TextSecondary, modifier = Modifier.padding(bottom = 7.dp))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Daytime alerts", state.daytimeReports, StatusYellow, darkAuditMode)
            StatCard("Active repairs", state.activeComplaints, PrimaryBlue, darkAuditMode)
            StatCard("Fixed", state.fixedComplaints, StatusGreen, darkAuditMode)
            StatCard("Total poles", state.totalPoles, PrimaryBlue, darkAuditMode)
        }

        GlassCard(modifier = Modifier.fillMaxWidth(), darkAuditMode = darkAuditMode) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Weekly trend", color = if (darkAuditMode) Color.White else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                WeeklyBarChart(values = state.weeklyTrend.ifEmpty { listOf(0, 0, 0, 0, 0, 0, 0) })
            }
        }

        if (state.anomalies.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth(), darkAuditMode = darkAuditMode) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Anomaly Alerts", fontWeight = FontWeight.Bold, color = StatusRed, fontSize = 16.sp)
                    state.anomalies.forEach { anomaly ->
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = StatusYellow, modifier = Modifier.padding(top = 2.dp))
                            Text(anomaly, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth(), darkAuditMode = darkAuditMode) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Monthly AI Report", fontWeight = FontWeight.Bold, color = if (darkAuditMode) Color.White else PrimaryBlue)
                Text(state.aiSummary, color = TextSecondary, lineHeight = 21.sp)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, color: Color, darkAuditMode: Boolean) {
    GlassCard(modifier = Modifier.width(170.dp), cornerRadius = 22.dp, darkAuditMode = darkAuditMode) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("$value", color = color, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
    }
}
