package com.example.glight.ui.screens.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.glight.domain.model.Pole
import com.example.glight.domain.model.PoleStatus
import com.example.glight.ui.components.AnimatedSubmitButton
import com.example.glight.ui.components.ComplaintIdCard
import com.example.glight.ui.components.GlassCard
import com.example.glight.ui.components.StatusVisual
import com.example.glight.ui.components.SubmitState
import com.example.glight.ui.components.label
import com.example.glight.ui.components.statusColor
import com.example.glight.ui.theme.PrimaryBlue
import com.example.glight.ui.theme.TextPrimary
import com.example.glight.ui.theme.TextSecondary
import com.example.glight.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    darkAuditMode: Boolean,
    viewModel: MapViewModel = hiltViewModel()
) {
    val poles by viewModel.poles.collectAsStateWithLifecycle()
    val lastReport by viewModel.lastReport.collectAsStateWithLifecycle()
    val reportState by viewModel.reportState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val onReportUpdated by rememberUpdatedState<(Pole, String, String) -> Unit> { pole, status, notes ->
        viewModel.reportPoleIssue(pole, status, notes)
    }

    var isListView by remember { mutableStateOf(true) }
    var selectedPole by remember { mutableStateOf<Pole?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<com.google.android.gms.maps.model.LatLng?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        userLocation = com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                // Handle exception
            }
        }
    }

    val mapsAvailable = remember {
        val key = BuildConfig.MAPS_API_KEY
        key.isNotBlank() && !key.contains("YOUR_") && !key.contains("API_KEY")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (poles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading poles...", color = TextSecondary)
                }
            }
        } else if (isListView || !mapsAvailable) {
            PoleDirectory(
                poles = poles,
                darkAuditMode = darkAuditMode,
                onPoleClick = { selectedPole = it }
            )
            
            if (!mapsAvailable && !isListView) {
                FallbackMapView(
                    poles = poles,
                    darkAuditMode = darkAuditMode,
                    onPoleClick = { selectedPole = it }
                )
            }
        } else {
            GoogleMapView(
                poles = poles,
                darkAuditMode = darkAuditMode,
                onPoleClick = { selectedPole = it },
                userLocation = userLocation,
                isMyLocationEnabled = hasLocationPermission
            )
        }

        FloatingMapHeader(
            poleCount = poles.size,
            darkAuditMode = darkAuditMode,
            isListView = isListView || !mapsAvailable,
            onToggleView = {
                if (mapsAvailable) isListView = !isListView
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, top = 16.dp, end = 118.dp)
        )

        FloatingActionButton(
            onClick = { selectedPole = poles.firstOrNull() },
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 18.dp, bottom = 112.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Report issue")
        }

        selectedPole?.let { pole ->
            ModalBottomSheet(
                onDismissRequest = { selectedPole = null },
                sheetState = sheetState,
                containerColor = Color.Transparent,
                dragHandle = null
            ) {
                ReportSheet(
                    pole = pole,
                    darkAuditMode = darkAuditMode,
                    reportState = reportState,
                    onSubmit = { status ->
                        onReportUpdated(pole, status.name, "Mobile Audit")
                    },
                    onDismiss = {
                        selectedPole = null
                        viewModel.clearLastReport()
                    }
                )
            }
        }

        lastReport?.let { report ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 120.dp, start = 16.dp, end = 16.dp)
                    .clickable { viewModel.clearLastReport() }
            ) {
                ComplaintIdCard(
                    id = report.complaintId,
                    tags = report.tags,
                    darkAuditMode = darkAuditMode,
                    onDismiss = viewModel::clearLastReport
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 96.dp)
        )
    }

    LaunchedEffect(lastReport) {
        lastReport?.let {
            snackbarHostState.showSnackbar("Report submitted: ${it.complaintId}")
        }
    }
}

@Composable
private fun GoogleMapView(
    poles: List<Pole>,
    darkAuditMode: Boolean,
    onPoleClick: (Pole) -> Unit,
    userLocation: com.google.android.gms.maps.model.LatLng?,
    isMyLocationEnabled: Boolean
) {
    val defaultLocation = com.google.android.gms.maps.model.LatLng(12.9716, 77.5946)
    val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(userLocation ?: defaultLocation, 14f)
    }
    
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(it, 16f)
            )
        }
    }
    val markerIcons = remember {
        mapOf(
            PoleStatus.WORKING to com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
            ),
            PoleStatus.FUSED to com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
            ),
            PoleStatus.BURNING_DAYTIME to com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_YELLOW
            )
        )
    }

    val lowZoom by remember {
        derivedStateOf { cameraPositionState.position.zoom < 13f && poles.size > 3 }
    }
    val clusterCenter by remember {
        derivedStateOf {
            if (poles.isEmpty()) defaultLocation else com.google.android.gms.maps.model.LatLng(
                poles.map { it.lat }.average(),
                poles.map { it.lng }.average()
            )
        }
    }
    val clusterMarkerState = com.google.maps.android.compose.rememberMarkerState(position = clusterCenter)
    LaunchedEffect(clusterCenter) { clusterMarkerState.position = clusterCenter }

    com.google.maps.android.compose.GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = com.google.maps.android.compose.MapProperties(
            mapType = com.google.maps.android.compose.MapType.NORMAL,
            isMyLocationEnabled = isMyLocationEnabled
        ),
        uiSettings = com.google.maps.android.compose.MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = false,
            myLocationButtonEnabled = true
        )
    ) {
        if (lowZoom) {
            com.google.maps.android.compose.Marker(
                state = clusterMarkerState,
                title = "${poles.size} village poles",
                snippet = "Zoom in to inspect individual lamp posts",
                icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                    com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE
                )
            )
        } else {
            poles.forEach { pole ->
                key(pole.id) {
                    val polePosition = com.google.android.gms.maps.model.LatLng(pole.lat, pole.lng)
                    val markerState = com.google.maps.android.compose.rememberMarkerState(position = polePosition)
                    LaunchedEffect(polePosition) { markerState.position = polePosition }
                    com.google.maps.android.compose.Marker(
                        state = markerState,
                        title = "Pole ${pole.id}",
                        snippet = pole.status.label(),
                        icon = markerIcons[pole.status],
                        onClick = { onPoleClick(pole); true }
                    )
                }
            }
        }
    }
}

@Composable
private fun FallbackMapView(
    poles: List<Pole>,
    darkAuditMode: Boolean,
    onPoleClick: (Pole) -> Unit
) {
    val minLat = poles.minOfOrNull { it.lat } ?: 0.0
    val maxLat = poles.maxOfOrNull { it.lat } ?: 1.0
    val minLng = poles.minOfOrNull { it.lng } ?: 0.0
    val maxLng = poles.maxOfOrNull { it.lng } ?: 1.0
    val latRange = (maxLat - minLat).coerceAtLeast(0.001)
    val lngRange = (maxLng - minLng).coerceAtLeast(0.001)

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bgColor = if (darkAuditMode) Color(0xFF1A2332) else Color(0xFFE8F0FE)
            drawRect(bgColor)
            drawRect(Color.Gray.copy(alpha = 0.1f))

            poles.forEach { pole ->
                val x = ((pole.lng - minLng) / lngRange * (size.width * 0.8f) + size.width * 0.1f).toFloat()
                val y = ((1.0 - (pole.lat - minLat) / latRange) * (size.height * 0.6f) + size.height * 0.2f).toFloat()
                val color = pole.status.statusColor()
                drawCircle(color.copy(alpha = 0.3f), radius = 28f, center = Offset(x, y))
                drawCircle(color, radius = 14f, center = Offset(x, y))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            GlassCard(darkAuditMode = darkAuditMode, cornerRadius = 16.dp) {
                Column {
                    Text(
                        "Simulated Map View",
                        fontWeight = FontWeight.Bold,
                        color = if (darkAuditMode) Color.White else TextPrimary
                    )
                    Text(
                        "Google Maps API key not configured. Add MAPS_API_KEY to local.properties for the full map experience.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingMapHeader(
    poleCount: Int,
    darkAuditMode: Boolean,
    isListView: Boolean,
    onToggleView: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth(), darkAuditMode = darkAuditMode) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Village Audit",
                        color = if (darkAuditMode) Color.White else TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = Color(0x2234A853), shape = RoundedCornerShape(12.dp)) {
                        Text(
                            "LIVE",
                            color = Color(0xFF34A853),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Text("$poleCount registered streetlights", color = TextSecondary, fontSize = 13.sp)
            }
            IconButton(onClick = onToggleView) {
                Icon(
                    if (isListView) Icons.Default.Map else Icons.AutoMirrored.Filled.List,
                    contentDescription = "Toggle view",
                    tint = PrimaryBlue
                )
            }
        }
    }
}

@Composable
private fun PoleDirectory(poles: List<Pole>, darkAuditMode: Boolean, onPoleClick: (Pole) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 110.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(poles, key = { it.id }) { pole ->
            PoleListItem(pole = pole, darkAuditMode = darkAuditMode, onClick = { onPoleClick(pole) })
        }
    }
}

@Composable
fun PoleListItem(pole: Pole, darkAuditMode: Boolean, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        cornerRadius = 20.dp,
        darkAuditMode = darkAuditMode
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusVisual(pole.status)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(pole.id, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (darkAuditMode) Color.White else TextPrimary)
                    Text(pole.bulbType, fontSize = 12.sp, color = TextSecondary)
                }
            }
            Surface(color = pole.status.statusColor().copy(alpha = 0.15f), shape = RoundedCornerShape(999.dp)) {
                Text(
                    pole.status.label(),
                    color = pole.status.statusColor(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReportSheet(
    pole: Pole,
    darkAuditMode: Boolean,
    reportState: ReportState,
    onSubmit: (PoleStatus) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStatus by remember(pole.id) { mutableStateOf(pole.status) }
    val submitState = when (reportState) {
        is ReportState.Loading -> SubmitState.Loading
        is ReportState.Success -> SubmitState.Done
        else -> SubmitState.Idle
    }

    LaunchedEffect(reportState) {
        if (reportState is ReportState.Success) {
            kotlinx.coroutines.delay(800)
            onDismiss()
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        cornerRadius = 30.dp,
        darkAuditMode = darkAuditMode
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Quick Report", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Pole ${pole.id}", color = if (darkAuditMode) Color.White else TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Choose the visible lamp status and submit it to Panchayat.", color = TextSecondary, lineHeight = 20.sp)

            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusOptionTile(Icons.Default.Check, PoleStatus.WORKING, selectedStatus, darkAuditMode) { selectedStatus = it }
                StatusOptionTile(Icons.Default.Lightbulb, PoleStatus.FUSED, selectedStatus, darkAuditMode) { selectedStatus = it }
                StatusOptionTile(Icons.Default.Lightbulb, PoleStatus.BURNING_DAYTIME, selectedStatus, darkAuditMode) { selectedStatus = it }
            }

            AnimatedSubmitButton(
                state = submitState,
                text = "Submit Report",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = submitState == SubmitState.Idle) { onSubmit(selectedStatus) }
            )

            if (reportState is ReportState.Error) {
                Text(
                    reportState.message,
                    color = Color(0xFFEA4335),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StatusOptionTile(
    icon: ImageVector,
    status: PoleStatus,
    selectedStatus: PoleStatus,
    darkAuditMode: Boolean,
    onSelect: (PoleStatus) -> Unit
) {
    val selected = status == selectedStatus
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "${status.name}TileScale"
    )
    val bg = if (selected) status.statusColor().copy(alpha = 0.16f) else Color.Transparent
    GlassCard(
        modifier = Modifier.width(150.dp).scale(scale).clickable { onSelect(status) },
        cornerRadius = 22.dp, contentPadding = 14.dp, darkAuditMode = darkAuditMode
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(42.dp).clickable { onSelect(status) }, contentAlignment = Alignment.Center) {
                Surface(color = bg, shape = RoundedCornerShape(15.dp)) {
                    Icon(icon, contentDescription = status.label(), tint = status.statusColor(), modifier = Modifier.padding(10.dp))
                }
            }
            Text(status.label(), color = if (darkAuditMode) Color.White else TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}
