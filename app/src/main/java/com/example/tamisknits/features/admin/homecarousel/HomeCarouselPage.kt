package com.example.tamisknits.features.admin.homecarousel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.ui.components.ClientImage
import com.example.tamisknits.features.client.ui.image.ClientImageResolver
import com.example.tamisknits.models.HomeCarousel
import com.example.tamisknits.models.Products
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.PageHeader
import com.example.tamisknits.ui.components.PrimaryButton

@Composable
fun HomeCarouselPage(
    viewModel: HomeCarouselViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        containerColor = AppColors.Cream,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(AppColors.Cream)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                PrimaryButton(
                    text = if (uiState.hasChanges) "Save carousel" else "Saved",
                    onClick = viewModel::save,
                    enabled = uiState.hasChanges,
                    loading = uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PageHeader(
                title = "Home carousel",
                subtitle = "Pick what clients swipe through first",
                onBackClick = onBackClick
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Terracotta)
                }
                return@Column
            }

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "selected-header") {
                    SectionLabel(
                        "ON THE CAROUSEL · ${uiState.selectedProducts.size}/${HomeCarousel.MAX_PRODUCTS}",
                        hint = "Top of the list shows first. Use the arrows to reorder."
                    )
                }

                if (uiState.selectedProducts.isEmpty()) {
                    item(key = "selected-empty") {
                        EmptyNote("Nothing picked yet, so clients see your newest in-stock products. Add some below.")
                    }
                } else {
                    itemsIndexed(
                        uiState.selectedProducts,
                        key = { _, p -> "sel-${p.productId}" }) { index, product ->
                        SelectedRow(
                            position = index + 1,
                            product = product,
                            canMoveUp = index > 0,
                            canMoveDown = index < uiState.selectedProducts.lastIndex,
                            onMoveUp = { viewModel.moveUp(product.productId) },
                            onMoveDown = { viewModel.moveDown(product.productId) },
                            onRemove = { viewModel.remove(product.productId) }
                        )
                    }
                }

                item(key = "available-header") {
                    Spacer(Modifier.size(8.dp))
                    SectionLabel(
                        "ADD PRODUCTS",
                        hint = if (uiState.isFull) "The carousel is full. Remove one to add another." else null
                    )
                }

                if (uiState.availableProducts.isEmpty()) {
                    item(key = "available-empty") {
                        EmptyNote("Every product is already on the carousel.")
                    }
                } else {
                    items(uiState.availableProducts, key = { "add-${it.productId}" }) { product ->
                        AvailableRow(
                            product = product,
                            enabled = !uiState.isFull,
                            onAdd = { viewModel.add(product.productId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, hint: String? = null) {
    Column(Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp)) {
        Text(text, style = AppTextStyles.Overline)
        hint?.let {
            Spacer(Modifier.size(2.dp))
            Text(it, style = AppTextStyles.Caption)
        }
    }
}

@Composable
private fun EmptyNote(text: String) {
    Text(
        text,
        style = AppTextStyles.BodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.SurfaceMuted)
            .padding(16.dp)
    )
}

@Composable
private fun ProductRowCard(content: @Composable RowScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) { content() }
    }
}

@Composable
private fun SelectedRow(
    position: Int,
    product: Products,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    ProductRowCard {
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(AppColors.Blush),
            contentAlignment = Alignment.Center
        ) {
            Text("$position", style = AppTextStyles.LabelSmall.copy(color = AppColors.Terracotta))
        }
        Spacer(Modifier.width(10.dp))
        Thumbnail(product)
        Spacer(Modifier.width(12.dp))
        ProductText(product, Modifier.weight(1f))
        Column {
            IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "Move ${product.name} up",
                    tint = if (canMoveUp) AppColors.Terracotta else AppColors.OutlineSoft
                )
            }
            IconButton(
                onClick = onMoveDown,
                enabled = canMoveDown,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Move ${product.name} down",
                    tint = if (canMoveDown) AppColors.Terracotta else AppColors.OutlineSoft
                )
            }
        }
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Remove ${product.name}",
                tint = AppColors.TextMuted
            )
        }
    }
}

@Composable
private fun AvailableRow(product: Products, enabled: Boolean, onAdd: () -> Unit) {
    ProductRowCard {
        Thumbnail(product)
        Spacer(Modifier.width(12.dp))
        ProductText(product, Modifier.weight(1f))
        IconButton(onClick = onAdd, enabled = enabled) {
            Icon(
                Icons.Rounded.AddCircle,
                contentDescription = "Add ${product.name} to the carousel",
                tint = if (enabled) AppColors.Terracotta else AppColors.OutlineSoft
            )
        }
    }
}

@Composable
private fun Thumbnail(product: Products) {
    val context = LocalContext.current
    val model = remember(product.productId, product.imageUrl) {
        ClientImageResolver.productImage(context, product)
    }
    ClientImage(
        model = model,
        contentDescription = null,
        placeholderIconSize = 22.dp,
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.DragBackground)
    )
}

@Composable
private fun ProductText(product: Products, modifier: Modifier) {
    val minPrice = product.variants.minOfOrNull { it.price }
    val stock = product.variants.sumOf { it.stock }
    Column(modifier) {
        Text(
            product.name,
            style = AppTextStyles.Label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            listOfNotNull(
                product.category.takeIf { it.isNotBlank() },
                minPrice?.let { "$%.2f".format(it) },
                if (stock == 0) "Out of stock" else null
            ).joinToString(" · "),
            style = AppTextStyles.Caption.copy(color = if (stock == 0) AppColors.Cancelled else AppColors.TextMuted),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
