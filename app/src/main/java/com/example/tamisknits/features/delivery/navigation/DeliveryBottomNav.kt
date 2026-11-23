package com.example.tamisknits.features.delivery.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.pressScale
import com.example.tamisknits.ui.components.readableWidth

private data class DeliveryTab(
    val route: DeliveryRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector
)

private val deliveryTabs = listOf(
    DeliveryTab(
        DeliveryRoute.Deliveries,
        "Deliveries",
        Icons.Rounded.LocalShipping,
        Icons.Outlined.LocalShipping
    ),
    DeliveryTab(DeliveryRoute.Profile, "Profile", Icons.Rounded.Person, Icons.Outlined.Person)
)

// Same look as ClientBottomNav, with the driver's two tabs.
@Composable
fun DeliveryBottomNav(
    currentTab: DeliveryRoute?,
    onTabSelected: (DeliveryRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppColors.Surface,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .readableWidth()
                .height(AppDimens.BottomBarHeight)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            deliveryTabs.forEach { tab ->
                BottomNavItem(
                    tab = tab,
                    selected = tab.route == currentTab,
                    onClick = { onTabSelected(tab.route) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(tab: DeliveryTab, selected: Boolean, onClick: () -> Unit) {
    val background by animateColorAsState(
        if (selected) AppColors.Blush else AppColors.Surface,
        label = "tabBackground"
    )
    val tint by animateColorAsState(
        if (selected) AppColors.Terracotta else AppColors.TextMuted,
        label = "tabTint"
    )
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .pressScale(interactionSource, 0.92f)
            .clip(AppShapes.Pill)
            .background(background)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (selected) tab.selectedIcon else tab.icon,
            contentDescription = tab.label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally() + fadeIn(),
            exit = shrinkHorizontally() + fadeOut()
        ) {
            Text(
                text = tab.label,
                style = AppTextStyles.LabelSmall.copy(color = AppColors.Terracotta),
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
