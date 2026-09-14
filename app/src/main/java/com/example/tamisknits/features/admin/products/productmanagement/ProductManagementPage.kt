package com.example.tamisknits.features.admin.products.productmanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.dialogs.ConfirmationDialog
import com.example.tamisknits.models.Products
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.ui.Loader
import com.example.tamisknits.ui.components.AppSearchBar
import com.example.tamisknits.ui.components.PageHeader

@Composable
fun ProductManagementPage(
    viewModel: ProductManagementViewModel,
    onAddProductClick: () -> Unit,
    onEditProductClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()

    ProductManagementUI(
        uiState = uiState,
        products = products,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCategorySelected = viewModel::onCategorySelected,
        onDeleteClick = viewModel::onDeleteClick,
        onConfirmDelete = viewModel::confirmDelete,
        onDismissDeleteDialog = viewModel::dismissDeleteDialog,
        onAddProductClick = onAddProductClick,
        onEditProductClick = onEditProductClick
    )
}

@Composable
fun ProductManagementUI(
    uiState: ProductManagementUiState,
    products: List<Products>,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onAddProductClick: () -> Unit,
    onEditProductClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        PageHeader(
            title = "Products",
            subtitle = "Manage your products"
        )

        AppSearchBar(
            query = uiState.searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = "Search products..."
        )

        Spacer(modifier = Modifier.height(12.dp))

        CategoryFilters(
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = onCategorySelected
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onAddProductClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Terracotta
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Add Product")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Loader()
            }

        } else if (products.isEmpty()) {

            EmptyProductsState()

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = products,
                    key = { product -> product.productId }
                ) { product ->

                    ProductCard(
                        product = product,
                        onEditClick = {
                            onEditProductClick(product.productId)
                        },
                        onDeleteClick = {
                            onDeleteClick(product.productId)
                        }
                    )
                }
            }
        }
    }

    if (uiState.deleteDialogProductId != null) {
        ConfirmationDialog(
            title = "Delete Product",
            message = "Are you sure you want to delete this product?",
            confirmLabel = "Delete",
            confirmColor = AppColors.Terracotta,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDeleteDialog
        )
    }
}

@Composable
private fun CategoryFilters(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        CategoryButton(
            text = "All",
            selected = selectedCategory == "All",
            onClick = {
                onCategorySelected("All")
            }
        )

        CategoryButton(
            text = "Bags",
            selected = selectedCategory == "Bags",
            onClick = {
                onCategorySelected("Bags")
            }
        )

        CategoryButton(
            text = "Clutches",
            selected = selectedCategory == "Clutches",
            onClick = {
                onCategorySelected("Clutches")
            }
        )
    }
}

@Composable
private fun CategoryButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                AppColors.Terracotta
            } else {
                AppColors.Surface
            },
            contentColor = if (selected) {
                AppColors.Surface
            } else {
                AppColors.TextDark
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text)
    }
}

@Composable
private fun EmptyProductsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = AppColors.TextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No products found",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Try another search or category",
                fontSize = 13.sp,
                color = AppColors.TextMuted
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: Products,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val totalStock = product.variants.sumOf { variant ->
        variant.stock
    }

    val prices = product.variants.map { variant ->
        variant.price
    }

    val minPrice = prices.minOrNull() ?: 0.0
    val maxPrice = prices.maxOrNull() ?: 0.0

    val priceText = if (minPrice == maxPrice) {
        "$%.2f".format(minPrice)
    } else {
        "$%.2f - $%.2f".format(
            minPrice,
            maxPrice
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            ProductImage(
                imageUrl = product.variants
                    .firstOrNull()
                    ?.imageUrl
                    ?: ""
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = product.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextDark
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.category,
                    fontSize = 13.sp,
                    color = AppColors.TextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${product.variants.size} variants",
                    fontSize = 13.sp,
                    color = AppColors.TextDark
                )

                Text(
                    text = "Stock: $totalStock",
                    fontSize = 13.sp,
                    color = AppColors.TextDark
                )

                Text(
                    text = priceText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Terracotta
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                IconButton(
                    onClick = onEditClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit product",
                        tint = AppColors.Terracotta
                    )
                }

                IconButton(
                    onClick = onDeleteClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete product",
                        tint = AppColors.TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductImage(
    imageUrl: String
) {
    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.DragBackground),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = Icons.Default.Inventory2,
            contentDescription = null,
            tint = AppColors.TextMuted,
            modifier = Modifier.size(32.dp)
        )
    }
}