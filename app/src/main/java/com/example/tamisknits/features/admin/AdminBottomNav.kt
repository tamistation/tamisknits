package com.example.tamisknits.features.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.tamisknits.navigation.AdminRoute
import com.example.tamisknits.theme.AppColors

private data class AdminNavItem(
    val route: AdminRoute,
    val icon: ImageVector,
    val label: String,
)

private val adminNavItems = listOf(
    AdminNavItem(AdminRoute.Orders, Icons.Default.ListAlt, "Orders"),
    AdminNavItem(AdminRoute.Support, Icons.Default.Headset, "Support"),
    AdminNavItem(AdminRoute.Settings, Icons.Default.Settings, "Settings"),
)

@Composable
fun AdminBottomNav(
    currentRoute: AdminRoute?,
    onRouteSelected: (AdminRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier,
        containerColor = AppColors.Surface,
        contentColor = AppColors.Terracotta,
    ) {
        adminNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onRouteSelected(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.Terracotta,
                    selectedTextColor = AppColors.Terracotta,
                    indicatorColor = AppColors.DragBackground,
                    unselectedIconColor = AppColors.TextMuted,
                    unselectedTextColor = AppColors.TextMuted,
                ),
            )
        }
    }
}