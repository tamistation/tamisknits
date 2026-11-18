package com.example.tamisknits.features.client.catalog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.common.ClientFormat
import com.example.tamisknits.features.client.ui.components.ProductCard
import com.example.tamisknits.features.client.ui.components.ProductCardSkeleton
import com.example.tamisknits.features.client.ui.components.ProductListRow
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppChip
import com.example.tamisknits.ui.components.AppSnackbarHost
import com.example.tamisknits.ui.components.CircleIconButton
import com.example.tamisknits.ui.components.ColorSwatch
import com.example.tamisknits.ui.components.ContentState
import com.example.tamisknits.ui.components.EmptyView
import com.example.tamisknits.ui.components.ErrorView
import com.example.tamisknits.ui.components.MessageEffect
import com.example.tamisknits.ui.components.OfflineBanner
import com.example.tamisknits.ui.components.PrimaryButton
import com.example.tamisknits.ui.components.ScreenTopBar
import com.example.tamisknits.ui.components.SearchField
import com.example.tamisknits.ui.components.SecondaryButton
import com.example.tamisknits.ui.components.contentStateOf

private val GridMinCellWidth = 160.dp

@Composable
fun CatalogPage(
    viewModel: CatalogViewModel,
    onProductClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val searchFocus = remember { FocusRequester() }
    MessageEffect(uiState.message, snackbarHostState, viewModel::consumeMessage)

    LaunchedEffect(uiState.focusSearch, uiState.isLoading) {
        if (uiState.focusSearch && !uiState.isLoading) {
            runCatching { searchFocus.requestFocus() }
            viewModel.onSearchFocused()
        }
    }

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            ScreenTopBar(
                title = "Shop",
                subtitle = if (uiState.isLoading) null else "${uiState.products.size} handmade pieces",
                actions = {
                    CircleIconButton(
                        icon = if (uiState.layout == CatalogLayout.GRID) Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                        contentDescription = "Switch layout",
                        onClick = viewModel::toggleLayout
                    )
                    Spacer(Modifier.width(8.dp))
                    CircleIconButton(
                        icon = Icons.Rounded.Tune,
                        contentDescription = "Filters",
                        onClick = { viewModel.showFilters(true) },
                        badgeCount = uiState.filters.activeCount
                    )
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            SearchField(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                focusRequester = searchFocus,
                modifier = Modifier.padding(horizontal = AppDimens.ScreenPadding)
            )
            if (uiState.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(
                        horizontal = AppDimens.ScreenPadding,
                        vertical = 12.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        AppChip(
                            label = "All",
                            selected = uiState.filters.category == null,
                            onClick = { viewModel.onCategorySelected(null) }
                        )
                    }
                    items(uiState.categories, key = { it }) { category ->
                        AppChip(
                            label = category,
                            selected = uiState.filters.category.equals(category, ignoreCase = true),
                            onClick = { viewModel.onCategorySelected(category) }
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(12.dp))
            }
            SortRow(uiState.sort, uiState.products.size, uiState.isLoading, viewModel::onSortChange)
            OfflineBanner(uiState.isOffline)

            Crossfade(
                targetState = contentStateOf(
                    uiState.isLoading,
                    uiState.errorMessage,
                    uiState.products.isEmpty()
                ),
                label = "catalogState"
            ) { state ->
                when (state) {
                    ContentState.LOADING -> CatalogSkeleton()
                    ContentState.ERROR -> ErrorView(
                        uiState.errorMessage.orEmpty(),
                        viewModel::retry
                    )

                    ContentState.EMPTY -> if (uiState.hasActiveQuery) {
                        EmptyView(
                            icon = Icons.Rounded.SearchOff,
                            title = "No pieces match",
                            message = "Try a different search or loosen your filters.",
                            actionLabel = "Clear filters",
                            onAction = viewModel::clearFilters
                        )
                    } else {
                        EmptyView(
                            icon = Icons.Rounded.SearchOff,
                            title = "The shop is empty",
                            message = "New handmade pieces will appear here soon."
                        )
                    }

                    ContentState.CONTENT -> CatalogResults(
                        uiState,
                        onProductClick,
                        viewModel::toggleFavorite
                    )
                }
            }
        }
    }

    if (uiState.showFilters) {
        FilterSheet(
            uiState = uiState,
            onDismiss = { viewModel.showFilters(false) },
            onCategorySelected = viewModel::onCategorySelected,
            onColorToggled = viewModel::onColorToggled,
            onPriceRangeChange = viewModel::onPriceRangeChange,
            onStockFilterChange = viewModel::onStockFilterChange,
            onClear = viewModel::clearFilters
        )
    }
}

@Composable
private fun SortRow(
    sort: CatalogSort,
    resultCount: Int,
    isLoading: Boolean,
    onSortChange: (CatalogSort) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.ScreenPadding)
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isLoading) "" else "$resultCount ${if (resultCount == 1) "result" else "results"}",
            style = AppTextStyles.Caption,
            modifier = Modifier.weight(1f)
        )
        Column {
            Row(
                modifier = Modifier
                    .clip(AppShapes.Pill)
                    .clickable { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Sort,
                    contentDescription = null,
                    tint = AppColors.Terracotta,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    sort.label,
                    style = AppTextStyles.LabelSmall.copy(color = AppColors.Terracotta)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = AppColors.Surface,
                shape = AppShapes.CardSmall
            ) {
                CatalogSort.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label, style = AppTextStyles.Label) },
                        trailingIcon = {
                            if (option == sort) Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = AppColors.Terracotta
                            )
                        },
                        onClick = {
                            onSortChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogResults(
    uiState: CatalogUiState,
    onProductClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    AnimatedContent(
        targetState = uiState.layout,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "catalogLayout"
    ) { layout ->
        when (layout) {
            CatalogLayout.GRID -> LazyVerticalGrid(
                columns = GridCells.Adaptive(GridMinCellWidth),
                contentPadding = PaddingValues(
                    start = AppDimens.ScreenPadding,
                    end = AppDimens.ScreenPadding,
                    top = 4.dp,
                    bottom = 32.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.products, key = { it.productId }) { product ->
                    ProductCard(
                        product = product,
                        isFavorite = product.productId in uiState.favoriteIds,
                        onClick = { onProductClick(product.productId) },
                        onToggleFavorite = { onToggleFavorite(product.productId) },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            CatalogLayout.LIST -> LazyColumn(
                contentPadding = PaddingValues(
                    start = AppDimens.ScreenPadding,
                    end = AppDimens.ScreenPadding,
                    top = 4.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.products, key = { it.productId }) { product ->
                    ProductListRow(
                        product = product,
                        isFavorite = product.productId in uiState.favoriteIds,
                        onClick = { onProductClick(product.productId) },
                        onToggleFavorite = { onToggleFavorite(product.productId) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogSkeleton() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(GridMinCellWidth),
        contentPadding = PaddingValues(AppDimens.ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
        userScrollEnabled = false,
        modifier = Modifier.fillMaxSize()
    ) {
        items(6) { ProductCardSkeleton() }
        item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(1.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterSheet(
    uiState: CatalogUiState,
    onDismiss: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onColorToggled: (String) -> Unit,
    onPriceRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onStockFilterChange: (StockFilter) -> Unit,
    onClear: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val bounds = uiState.priceBounds
    var priceRange by remember(uiState.filters.priceRange, bounds) {
        mutableStateOf(uiState.filters.priceRange ?: bounds)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Surface,
        shape = AppShapes.Sheet
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppDimens.ScreenPadding)
                .navigationBarsPadding()
        ) {
            Text("Filters", style = AppTextStyles.Title)
            Spacer(Modifier.height(20.dp))

            if (uiState.categories.isNotEmpty()) {
                FilterLabel("Category")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppChip("All", uiState.filters.category == null, { onCategorySelected(null) })
                    uiState.categories.forEach { category ->
                        AppChip(
                            label = category,
                            selected = uiState.filters.category.equals(category, ignoreCase = true),
                            onClick = { onCategorySelected(category) }
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            if (uiState.colors.isNotEmpty()) {
                FilterLabel("Colour")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.colors.forEach { color ->
                        ColorSwatch(
                            colorName = color,
                            selected = uiState.filters.colors.any {
                                it.equals(
                                    color,
                                    ignoreCase = true
                                )
                            },
                            onClick = { onColorToggled(color) },
                            showLabel = true
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            if (bounds.endInclusive > bounds.start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterLabel("Price", Modifier.weight(1f))
                    Text(
                        "${ClientFormat.price(priceRange.start.toDouble())} – ${
                            ClientFormat.price(
                                priceRange.endInclusive.toDouble()
                            )
                        }",
                        style = AppTextStyles.LabelSmall.copy(color = AppColors.Terracotta)
                    )
                }
                RangeSlider(
                    value = priceRange,
                    onValueChange = { priceRange = it },
                    onValueChangeFinished = { onPriceRangeChange(priceRange) },
                    valueRange = bounds,
                    colors = SliderDefaults.colors(
                        thumbColor = AppColors.Terracotta,
                        activeTrackColor = AppColors.Terracotta,
                        inactiveTrackColor = AppColors.Blush
                    )
                )
                Spacer(Modifier.height(16.dp))
            }

            FilterLabel("Availability")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StockFilter.entries.forEach { option ->
                    AppChip(
                        label = option.label,
                        selected = uiState.filters.stock == option,
                        onClick = { onStockFilterChange(option) }
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(text = "Clear", onClick = onClear, modifier = Modifier.weight(1f))
                PrimaryButton(
                    text = "Show ${uiState.products.size}",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1.4f)
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FilterLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = AppTextStyles.Overline,
        modifier = modifier.padding(bottom = 10.dp)
    )
}
