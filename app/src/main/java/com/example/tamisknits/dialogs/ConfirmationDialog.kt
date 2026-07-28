package com.example.tamisknits.dialogs


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.theme.AppColors

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    confirmColor: androidx.compose.ui.graphics.Color = AppColors.Terracotta,
    inputValue: String? = null,
    inputPlaceholder: String = "",
    onInputChange: ((String) -> Unit)? = null,
    confirmEnabled: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(title, fontWeight = FontWeight.Bold, color = AppColors.TextDark)
        },
        text = {
            Column {
                Text(message, fontSize = 14.sp, color = AppColors.TextMuted)

                if (inputValue != null && onInputChange != null) {

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = onInputChange,
                        placeholder = { Text(inputPlaceholder, fontSize = 13.sp) },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Terracotta,
                            unfocusedBorderColor = AppColors.Outline
                        )
                    )
                }
            }
        },


        confirmButton = {
            Button(

                onClick = onConfirm,
                enabled = confirmEnabled,

                colors = ButtonDefaults.buttonColors(
                    containerColor = confirmColor,
                    disabledContainerColor = confirmColor.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(confirmLabel, color = AppColors.Surface)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Go back", color = AppColors.TextMuted)
            }
        }
    )
}