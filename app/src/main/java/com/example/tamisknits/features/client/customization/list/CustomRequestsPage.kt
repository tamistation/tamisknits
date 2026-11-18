package com.example.tamisknits.features.client.customization.list

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.common.ClientFormat
import com.example.tamisknits.features.client.ui.components.ProductRowSkeleton
import com.example.tamisknits.models.ClientCustomizationRequest
import com.example.tamisknits.models.CustomizationOptions
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppCard
import com.example.tamisknits.ui.components.ContentState
import com.example.tamisknits.ui.components.EmptyView
import com.example.tamisknits.ui.components.ErrorView
import com.example.tamisknits.ui.components.OfflineBanner
import com.example.tamisknits.ui.components.ScreenTopBar
import com.example.tamisknits.ui.components.StatusPill
import com.example.tamisknits.ui.components.contentStateOf
import com.example.tamisknits.ui.components.readableWidth

@Composable
fun CustomRequestsPage(
    viewModel: CustomRequestsViewModel,
    onBack: () -> Unit,
    onNewRequest: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        topBar = { ScreenTopBar(title = "Custom requests", onBack = onBack) },
        floatingActionButton = {
            if (!uiState.isEmpty) {
                ExtendedFloatingActionButton(
                    onClick = onNewRequest,
                    containerColor = AppColors.Terracotta,
                    contentColor = AppColors.OnTerracotta,
                    shape = AppShapes.Pill,
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("New request", style = AppTextStyles.Button) }
                )
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OfflineBanner(uiState.isOffline)
            Crossfade(
                targetState = contentStateOf(
                    uiState.isLoading,
                    uiState.errorMessage,
                    uiState.isEmpty
                ),
                label = "requestsState"
            ) { state ->
                when (state) {
                    ContentState.LOADING -> Column(Modifier.padding(AppDimens.ScreenPadding)) {
                        repeat(3) { ProductRowSkeleton(Modifier.padding(vertical = 8.dp)) }
                    }

                    ContentState.ERROR -> ErrorView(
                        uiState.errorMessage.orEmpty(),
                        viewModel::retry
                    )

                    ContentState.EMPTY -> EmptyView(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "No custom requests yet",
                        message = "Have a bag in mind that isn't in the shop? Describe it and we'll make it for you.",
                        actionLabel = "Request a custom bag",
                        onAction = onNewRequest
                    )

                    ContentState.CONTENT -> LazyColumn(
                        contentPadding = PaddingValues(
                            start = AppDimens.ScreenPadding,
                            end = AppDimens.ScreenPadding,
                            top = 8.dp,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.requests, key = { it.requestId }) { request ->
                            RequestCard(
                                request = request,
                                productName = uiState.productNames[request.request.productId],
                                modifier = Modifier.readableWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestCard(
    request: ClientCustomizationRequest,
    productName: String?,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    AppCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(productName?.let { "Custom $it" } ?: "Custom bag",
                    style = AppTextStyles.Subtitle)
                Text(ClientFormat.date(request.createdAt), style = AppTextStyles.Caption)
            }
            StatusPill(
                CustomizationOptions.statusLabel(request.status),
                AppColors.requestStatusColor(request.status)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (request.request.colorPreference.isNotBlank()) {
                Box(
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(AppColors.swatch(request.request.colorPreference))
                        .border(1.dp, AppColors.Outline, CircleShape)
                )
                Spacer(Modifier.width(6.dp))
                Text(request.request.colorPreference, style = AppTextStyles.LabelSmall)
            }
            if (request.request.sizePreference.isNotBlank()) {
                Text("  ·  ${request.request.sizePreference}", style = AppTextStyles.LabelSmall)
            }
        }
        if (request.request.details.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                request.request.details,
                style = AppTextStyles.BodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (request.referenceImageUrl.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.CardSmall)
                    .background(AppColors.SurfaceMuted)
                    .clickable { runCatching { uriHandler.openUri(request.referenceImageUrl) } }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Link,
                    contentDescription = null,
                    tint = AppColors.Terracotta,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    request.referenceImageUrl.removePrefix("https://").removePrefix("http://"),
                    style = AppTextStyles.Caption.copy(color = AppColors.Terracotta),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
