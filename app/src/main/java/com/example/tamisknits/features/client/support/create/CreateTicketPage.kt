package com.example.tamisknits.features.client.support.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
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
fun CreateTicketPage(
    viewModel: CreateTicketViewModel,
    onBack: () -> Unit,
    onCreated: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    MessageEffect(uiState.message, snackbarHostState, viewModel::consumeMessage)

    LaunchedEffect(uiState.createdTicketId) {
        uiState.createdTicketId?.let { ticketId ->
            viewModel.consumeCreated()
            onCreated(ticketId)
        }
    }

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = { ScreenTopBar(title = "New ticket", onBack = onBack) }
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
                Text("What's it about?", style = AppTextStyles.Subtitle)
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TicketTopic.entries.forEach { topic ->
                        AppChip(
                            label = topic.label,
                            selected = uiState.topic == topic,
                            onClick = { viewModel.onTopicSelected(topic) }
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                AppTextField(
                    value = uiState.subject,
                    onValueChange = viewModel::onSubjectChange,
                    label = "Subject",
                    error = uiState.fieldErrors[CreateTicketField.SUBJECT],
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    value = uiState.body,
                    onValueChange = viewModel::onBodyChange,
                    label = "How can we help?",
                    singleLine = false,
                    minLines = 6,
                    error = uiState.fieldErrors[CreateTicketField.BODY],
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    text = "Send",
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
