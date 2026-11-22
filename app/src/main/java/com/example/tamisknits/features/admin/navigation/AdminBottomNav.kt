package com.example.tamisknits.features.admin.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.Headset
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Settings
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
import com.example.tamisknits.theme.AppTextStyles

private data class AdminNavItem(
    val route: AdminRoute,
    val icon: ImageVector,
    val label: String,
)

private val adminNavItems = listOf(
    AdminNavItem(AdminRoute.Orders, Icons.AutoMirrored.Rounded.ListAlt, "Orders"),
    AdminNavItem(AdminRoute.Products, Icons.Rounded.Inventory2, "Products"),
    AdminNavItem(AdminRoute.Support, Icons.Rounded.Headset, "Support"),
    AdminNavItem(AdminRoute.Settings, Icons.Rounded.Settings, "Settings"),
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
                label = { Text(item.label, style = AppTextStyles.LabelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.Terracotta,
                    selectedTextColor = AppColors.Terracotta,
                    indicatorColor = AppColors.Blush,
                    unselectedIconColor = AppColors.TextMuted,
                    unselectedTextColor = AppColors.TextMuted,
                ),
            )
        }
    }
}