package com.example.tamisknits.features.delivery.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.common.ClientFormat
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppTextField
import com.example.tamisknits.ui.components.PrimaryButton

// Screen 3: last step before "delivered". Cash orders need the driver to confirm they collected it.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDeliverySheet(
    uiState: DeliveryDetailsUiState,
    onCashConfirmedChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
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
                .padding(horizontal = AppDimens.ScreenPadding)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Text("Confirm delivery", style = AppTextStyles.Title)
            Spacer(Modifier.height(6.dp))
            Text(
                "Only confirm once the order is in the customer's hands. " +
                        "The customer's tracker will show it as delivered.",
                style = AppTextStyles.BodySmall
            )

            if (uiState.isCashOnDelivery) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.CardSmall)
                        .toggleable(
                            value = uiState.cashConfirmed,
                            enabled = !uiState.isAdvancing,
                            role = Role.Checkbox,
                            onValueChange = onCashConfirmedChange
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.cashConfirmed,
                        onCheckedChange = null,
                        enabled = !uiState.isAdvancing,
                        colors = CheckboxDefaults.colors(checkedColor = AppColors.Terracotta)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "I collected ${ClientFormat.price(uiState.total)} cash",
                        style = AppTextStyles.Label
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = "Confirm delivery",
                onClick = onConfirm,
                enabled = uiState.canConfirmDelivery,
                loading = uiState.isAdvancing,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(
                onClick = onDismiss,
                enabled = !uiState.isAdvancing,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    "Not yet",
                    style = AppTextStyles.Label.copy(color = AppColors.TextSecondary)
                )
            }
        }
    }
}

// Screen 4: flags the order for the admin. The status is left as it is.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportProblemSheet(
    uiState: DeliveryDetailsUiState,
    onReasonChange: (IssueReason) -> Unit,
    onOtherTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
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
                .padding(bottom = 16.dp)
        ) {
            Text("Problem with this delivery?", style = AppTextStyles.Title)
            Spacer(Modifier.height(6.dp))
            Text(
                "The shop will see this on the order and follow up. The order stays where it is.",
                style = AppTextStyles.BodySmall
            )
            Spacer(Modifier.height(12.dp))

            IssueReason.entries.forEach { reason ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.CardSmall)
                        .selectable(
                            selected = uiState.issueReason == reason,
                            enabled = !uiState.isReporting,
                            role = Role.RadioButton,
                            onClick = { onReasonChange(reason) }
                        )
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.issueReason == reason,
                        onClick = null,
                        enabled = !uiState.isReporting,
                        colors = RadioButtonDefaults.colors(selectedColor = AppColors.Terracotta)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(reason.label, style = AppTextStyles.Body)
                }
            }

            if (uiState.issueReason == IssueReason.OTHER) {
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    value = uiState.issueOtherText,
                    onValueChange = onOtherTextChange,
                    label = "What happened?",
                    singleLine = false,
                    minLines = 3,
                    enabled = !uiState.isReporting
                )
            }

            uiState.issueError?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(error, style = AppTextStyles.BodySmall.copy(color = AppColors.ErrorRed))
            }

            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = "Send report",
                onClick = onSubmit,
                loading = uiState.isReporting,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(
                onClick = onDismiss,
                enabled = !uiState.isReporting,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Cancel", style = AppTextStyles.Label.copy(color = AppColors.TextSecondary))
            }
        }
    }
}
