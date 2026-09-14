package com.example.tamisknits.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
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
