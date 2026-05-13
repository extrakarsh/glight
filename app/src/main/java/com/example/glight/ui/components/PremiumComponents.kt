package com.example.glight.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glight.domain.model.ComplaintStatus
import com.example.glight.domain.model.PoleStatus
import com.example.glight.ui.theme.CardLight
import com.example.glight.ui.theme.NightBackground
import com.example.glight.ui.theme.PrimaryBlue
import com.example.glight.ui.theme.StatusGreen
import com.example.glight.ui.theme.StatusRed
import com.example.glight.ui.theme.StatusYellow
import com.example.glight.ui.theme.SubtleBorder
import com.example.glight.ui.theme.TextPrimary
import com.example.glight.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.max

@Stable
fun PoleStatus.statusColor(): Color = when (this) {
    PoleStatus.WORKING -> StatusGreen
    PoleStatus.FUSED -> StatusRed
    PoleStatus.BURNING_DAYTIME -> StatusYellow
}

@Stable
fun PoleStatus.label(): String = when (this) {
    PoleStatus.WORKING -> "Working"
    PoleStatus.FUSED -> "Fused"
    PoleStatus.BURNING_DAYTIME -> "Burning Daytime"
}

@Stable
fun ComplaintStatus.stepIndex(): Int = when (this) {
    ComplaintStatus.SUBMITTED -> 0
    ComplaintStatus.ASSIGNED -> 1
    ComplaintStatus.FIXED -> 2
}

@Composable
fun PremiumScaffoldBackground(
    darkAuditMode: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Crossfade(targetState = darkAuditMode, animationSpec = tween(450), label = "auditThemeCrossfade") { isNight ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(if (isNight) NightBackground else Color.White)
        ) {
            content()
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    contentPadding: Dp = 18.dp,
    darkAuditMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cardColor = if (darkAuditMode) Color(0xFF161B22) else CardLight
    val borderColor = if (darkAuditMode) Color(0xFF30363D) else SubtleBorder
    Surface(
        modifier = modifier
            .graphicsLayer {
                shadowElevation = 8.dp.toPx()
                ambientShadowColor = Color.Black.copy(alpha = 0.15f)
                spotShadowColor = Color.Black.copy(alpha = 0.15f)
                this.shape = shape
                clip = false
            }
            .clip(shape)
            .background(cardColor)
            .border(1.dp, borderColor, shape),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = shape
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
private fun DayGlow() {
    Canvas(modifier = Modifier.fillMaxSize().blur(18.dp)) {
        drawCircle(
            color = PrimaryBlue.copy(alpha = 0.05f),
            radius = size.minDimension * 0.42f,
            center = Offset(size.width * 0.82f, size.height * 0.08f)
        )
        drawCircle(
            color = StatusYellow.copy(alpha = 0.07f),
            radius = size.minDimension * 0.3f,
            center = Offset(size.width * 0.08f, size.height * 0.88f)
        )
    }
}

@Composable
private fun StarField() {
    val transition = rememberInfiniteTransition(label = "stars")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "starDrift"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        repeat(38) { index ->
            val x = ((index * 73) % max(size.width.toInt(), 1)).toFloat()
            val y = ((index * 137) % max(size.height.toInt(), 1)).toFloat()
            drawCircle(
                color = Color.White.copy(alpha = 0.12f + (index % 4) * 0.04f),
                radius = 1.2f + (index % 3),
                center = Offset(x, (y + drift * 24f) % size.height)
            )
        }
    }
}

@Composable
fun StatusVisual(
    status: PoleStatus,
    modifier: Modifier = Modifier,
    dotSize: Dp = 18.dp
) {
    val color = status.statusColor()
    val infinite = rememberInfiniteTransition(label = "statusVisual")
    val pulse by infinite.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val ripple by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300), RepeatMode.Restart),
        label = "ripple"
    )
    val shimmer by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Restart),
        label = "shimmer"
    )

    Canvas(modifier = modifier.size(dotSize * 2)) {
        when (status) {
            PoleStatus.WORKING -> {
                drawCircle(color.copy(alpha = 0.18f), radius = this.size.minDimension * 0.42f * pulse)
                drawCircle(color, radius = this.size.minDimension * 0.22f * pulse)
            }
            PoleStatus.FUSED -> {
                drawCircle(color.copy(alpha = 0.22f * (1f - ripple)), radius = this.size.minDimension * (0.2f + ripple * 0.42f))
                drawCircle(color, radius = this.size.minDimension * 0.22f)
            }
            PoleStatus.BURNING_DAYTIME -> {
                drawCircle(color.copy(alpha = 0.24f), radius = this.size.minDimension * 0.42f)
                drawCircle(color, radius = this.size.minDimension * 0.22f)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.65f), Color.Transparent),
                        start = Offset(this.size.width * shimmer, 0f),
                        end = Offset(this.size.width * (shimmer + 0.55f), this.size.height)
                    ),
                    size = this.size
                )
            }
        }
    }
}

@Composable
fun AnimatedSubmitButton(
    state: SubmitState,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(18.dp),
        color = PrimaryBlue
    ) {
        Box(contentAlignment = Alignment.Center) {
            AnimatedContent(targetState = state, label = "submitState") { target ->
                when (target) {
                    SubmitState.Idle -> Text(text, color = Color.White, fontWeight = FontWeight.Bold)
                    SubmitState.Loading -> CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    SubmitState.Done -> Text("Reported", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

enum class SubmitState {
    Idle,
    Loading,
    Done
}

@Composable
fun ComplaintIdCard(
    id: String,
    tags: List<String>,
    darkAuditMode: Boolean,
    onDismiss: () -> Unit
) {
    var visibleChars by remember(id) { mutableIntStateOf(0) }
    LaunchedEffect(id) {
        visibleChars = 0
        id.forEachIndexed { index, _ ->
            delay(24L + index * 6L)
            visibleChars = index + 1
        }
    }

    val transition = updateTransition(targetState = id.isNotBlank(), label = "complaintCard")
    val scale by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy) },
        label = "complaintScale"
    ) { if (it) 1f else 0.8f }
    val rotation by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy) },
        label = "complaintRotation"
    ) { if (it) 0f else -2f }

    AnimatedVisibility(
        visible = id.isNotBlank(),
        enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDismiss() }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                },
            darkAuditMode = darkAuditMode
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Complaint logged", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    id.take(visibleChars),
                    color = if (darkAuditMode) Color.White else TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                if (tags.isNotEmpty()) {
                    Text("AI tags: ${tags.joinToString()}", color = TextSecondary, fontSize = 13.sp)
                }
                Text(
                    "Tap to dismiss",
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun CircularGauge(
    value: Float,
    label: String,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = tween(1100, easing = FastOutSlowInEasing),
        label = "gaugeProgress"
    )
    Box(modifier = modifier.size(190.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = PrimaryBlue.copy(alpha = 0.12f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
                size = Size(size.width, size.height)
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(StatusGreen, PrimaryBlue, StatusYellow)),
                startAngle = -90f,
                sweepAngle = animated * 360f,
                useCenter = false,
                style = stroke,
                size = Size(size.width, size.height)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedCounter((animated * 100).toInt(), suffix = "%")
            Text(label, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun AnimatedCounter(value: Int, suffix: String = "") {
    AnimatedContent(targetState = value, label = "counter") { target ->
        Text(
            "$target$suffix",
            color = TextPrimary,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WeeklyBarChart(values: List<Int>, modifier: Modifier = Modifier) {
    val maxValue = max(values.maxOrNull() ?: 1, 1)
    Canvas(modifier = modifier.height(132.dp).fillMaxWidth()) {
        val gap = 12.dp.toPx()
        val barWidth = (size.width - gap * (values.size - 1)) / values.size
        values.forEachIndexed { index, value ->
            val ratio = value / maxValue.toFloat()
            val barHeight = size.height * ratio
            drawRoundRect(
                color = PrimaryBlue.copy(alpha = 0.18f),
                topLeft = Offset(index * (barWidth + gap), 0f),
                size = Size(barWidth, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(PrimaryBlue, StatusGreen)),
                topLeft = Offset(index * (barWidth + gap), size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
            )
        }
    }
}

@Composable
fun TimelineStepper(status: ComplaintStatus, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(
        targetValue = status.stepIndex() / 2f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "timelineProgress"
    )
    val labels = listOf("Submitted", "Assigned", "Fixed")
    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(28.dp)) {
            val y = size.height / 2f
            drawLine(
                color = TextSecondary.copy(alpha = 0.18f),
                start = Offset(16f, y),
                end = Offset(size.width - 16f, y),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = PrimaryBlue,
                start = Offset(16f, y),
                end = Offset(16f + (size.width - 32f) * progress, y),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
            repeat(3) { index ->
                val x = 16f + (size.width - 32f) * (index / 2f)
                val active = index <= status.stepIndex()
                drawCircle(if (active) PrimaryBlue else TextSecondary.copy(alpha = 0.22f), radius = 9f, center = Offset(x, y))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach {
                Text(it, fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}
