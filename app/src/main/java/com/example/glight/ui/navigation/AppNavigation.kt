package com.example.glight.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.glight.ui.components.PremiumScaffoldBackground
import com.example.glight.ui.screens.admin.AdminScreen
import com.example.glight.ui.screens.login.LoginScreen
import com.example.glight.ui.screens.map.MapScreen
import com.example.glight.ui.screens.repairs.ComplaintDetailScreen
import com.example.glight.ui.screens.repairs.RepairsScreen
import com.example.glight.ui.screens.savings.SavingsScreen
import com.example.glight.ui.theme.CardLight
import com.example.glight.ui.theme.PrimaryBlue
import com.example.glight.ui.theme.SubtleBorder
import com.example.glight.ui.theme.TextPrimary
import com.example.glight.ui.theme.TextSecondary

private object Routes {
    const val LOGIN = "login"
    const val MAP = "map"
    const val REPAIRS = "repairs"
    const val COMPLAINT_DETAIL = "repairs/{complaintId}"
    const val SAVINGS = "savings"
    const val ADMIN = "admin"
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.MAP, "Map", Icons.Default.Map),
    BottomNavItem(Routes.REPAIRS, "Repairs", Icons.Default.Build),
    BottomNavItem(Routes.SAVINGS, "Savings", Icons.Default.Savings),
    BottomNavItem(Routes.ADMIN, "Admin", Icons.Default.AdminPanelSettings),
)

@Composable
fun AppNavigation(
    darkAuditMode: Boolean,
    onDarkAuditModeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != null && currentRoute != Routes.LOGIN && !currentRoute.contains("{")

    PremiumScaffoldBackground(darkAuditMode) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (showBottomBar) {
                    PremiumBottomBar(
                        navController = navController,
                        currentRoute = currentRoute ?: "",
                        darkAuditMode = darkAuditMode,
                        onDarkAuditModeChange = onDarkAuditModeChange
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.LOGIN,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Routes.LOGIN) {
                    LoginScreen(
                        darkAuditMode = darkAuditMode,
                        onLoginSuccess = {
                            navController.navigate(Routes.MAP) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.MAP) {
                    MapScreen(darkAuditMode = darkAuditMode)
                }

                composable(Routes.REPAIRS) {
                    RepairsScreen(
                        darkAuditMode = darkAuditMode,
                        onComplaintClick = { complaintId ->
                            navController.navigate("repairs/$complaintId")
                        }
                    )
                }

                composable(
                    Routes.COMPLAINT_DETAIL,
                    arguments = listOf(navArgument("complaintId") { type = NavType.StringType })
                ) { entry ->
                    val complaintId = entry.arguments?.getString("complaintId") ?: ""
                    ComplaintDetailScreen(
                        complaintId = complaintId,
                        darkAuditMode = darkAuditMode,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Routes.SAVINGS) {
                    SavingsScreen(darkAuditMode = darkAuditMode)
                }

                composable(Routes.ADMIN) {
                    AdminScreen(darkAuditMode = darkAuditMode)
                }
            }
        }
    }
}

@Composable
private fun PremiumBottomBar(
    navController: NavHostController,
    currentRoute: String,
    darkAuditMode: Boolean,
    onDarkAuditModeChange: (Boolean) -> Unit
) {
    val bgColor = if (darkAuditMode) Color(0xFF101826) else CardLight
    val borderColor = if (darkAuditMode) Color(0xFF34A853) else SubtleBorder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(26.dp))
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route
                PremiumTabItem(item, selected, darkAuditMode) {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
            IconButton(onClick = { onDarkAuditModeChange(!darkAuditMode) }) {
                Icon(
                    if (darkAuditMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle audit mode",
                    tint = if (darkAuditMode) Color(0xFFFFE082) else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun PremiumTabItem(
    item: BottomNavItem,
    selected: Boolean,
    darkAuditMode: Boolean,
    onClick: () -> Unit
) {
    val itemScale by animateFloatAsState(
        targetValue = if (selected) 1.13f else 1f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "${item.route}Scale"
    )
    val iconColor = when {
        selected && darkAuditMode -> Color(0xFF4ADE80)
        selected -> PrimaryBlue
        darkAuditMode -> Color.White.copy(alpha = 0.55f)
        else -> TextSecondary
    }
    Column(
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .scale(itemScale)
            .clickable(onClick = onClick, indication = null, interactionSource = null),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(item.icon, contentDescription = item.label, tint = iconColor, modifier = Modifier.size(24.dp))
        if (selected) {
            Text(
                item.label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = iconColor,
                modifier = Modifier.offset(y = (-1).dp)
            )
        }
    }
}
