package com.example.tamisknits.features.admin.support.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.models.TicketMessage
import com.example.tamisknits.theme.AppColors

@Composable
fun TicketChatPage(
    viewModel: TicketChatViewModel,
    onBack: () -> Unit,
    onClosed: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()//listens to the current state of the ui(current input text,..)
    val messages by viewModel.messages.collectAsState()//listens to the list of messsages from firestore

    TicketChatUI(
//passind down the data to the composable
        uiState = uiState,
        messages = messages,
        onBack = onBack,
        onCloseTicket = { viewModel.closeTicket(onClosed) },
        onMessageInputChange = viewModel::onMessageInputChange,
        onSend = viewModel::sendMessage,
        onConsumeSendError = viewModel::consumeSendError,
    )
}

@Composable
private fun TicketChatUI(
    uiState: TicketChatUiState,
    messages: List<TicketMessage>,
    onBack: () -> Unit,
    onCloseTicket: () -> Unit,
    onMessageInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onConsumeSendError: () -> Unit,
) {
    val listState = rememberLazyListState()
//LazyListState is an object that keeps track of what is happening with a scrollable list

    LaunchedEffect(messages.size) {//when the messages list changes
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)//scrolls to the last message
        }
    }

    Column(Modifier.fillMaxSize().background(AppColors.Cream)) {
        ChatHeader(
            clientName = uiState.clientName,
            subject = uiState.subject,
            onBack = onBack,
            onCloseTicket = onCloseTicket,
        )

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Terracotta)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)//fills the remaining space to the bottom
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(messages, key = { it.messageId }) { message ->
                    ChatBubble(message)
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }

        ChatInputBar(
            value = uiState.messageInput,
            onValueChange = onMessageInputChange,
            onSend = onSend,
        )
    }

    uiState.sendError?.let { error ->
        AlertDialog(
            onDismissRequest = onConsumeSendError,
            confirmButton = {
                TextButton(onClick = onConsumeSendError) { Text("Close") }
            },
            text = { Text(error) },//if an error occurs
        )
    }
}

@Composable
private fun ChatHeader(
    clientName: String,
    subject: String,
    onBack: () -> Unit,
    onCloseTicket: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = "Back",
                tint = AppColors.TextDark
            )
        }

        Column(Modifier.weight(1f)) {
            Text(
                clientName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextDark
            )
            Text(subject, fontSize = 12.sp, color = AppColors.TextMuted)
        }

        IconButton(onClick = onCloseTicket) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Close ticket",
                tint = AppColors.Delivered
            )
        }
    }
}

@Composable
private fun ChatBubble(message: TicketMessage) {
    val isAdmin = message.senderType == "admin"//checks if the message is from the admin

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAdmin) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isAdmin) AppColors.Terracotta else AppColors.Blush,
                    RoundedCornerShape(16.dp),
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = message.message,
                color = if (isAdmin) AppColors.Surface else AppColors.TextDark,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()//keeps the keyboard open,the input bar slides up instead of disappearing
            //ime: it adds padding when the on-screen keyboard (IME) appears.
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message…", color = AppColors.TextMuted) },
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Terracotta,
                unfocusedBorderColor = AppColors.Outline,
                focusedContainerColor = AppColors.Surface,
                unfocusedContainerColor = AppColors.Surface,
            ),
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            modifier = Modifier
                .height(48.dp)
                .background(AppColors.Terracotta, RoundedCornerShape(24.dp)),
        ) {
            Icon(
                Icons.AutoMirrored.Default.Send,
                contentDescription = "Send",
                tint = AppColors.Surface
            )
        }
    }
}