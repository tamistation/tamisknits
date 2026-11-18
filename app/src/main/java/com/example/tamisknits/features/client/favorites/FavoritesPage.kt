package com.example.tamisknits.features.client.favorites

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.ui.components.ProductCard
import com.example.tamisknits.features.client.ui.components.ProductCardSkeleton
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.ui.components.AppSnackbarHost
import com.example.tamisknits.ui.components.ContentState
import com.example.tamisknits.ui.components.EmptyView
import com.example.tamisknits.ui.components.ErrorView
import com.example.tamisknits.ui.components.MessageEffect
import com.example.tamisknits.ui.components.OfflineBanner
import com.example.tamisknits.ui.components.ScreenTopBar
import com.example.tamisknits.ui.components.SecondaryButton
import com.example.tamisknits.ui.components.contentStateOf

@Composable
fun FavoritesPage(
    viewModel: FavoritesViewModel,
    onProductClick: (String) -> Unit,
    onBrowseClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    MessageEffect(uiState.message, snackbarHostState, viewModel::consumeMessage)

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            ScreenTopBar(
                title = "Favourites",
                subtitle = when {
                    uiState.isLoading || uiState.products.isEmpty() -> null
                    uiState.products.size == 1 -> "1 saved piece"
                    else -> "${uiState.products.size} saved pieces"
                }
            )
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
                label = "favoritesState"
            ) { state ->
                when (state) {
                    ContentState.LOADING -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(160.dp),
                        contentPadding = PaddingValues(AppDimens.ScreenPadding),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(22.dp),
                        userScrollEnabled = false
                    ) { items(4) { ProductCardSkeleton() } }

                    ContentState.ERROR -> ErrorView(
                        uiState.errorMessage.orEmpty(),
                        viewModel::retry
                    )

                    ContentState.EMPTY -> EmptyView(
                        icon = Icons.Rounded.FavoriteBorder,
                        title = "Nothing saved yet",
                        message = "Tap the heart on any piece you love and it will wait for you here.",
                        actionLabel = "Discover pieces",
                        onAction = onBrowseClick
                    )

                    ContentState.CONTENT -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(160.dp),
                        contentPadding = PaddingValues(
                            start = AppDimens.ScreenPadding,
                            end = AppDimens.ScreenPadding,
                            top = 8.dp,
                            bottom = 32.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.products, key = { it.productId }) { product ->
                            Column(Modifier.animateItem()) {
                                ProductCard(
                                    product = product,
                                    isFavorite = true,
                                    onClick = { onProductClick(product.productId) },
                                    onToggleFavorite = { viewModel.removeFavorite(product.productId) }
                                )
                                Spacer(Modifier.height(10.dp))
                                SecondaryButton(
                                    text = if (product.isInStock) "Move to cart" else "Sold out",
                                    icon = Icons.Rounded.ShoppingBag,
                                    enabled = product.isInStock && product.productId !in uiState.movingToCartIds,
                                    onClick = { viewModel.moveToCart(product) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
