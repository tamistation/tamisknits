package com.example.tamisknits.features.admin.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tamisknits.models.ProductVariant
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.ui.Loader
import com.example.tamisknits.ui.components.PageHeader

@Composable
fun AddProductPage(
    viewModel: AddProductViewModel,
    onProductAdded: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    AddProductUI(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onCategoryChange = viewModel::onCategoryChange,
        onImageSelected = viewModel::onImageSelected,
        onCustomizableChange = viewModel::onCustomizableChange,
        onAddVariant = viewModel::addVariant,
        onRemoveVariant = viewModel::removeVariant,
        onUpdateVariant = viewModel::updateVariant,
        onSaveProduct = {
            viewModel.addProduct(
                onSuccess = onProductAdded
            )
        },
        onBackClick = onBackClick
    )
}

@Composable
fun AddProductUI(
    uiState: AddProductUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onImageSelected: (String?) -> Unit,
    onCustomizableChange: (Boolean) -> Unit,
    onAddVariant: () -> Unit,
    onRemoveVariant: (Int) -> Unit,
    onUpdateVariant: (Int, ProductVariant) -> Unit,
    onSaveProduct: () -> Unit,
    onBackClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            bottom = 24.dp
        )
    ) {

        item {
            PageHeader(
                title = "Add Product",
                subtitle = "Create a new product"
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Product name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                CategoryDropdown(
                    selectedCategory = uiState.category,
                    onCategoryChange = onCategoryChange
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Customizable",
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextDark
                        )

                        Text(
                            text = "Allow customers to customize this product",
                            fontSize = 12.sp,
                            color = AppColors.TextMuted
                        )
                    }

                    Switch(
                        checked = uiState.isCustomizable,
                        onCheckedChange = onCustomizableChange
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Product Image",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                ImagePicker(
                    imageUri = uiState.imageUri,
                    onImageSelected = onImageSelected
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Variants",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextDark,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = onAddVariant
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "Add Variant",
                            color = AppColors.Terracotta
                        )
                    }
                }
            }
        }

        itemsIndexed(
            items = uiState.variants
        ) { index, variant ->

            VariantCard(
                variantNumber = index + 1,
                variant = variant,
                canDelete = uiState.variants.size > 1,
                onVariantChange = {
                    onUpdateVariant(index, it)
                },
                onDelete = {
                    onRemoveVariant(index)
                }
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = uiState.errorMessage,
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onSaveProduct,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Terracotta
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        Loader(
                            modifier = Modifier.size(22.dp),
                            color = AppColors.Surface
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Save Product")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Go Back",
                        color = AppColors.TextMuted
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            listOf("Bags", "Clutches").forEach { category ->

                DropdownMenuItem(
                    text = {
                        Text(category)
                    },
                    onClick = {
                        onCategoryChange(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ImagePicker(
    imageUri: String?,
    onImageSelected: (String?) -> Unit
) {

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->

        onImageSelected(uri?.toString())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.DragBackground
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            if (imageUri != null) {

                AsyncImage(
                    model = imageUri,
                    contentDescription = "Product image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

            } else {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = AppColors.TextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Product image",
                        color = AppColors.TextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                        }
                    ) {
                        Text("Choose Image")
                    }
                }
            }
        }
    }
}

@Composable
private fun VariantCard(
    variantNumber: Int,
    variant: ProductVariant,
    canDelete: Boolean,
    onVariantChange: (ProductVariant) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 6.dp
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Variant $variantNumber",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextDark,
                    modifier = Modifier.weight(1f)
                )

                if (canDelete) {
                    IconButton(
                        onClick = onDelete
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete variant",
                            tint = AppColors.TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = variant.size,
                onValueChange = {
                    onVariantChange(
                        variant.copy(size = it)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Size") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = variant.color,
                onValueChange = {
                    onVariantChange(
                        variant.copy(color = it)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Color") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = variant.stock.toString(),
                onValueChange = { value ->
                    val stock = value.toIntOrNull() ?: 0

                    onVariantChange(
                        variant.copy(stock = stock)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Stock") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = variant.price.toString(),
                onValueChange = { value ->
                    val price = value.toDoubleOrNull() ?: 0.0

                    onVariantChange(
                        variant.copy(price = price)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Price") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}