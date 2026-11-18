package com.example.tamisknits.features.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Work
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tamisknits.models.SavedAddress
import com.example.tamisknits.ui.components.AppChip
import com.example.tamisknits.ui.components.AppTextField

private val addressLabels = listOf(
    "Home" to Icons.Rounded.Home,
    "Work" to Icons.Rounded.Work,
    "Other" to Icons.Rounded.Place
)

@Composable
fun AddressForm(
    address: SavedAddress,
    onChange: (SavedAddress) -> Unit,
    modifier: Modifier = Modifier,
    showPhone: Boolean = true,
    fullNameError: String? = null,
    phoneError: String? = null,
    streetError: String? = null,
    cityError: String? = null
) {
    val words =
        KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            addressLabels.forEach { (label, icon) ->
                AppChip(
                    label = label,
                    selected = address.label.equals(label, ignoreCase = true),
                    onClick = { onChange(address.copy(label = label)) },
                    leadingIcon = icon
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        AppTextField(
            value = address.fullName,
            onValueChange = { onChange(address.copy(fullName = it)) },
            label = "Full name",
            error = fullNameError,
            keyboardOptions = words
        )
        if (showPhone) {
            Spacer(Modifier.height(4.dp))
            AppTextField(
                value = address.phone,
                onValueChange = { onChange(address.copy(phone = it)) },
                label = "Phone number",
                error = phoneError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )
        }
        Spacer(Modifier.height(4.dp))
        AppTextField(
            value = address.street,
            onValueChange = { onChange(address.copy(street = it)) },
            label = "Street address",
            error = streetError,
            leadingIcon = Icons.Rounded.Place,
            keyboardOptions = words
        )
        Spacer(Modifier.height(4.dp))
        AppTextField(
            value = address.building,
            onValueChange = { onChange(address.copy(building = it)) },
            label = "Building, floor, apartment (optional)",
            leadingIcon = Icons.Rounded.Apartment,
            keyboardOptions = words
        )
        Spacer(Modifier.height(4.dp))
        AppTextField(
            value = address.city,
            onValueChange = { onChange(address.copy(city = it)) },
            label = "City",
            error = cityError,
            keyboardOptions = words
        )
        Spacer(Modifier.height(4.dp))
        AppTextField(
            value = address.notes,
            onValueChange = { onChange(address.copy(notes = it)) },
            label = "Directions for the courier (optional)",
            singleLine = false,
            minLines = 2,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
    }
}
