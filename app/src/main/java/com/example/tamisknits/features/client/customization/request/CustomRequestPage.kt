package com.example.tamisknits.features.client.customization.request

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppChip
import com.example.tamisknits.ui.components.AppSnackbarHost
import com.example.tamisknits.ui.components.AppTextField
import com.example.tamisknits.ui.components.MessageEffect
import com.example.tamisknits.ui.components.PrimaryButton
import com.example.tamisknits.ui.components.ScreenTopBar
import com.example.tamisknits.ui.components.readableWidth

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomRequestPage(
    viewModel: CustomRequestViewModel,
    onBack: () -> Unit,
    onSubmitted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    MessageEffect(uiState.message, snackbarHostState, viewModel::consumeMessage)

    LaunchedEffect(uiState.submittedRequestId) {
        if (uiState.submittedRequestId != null) {
            viewModel.consumeSubmitted()
            onSubmitted()
        }
    }

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = { ScreenTopBar(title = "Custom bag", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .readableWidth()
                    .padding(horizontal = AppDimens.ScreenPadding, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.Card)
                        .background(AppColors.Blush)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = AppColors.Terracotta,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "MADE TO ORDER",
                            style = AppTextStyles.Overline.copy(color = AppColors.Terracotta)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Design a bag that's truly yours", style = AppTextStyles.Title)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (uiState.productName.isNotBlank()) "Based on ${uiState.productName}. Tell us what you'd change and we'll get back to you with a quote."
                        else "Choose a colour and size, describe your idea, and we'll get back to you with a quote.",
                        style = AppTextStyles.BodySmall
                    )
                }

                FieldTitle("Colour", uiState.fieldErrors[CustomRequestField.COLOR])
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.colorOptions.forEach { color ->
                        AppChip(
                            label = color,
                            selected = uiState.color == color,
                            onClick = { viewModel.onColorSelected(color) },
                            leadingColor = AppColors.swatch(color)
                        )
                    }
                }

                FieldTitle("Size", uiState.fieldErrors[CustomRequestField.SIZE])
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.sizeOptions.forEach { size ->
                        AppChip(
                            label = size,
                            selected = uiState.size == size,
                            onClick = { viewModel.onSizeSelected(size) }
                        )
                    }
                }

                FieldTitle("Describe your idea", null)
                AppTextField(
                    value = uiState.details,
                    onValueChange = viewModel::onDetailsChange,
                    label = "Pattern, strap, lining, details you'd love…",
                    singleLine = false,
                    minLines = 5,
                    error = uiState.fieldErrors[CustomRequestField.DETAILS],
                    supportingText = "${uiState.detailsRemaining} characters left",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                FieldTitle("Inspiration link", null)
                AppTextField(
                    value = uiState.referenceLink,
                    onValueChange = viewModel::onReferenceLinkChange,
                    label = "Instagram or Pinterest link (optional)",
                    leadingIcon = Icons.Rounded.Link,
                    error = uiState.fieldErrors[CustomRequestField.LINK],
                    supportingText = "Paste a link to a photo that shows the look you want",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    text = "Send request",
                    icon = Icons.AutoMirrored.Rounded.Send,
                    onClick = viewModel::submit,
                    loading = uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun FieldTitle(title: String, error: String?) {
    Row(
        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = AppTextStyles.Subtitle)
        if (error != null) {
            Spacer(Modifier.width(10.dp))
            Text(error, style = AppTextStyles.Caption.copy(color = AppColors.Cancelled))
        }
    }
}
