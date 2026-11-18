package com.example.tamisknits.features.client.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.CountBadge
import com.example.tamisknits.ui.components.pressScale
import com.example.tamisknits.ui.components.readableWidth

private data class ClientTab(
    val route: ClientRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
    val matches: (ClientRoute?) -> Boolean
)

private val clientTabs = listOf(
    ClientTab(
        ClientRoute.Home,
        "Home",
        Icons.Rounded.Home,
        Icons.Outlined.Home
    ) { it is ClientRoute.Home },
    ClientTab(
        ClientRoute.Catalog(),
        "Shop",
        Icons.Rounded.Storefront,
        Icons.Outlined.Storefront
    ) { it is ClientRoute.Catalog },
    ClientTab(
        ClientRoute.Favorites,
        "Saved",
        Icons.Rounded.Favorite,
        Icons.Outlined.FavoriteBorder
    ) { it is ClientRoute.Favorites },
    ClientTab(
        ClientRoute.Cart,
        "Cart",
        Icons.Rounded.ShoppingBag,
        Icons.Outlined.ShoppingBag
    ) { it is ClientRoute.Cart },
    ClientTab(
        ClientRoute.Profile,
        "Profile",
        Icons.Rounded.Person,
        Icons.Outlined.Person
    ) { it is ClientRoute.Profile }
)

@Composable
fun ClientBottomNav(
    currentTab: ClientRoute?,
    cartCount: Int,
    onTabSelected: (ClientRoute) -> Unit,
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
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            clientTabs.forEach { tab ->
                BottomNavItem(
                    tab = tab,
                    selected = tab.matches(currentTab),
                    badgeCount = if (tab.route == ClientRoute.Cart) cartCount else 0,
                    onClick = { onTabSelected(tab.route) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: ClientTab,
    selected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        if (selected) AppColors.Blush else AppColors.Surface,
        label = "tabBackground"
    )
    val tint by animateColorAsState(
        if (selected) AppColors.Terracotta else AppColors.TextMuted,
        label = "tabTint"
    )
    val interactionSource = remember { MutableInteractionSource() }

    // The cart icon gives a little bounce whenever something is added.
    val badgeBounce = remember { Animatable(1f) }
    LaunchedEffect(badgeCount) {
        if (badgeCount > 0) {
            badgeBounce.snapTo(1.35f)
            badgeBounce.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 500f))
        }
    }

    Row(
        modifier = Modifier
            .pressScale(interactionSource, 0.92f)
            .clip(AppShapes.Pill)
            .background(background)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Icon(
                imageVector = if (selected) tab.selectedIcon else tab.icon,
                contentDescription = tab.label,
                tint = tint,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = badgeBounce.value
                        scaleY = badgeBounce.value
                    }
            )
            if (badgeCount > 0) {
                CountBadge(
                    count = badgeCount,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-6).dp)
                )
            }
        }
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
