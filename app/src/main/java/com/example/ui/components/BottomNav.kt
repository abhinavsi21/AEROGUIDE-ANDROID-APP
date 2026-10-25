package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class AeroNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    RADAR("Radar", Icons.Filled.Radar, Icons.Outlined.Radar, "nav_radar"),
    NAVIGATE("Map", Icons.Filled.NearMe, Icons.Outlined.NearMe, "nav_navigate"),
    ALERTS("Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications, "nav_alerts"),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
}

@Composable
fun AeroBottomNav(
    currentTab: AeroNavTab,
    unreadAlertCount: Int,
    onTabSelected: (AeroNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            AeroNavTab.values().forEach { tab ->
                val isSelected = currentTab == tab
                NavigationBarItem(
                    modifier = Modifier.testTag(tab.testTag),
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (tab == AeroNavTab.ALERTS && unreadAlertCount > 0) {
                                    Badge(
                                        containerColor = AeroCritical,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            unreadAlertCount.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(23.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AeroSkyBlue,
                        selectedTextColor = AeroSkyBlue,
                        indicatorColor = AeroBlue.copy(alpha = 0.18f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                )
            }
        }
    }
}
