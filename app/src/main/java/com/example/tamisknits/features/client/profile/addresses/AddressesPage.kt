package com.example.tamisknits.features.client.profile.addresses

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.tamisknits.dialogs.ConfirmationDialog
import com.example.tamisknits.features.client.ui.components.AddressForm
import com.example.tamisknits.models.SavedAddress
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppCard
import com.example.tamisknits.ui.components.AppSnackbarHost
import com.example.tamisknits.ui.components.ContentState
import com.example.tamisknits.ui.components.EmptyView
import com.example.tamisknits.ui.components.ErrorView
import com.example.tamisknits.ui.components.LoadingView
import com.example.tamisknits.ui.components.MessageEffect
import com.example.tamisknits.ui.components.PrimaryButton
import com.example.tamisknits.ui.components.ScreenTopBar
import com.example.tamisknits.ui.components.contentStateOf
import com.example.tamisknits.ui.components.readableWidth

@Composable
fun AddressesPage(
    viewModel: AddressesViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    MessageEffect(uiState.message, snackbarHostState, viewModel::consumeMessage)

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = { ScreenTopBar(title = "Saved addresses", onBack = onBack) },
        floatingActionButton = {
            if (!uiState.isEmpty && !uiState.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::addAddress,
                    containerColor = AppColors.Terracotta,
                    contentColor = AppColors.OnTerracotta,
                    shape = AppShapes.Pill,
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("Add address", style = AppTextStyles.Button) }
                )
            }
        }
    ) { padding ->
        Crossfade(
            targetState = contentStateOf(uiState.isLoading, uiState.errorMessage, uiState.isEmpty),
            modifier = Modifier.padding(padding),
            label = "addressesState"
        ) { state ->
            when (state) {
                ContentState.LOADING -> LoadingView()
                ContentState.ERROR -> ErrorView(uiState.errorMessage.orEmpty(), viewModel::retry)
                ContentState.EMPTY -> EmptyView(
                    icon = Icons.Rounded.Place,
                    title = "No saved addresses",
                    message = "Save your home or work address for a faster checkout.",
                    actionLabel = "Add an address",
                    onAction = viewModel::addAddress
                )

                ContentState.CONTENT -> LazyColumn(
                    contentPadding = PaddingValues(
                        start = AppDimens.ScreenPadding,
                        end = AppDimens.ScreenPadding,
                        top = 8.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.addresses, key = { it.addressId }) { address ->
                        AddressCard(
                            address = address,
                            onEdit = { viewModel.editAddress(address) },
                            onSetDefault = { viewModel.setDefault(address.addressId) },
                            onDelete = { viewModel.requestDelete(address.addressId) },
                            modifier = Modifier
                                .readableWidth()
                                .animateItem()
                        )
                    }
                }
            }
        }
    }

    uiState.editor?.let { editor ->
        AddressEditorSheet(
            address = editor,
            errors = uiState.editorErrors,
            isSaving = uiState.isSaving,
            onChange = viewModel::onEditorChange,
            onSave = viewModel::saveEditor,
            onDismiss = viewModel::dismissEditor
        )
    }

    if (uiState.confirmDeleteId != null) {
        ConfirmationDialog(
            title = "Remove this address?",
            message = "You can always add it again later.",
            confirmLabel = "Remove",
            confirmColor = AppColors.Cancelled,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDelete
        )
    }
}

@Composable
private fun AddressCard(
    address: SavedAddress,
    onEdit: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier, onClick = onEdit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                address.label,
                style = AppTextStyles.Subtitle,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (address.isDefault) {
                Spacer(Modifier.width(8.dp))
                Row(
                    modifier = Modifier
                        .clip(AppShapes.Pill)
                        .background(AppColors.Blush)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = null,
                        tint = AppColors.Terracotta,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Default",
                        style = AppTextStyles.Caption.copy(color = AppColors.Terracotta)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(address.fullName, style = AppTextStyles.Label)
        Text(address.singleLine, style = AppTextStyles.BodySmall)
        if (address.phone.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Phone,
                    contentDescription = null,
                    tint = AppColors.TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(address.phone, style = AppTextStyles.Caption)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row {
            TextButton(onClick = onEdit) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = AppColors.Terracotta,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Edit", style = AppTextStyles.LabelSmall.copy(color = AppColors.Terracotta))
            }
            if (!address.isDefault) {
                TextButton(onClick = onSetDefault) {
                    Text(
                        "Set as default",
                        style = AppTextStyles.LabelSmall.copy(color = AppColors.Terracotta)
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = AppColors.Cancelled,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Remove", style = AppTextStyles.LabelSmall.copy(color = AppColors.Cancelled))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressEditorSheet(
    address: SavedAddress,
    errors: Map<AddressField, String>,
    isSaving: Boolean,
    onChange: (SavedAddress) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.Surface,
        shape = AppShapes.Sheet
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppDimens.ScreenPadding)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Text(
                if (address.addressId.isBlank()) "New address" else "Edit address",
                style = AppTextStyles.Title
            )
            Spacer(Modifier.height(16.dp))
            AddressForm(
                address = address,
                onChange = onChange,
                fullNameError = errors[AddressField.FULL_NAME],
                phoneError = errors[AddressField.PHONE],
                streetError = errors[AddressField.STREET],
                cityError = errors[AddressField.CITY]
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Use as default address",
                    style = AppTextStyles.Body,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = address.isDefault,
                    onCheckedChange = { onChange(address.copy(isDefault = it)) },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = AppColors.Terracotta,
                        checkedThumbColor = AppColors.OnTerracotta
                    )
                )
            }
            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                text = "Save address",
                onClick = onSave,
                loading = isSaving,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
