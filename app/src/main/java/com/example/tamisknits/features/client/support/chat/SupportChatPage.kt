package com.example.tamisknits.features.client.support.chat

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.common.ClientFormat
import com.example.tamisknits.features.client.support.SupportUseCase
import com.example.tamisknits.models.TicketMessage
import com.example.tamisknits.models.TicketStatuses
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.theme.AppType
import com.example.tamisknits.ui.components.AppSnackbarHost
import com.example.tamisknits.ui.components.EmptyView
import com.example.tamisknits.ui.components.ErrorView
import com.example.tamisknits.ui.components.LoadingView
import com.example.tamisknits.ui.components.MessageEffect
import com.example.tamisknits.ui.components.ScreenTopBar

private data class ChatBubbleData(
    val key: String,
    val text: String,
    val fromClient: Boolean,
    val time: String
)

@Composable
fun SupportChatPage(
    viewModel: SupportChatViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    MessageEffect(uiState.sendError, snackbarHostState, viewModel::consumeSendError)

    Scaffold(
        containerColor = AppColors.Cream,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            ScreenTopBar(
                title = uiState.ticket?.subject?.ifBlank { null } ?: "Support chat",
                subtitle = uiState.ticket?.let { TicketStatuses.label(it.status) },
                onBack = onBack
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .imePadding()
        ) {
            when {
                uiState.isLoading -> LoadingView()
                uiState.errorMessage != null -> ErrorView(uiState.errorMessage.orEmpty(), onBack)
                uiState.notFound || uiState.ticket == null -> EmptyView(
                    icon = Icons.Rounded.Forum,
                    title = "Conversation not found",
                    message = "This ticket may have been removed.",
                    actionLabel = "Go back",
                    onAction = onBack
                )

                else -> ChatContent(uiState, messages, viewModel)
            }
        }
    }
}

@Composable
private fun ChatContent(
    uiState: SupportChatUiState,
    messages: List<TicketMessage>,
    viewModel: SupportChatViewModel
) {
    val ticket = uiState.ticket ?: return
    // The ticket's opening message isn't in TicketMessages, so it's shown as the first bubble.
    val bubbles = remember(ticket, messages) {
        listOf(ChatBubbleData("opening", ticket.message, fromClient = true, time = "")) +
                messages.map { message ->
                    ChatBubbleData(
                        key = message.messageId,
                        text = message.message,
                        fromClient = message.senderType != SupportUseCase.SENDER_ADMIN,
                        time = ClientFormat.time(message.sentAt * 1000)
                    )
                }
    }
    val listState = rememberLazyListState()
    LaunchedEffect(bubbles.size) {
        if (bubbles.isNotEmpty()) listState.animateScrollToItem(bubbles.lastIndex)
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = AppDimens.ScreenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bubbles, key = { it.key }) { bubble ->
                ChatBubble(bubble, Modifier.animateItem())
            }
            if (bubbles.size == 1 && !uiState.isClosed) {
                item(key = "waiting") {
                    Text(
                        "Thanks for reaching out! Our team will reply here soon.",
                        style = AppTextStyles.Caption,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }
        }
        if (uiState.isClosed) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.DeliveredSoft)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = AppColors.Delivered,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "This conversation was marked as resolved.",
                    style = AppTextStyles.LabelSmall.copy(color = AppColors.TextDark)
                )
            }
        } else {
            ChatInput(
                value = uiState.messageInput,
                onValueChange = viewModel::onMessageInputChange,
                onSend = viewModel::sendMessage
            )
        }
    }
}

@Composable
private fun ChatBubble(bubble: ChatBubbleData, modifier: Modifier = Modifier) {
    val shape = if (bubble.fromClient) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp)
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (bubble.fromClient) Alignment.End else Alignment.Start
    ) {
        if (!bubble.fromClient) {
            Text(
                "Tami's Knits",
                style = AppType.BrandSmall.copy(color = AppColors.Terracotta),
                modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
            )
        }
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(if (bubble.fromClient) AppColors.Terracotta else AppColors.Surface)
                .then(
                    if (bubble.fromClient) Modifier else Modifier.border(
                        1.dp,
                        AppColors.OutlineSoft,
                        shape
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                bubble.text,
                style = AppTextStyles.Body.copy(color = if (bubble.fromClient) AppColors.OnTerracotta else AppColors.TextDark)
            )
        }
        if (bubble.time.isNotBlank()) {
            Text(
                bubble.time,
                style = AppTextStyles.Caption,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun ChatInput(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    val canSend = value.isNotBlank()
    val sendColor by animateColorAsState(
        if (canSend) AppColors.Terracotta else AppColors.BlushDeep,
        label = "sendColor"
    )
    Surface(color = AppColors.Surface, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(AppShapes.Pill)
                    .background(AppColors.SurfaceMuted)
                    .padding(horizontal = 18.dp, vertical = 13.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        "Write a message…",
                        style = AppTextStyles.Body.copy(color = AppColors.TextMuted)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = AppTextStyles.Body.copy(color = AppColors.TextDark),
                    cursorBrush = SolidColor(AppColors.Terracotta),
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(sendColor)
                    .clickable(enabled = canSend, onClick = onSend),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send",
                    tint = AppColors.OnTerracotta,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
