package com.example.tamisknits.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.models.ProductVariant
import com.example.tamisknits.theme.AppColors


@Composable
fun VariantCard(
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

            Spacer(modifier = Modifier.height(12.dp))

            ImagePicker(
                imageUri = variant.imageUrl,
                onImageSelected = { imageUri ->
                    onVariantChange(
                        variant.copy(
                            imageUrl = imageUri ?: ""
                        )
                    )
                }
            )
        }
    }
}