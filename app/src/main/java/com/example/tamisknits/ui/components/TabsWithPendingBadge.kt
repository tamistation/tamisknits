package com.example.tamisknits.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.theme.AppColors

@Composable
fun <T> TabsWithPendingBadge(
    tabs: List<T>,
    labels: List<String>,
    selectedTab: T,
    onTabSelected: (T) -> Unit,
    pendingTab: T,
    pendingCount: Int,
) {
    TabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        containerColor = AppColors.Cream,
        contentColor = AppColors.Terracotta,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[tabs.indexOf(selectedTab)]),
                color = AppColors.Terracotta,
            )
        },
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = labels[index],
                            fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == tab) AppColors.Terracotta else AppColors.TextMuted,
                            fontSize = 14.sp,
                        )
                        if (tab == pendingTab && pendingCount > 0) {
                            Spacer(Modifier.width(4.dp))
                            Badge(containerColor = AppColors.Terracotta) {
                                Text("$pendingCount", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                },
            )
        }
    }
}