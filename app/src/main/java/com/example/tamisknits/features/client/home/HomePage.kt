package com.example.tamisknits.features.client.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.common.ClientFormat
import com.example.tamisknits.features.client.ui.components.FavoriteButton
import com.example.tamisknits.features.client.ui.components.ProductCard
import com.example.tamisknits.features.client.ui.components.ProductCardSkeleton
import com.example.tamisknits.features.client.ui.components.ProductImage
import com.example.tamisknits.features.client.ui.components.PromoBanner
import com.example.tamisknits.models.ClientProduct
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppChip
import com.example.tamisknits.ui.components.AppSnackbarHost
import com.example.tamisknits.ui.components.ContentState
import com.example.tamisknits.ui.components.EmptyView
import com.example.tamisknits.ui.components.ErrorView
import com.example.tamisknits.ui.components.MessageEffect
import com.example.tamisknits.ui.components.OfflineBanner
import com.example.tamisknits.ui.components.SearchBarButton
import com.example.tamisknits.ui.components.SectionHeader
import com.example.tamisknits.ui.components.ShimmerBox
import com.example.tamisknits.ui.components.contentStateOf

@Composable
fun HomePage(
    viewModel: HomeViewModel,
    onProductClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onBrowseAllClick: () -> Unit,
    onCustomBagClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    MessageEffect(uiState.message, snackbarHostState, viewModel::consumeMessage)

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) }
    ) { padding ->
        Crossfade(
            targetState = contentStateOf(uiState.isLoading, uiState.errorMessage, uiState.isEmpty),
            modifier = Modifier.padding(padding),
            label = "homeState"
        ) { state ->
            when (state) {
                ContentState.LOADING -> HomeSkeleton()
                ContentState.ERROR -> ErrorView(uiState.errorMessage.orEmpty(), viewModel::retry)
                ContentState.EMPTY -> EmptyView(
                    icon = Icons.Rounded.Storefront,
                    title = "New pieces are on the way",
                    message = "Our shelves are being restocked with fresh handmade bags. Check back soon!",
                    actionLabel = "Request a custom bag",
                    onAction = onCustomBagClick
                )

                ContentState.CONTENT -> HomeContent(
                    uiState = uiState,
                    onProductClick = onProductClick,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onSearchClick = onSearchClick,
                    onCategoryClick = onCategoryClick,
                    onBrowseAllClick = onBrowseAllClick,
                    onCustomBagClick = onCustomBagClick
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onProductClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSearchClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onBrowseAllClick: () -> Unit,
    onCustomBagClick: () -> Unit
) {
    val gutter = AppDimens.ScreenPadding
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item(key = "header") {
            Column(Modifier.padding(start = gutter, end = gutter, top = 16.dp)) {
                Text(
                    text = if (uiState.userName.isNotBlank()) "Hello, ${uiState.userName}" else "Welcome to Tami's Knits",
                    style = AppTextStyles.LabelSmall
                )
                Spacer(Modifier.height(4.dp))
                Text("Find your next\nfavourite bag", style = AppTextStyles.DisplayItalic)
                Spacer(Modifier.height(18.dp))
                SearchBarButton(onClick = onSearchClick)
            }
        }
        item(key = "offline") { OfflineBanner(uiState.isOffline, Modifier.padding(top = 8.dp)) }

        if (uiState.featured.isNotEmpty()) {
            item(key = "featured") {
                FeaturedCarousel(
                    products = uiState.featured,
                    favoriteIds = uiState.favoriteIds,
                    onProductClick = onProductClick,
                    onToggleFavorite = onToggleFavorite,
                    modifier = Modifier.padding(top = 22.dp)
                )
            }
        }

        if (uiState.categories.isNotEmpty()) {
            item(key = "categories") {
                Column(Modifier.padding(top = AppDimens.SectionSpacing)) {
                    SectionHeader(
                        title = "Shop by category",
                        modifier = Modifier.padding(horizontal = gutter)
                    )
                    Spacer(Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = gutter),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { AppChip(label = "All", selected = true, onClick = onBrowseAllClick) }
                        items(uiState.categories, key = { it }) { category ->
                            AppChip(
                                label = category,
                                selected = false,
                                onClick = { onCategoryClick(category) })
                        }
                    }
                }
            }
        }

        productRow(
            key = "new",
            title = "New arrivals",
            subtitle = "Fresh off the needles",
            products = uiState.newArrivals,
            favoriteIds = uiState.favoriteIds,
            onSeeAll = onBrowseAllClick,
            onProductClick = onProductClick,
            onToggleFavorite = onToggleFavorite
        )

        item(key = "promo") {
            PromoBanner(
                overline = "Made just for you",
                title = "Dream up your own bag",
                message = "Pick the colour and size, share an inspiration link, and we'll knit it by hand.",
                actionLabel = "Request a custom bag",
                onAction = onCustomBagClick,
                modifier = Modifier.padding(
                    start = gutter,
                    end = gutter,
                    top = AppDimens.SectionSpacing
                )
            )
        }

        productRow(
            key = "best",
            title = "Most loved",
            subtitle = "Our customers' favourites",
            products = uiState.bestSellers,
            favoriteIds = uiState.favoriteIds,
            onSeeAll = onBrowseAllClick,
            onProductClick = onProductClick,
            onToggleFavorite = onToggleFavorite
        )

        productRow(
            key = "recent",
            title = "Recently viewed",
            subtitle = null,
            products = uiState.recentlyViewed,
            favoriteIds = uiState.favoriteIds,
            onSeeAll = null,
            onProductClick = onProductClick,
            onToggleFavorite = onToggleFavorite,
            cardWidth = 132
        )
    }
}

private fun LazyListScope.productRow(
    key: String,
    title: String,
    subtitle: String?,
    products: List<ClientProduct>,
    favoriteIds: Set<String>,
    onSeeAll: (() -> Unit)?,
    onProductClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    cardWidth: Int = 164
) {
    if (products.isEmpty()) return
    item(key = key) {
        Column(Modifier.padding(top = AppDimens.SectionSpacing)) {
            SectionHeader(
                title = title,
                subtitle = subtitle,
                onAction = onSeeAll,
                modifier = Modifier.padding(horizontal = AppDimens.ScreenPadding)
            )
            Spacer(Modifier.height(14.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = AppDimens.ScreenPadding),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(products, key = { it.productId }) { product ->
                    ProductCard(
                        product = product,
                        isFavorite = product.productId in favoriteIds,
                        onClick = { onProductClick(product.productId) },
                        onToggleFavorite = { onToggleFavorite(product.productId) },
                        modifier = Modifier.width(cardWidth.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedCarousel(
    products: List<ClientProduct>,
    favoriteIds: Set<String>,
    onProductClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { products.size })
    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = AppDimens.ScreenPadding),
            pageSpacing = 12.dp
        ) { page ->
            val product = products[page]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.3f)
                    .clip(AppShapes.Card)
                    .clickable { onProductClick(product.productId) }
            ) {
                ProductImage(
                    product = product.product,
                    modifier = Modifier.matchParentSize(),
                    placeholderIconSize = 56.dp
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                0.45f to Color.Transparent,
                                1f to AppColors.DarkTerracotta.copy(alpha = 0.78f)
                            )
                        )
                )
                FavoriteButton(
                    isFavorite = product.productId in favoriteIds,
                    onToggle = { onToggleFavorite(product.productId) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp)
                ) {
                    Text(
                        "FEATURED",
                        style = AppTextStyles.Overline.copy(color = AppColors.BlushDeep)
                    )
                    Text(
                        product.name,
                        style = AppTextStyles.Title.copy(color = Color.White),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        ClientFormat.price(product.minPrice),
                        style = AppTextStyles.Label.copy(color = Color.White)
                    )
                }
            }
        }
        if (products.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(products.size) { index ->
                    val selected = pagerState.currentPage == index
                    val width by animateDpAsState(if (selected) 22.dp else 7.dp, label = "dotWidth")
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(7.dp)
                            .width(width)
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) AppColors.Terracotta else AppColors.BlushDeep)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppDimens.ScreenPadding, vertical = 16.dp)
    ) {
        ShimmerBox(Modifier
            .width(120.dp)
            .height(14.dp), RoundedCornerShape(4.dp))
        Spacer(Modifier.height(10.dp))
        ShimmerBox(Modifier
            .width(220.dp)
            .height(64.dp), RoundedCornerShape(8.dp))
        Spacer(Modifier.height(18.dp))
        ShimmerBox(Modifier
            .fillMaxWidth()
            .height(52.dp), AppShapes.Pill)
        Spacer(Modifier.height(22.dp))
        ShimmerBox(Modifier
            .fillMaxWidth()
            .aspectRatio(1.3f), AppShapes.Card)
        Spacer(Modifier.height(28.dp))
        ShimmerBox(Modifier
            .width(140.dp)
            .height(18.dp), RoundedCornerShape(4.dp))
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(3) { ProductCardSkeleton(Modifier.width(164.dp)) }
        }
    }
}

