package com.example.tamisknits.features.admin.products.add

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.models.ProductVariant
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.ui.Loader
import com.example.tamisknits.ui.components.CategoryDropdown
import com.example.tamisknits.ui.components.ImagePicker
import com.example.tamisknits.ui.components.PageHeader
import com.example.tamisknits.ui.components.VariantCard

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
        onCustomizableChange = viewModel::onCustomizableChange,
        onImageSelected = viewModel::onImageSelected,
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
    onCustomizableChange: (Boolean) -> Unit,
    onImageSelected: (String?) -> Unit,
    onAddVariant: () -> Unit,
    onRemoveVariant: (Int) -> Unit,
    onUpdateVariant: (Int, ProductVariant) -> Unit,
    onSaveProduct: () -> Unit,
    onBackClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
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



