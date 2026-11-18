package com.example.tamisknits.features.client.support.list

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.common.ClientFormat
import com.example.tamisknits.features.client.ui.components.ProductRowSkeleton
import com.example.tamisknits.models.ClientTicket
import com.example.tamisknits.models.TicketStatuses
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppCard
import com.example.tamisknits.ui.components.ContentState
import com.example.tamisknits.ui.components.EmptyView
import com.example.tamisknits.ui.components.ErrorView
import com.example.tamisknits.ui.components.OfflineBanner
import com.example.tamisknits.ui.components.ScreenTopBar
import com.example.tamisknits.ui.components.StatusPill
import com.example.tamisknits.ui.components.contentStateOf
import com.example.tamisknits.ui.components.readableWidth

@Composable
fun SupportTicketsPage(
    viewModel: SupportTicketsViewModel,
    onBack: () -> Unit,
    onTicketClick: (String) -> Unit,
    onNewTicket: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            ScreenTopBar(
                title = "Support",
                subtitle = "We usually reply within a day",
                onBack = onBack
            )
        },
        floatingActionButton = {
            if (!uiState.isEmpty) {
                ExtendedFloatingActionButton(
                    onClick = onNewTicket,
                    containerColor = AppColors.Terracotta,
                    contentColor = AppColors.OnTerracotta,
                    shape = AppShapes.Pill,
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("New ticket", style = AppTextStyles.Button) }
                )
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OfflineBanner(uiState.isOffline)
            Crossfade(
                targetState = contentStateOf(
                    uiState.isLoading,
                    uiState.errorMessage,
                    uiState.isEmpty
                ),
                label = "ticketsState"
            ) { state ->
                when (state) {
                    ContentState.LOADING -> Column(Modifier.padding(AppDimens.ScreenPadding)) {
                        repeat(3) { ProductRowSkeleton(Modifier.padding(vertical = 8.dp)) }
                    }

                    ContentState.ERROR -> ErrorView(
                        uiState.errorMessage.orEmpty(),
                        viewModel::retry
                    )

                    ContentState.EMPTY -> EmptyView(
                        icon = Icons.Rounded.HeadsetMic,
                        title = "How can we help?",
                        message = "Questions about an order, a custom piece or anything else? Start a chat with us.",
                        actionLabel = "Contact support",
                        onAction = onNewTicket
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
                        if (uiState.openTickets.isNotEmpty()) {
                            item(key = "openHeader") { GroupHeader("Open conversations") }
                            items(uiState.openTickets, key = { it.ticketId }) { ticket ->
                                TicketCard(
                                    ticket,
                                    { onTicketClick(ticket.ticketId) },
                                    Modifier.readableWidth()
                                )
                            }
                        }
                        if (uiState.closedTickets.isNotEmpty()) {
                            item(key = "closedHeader") { GroupHeader("Resolved") }
                            items(uiState.closedTickets, key = { it.ticketId }) { ticket ->
                                TicketCard(
                                    ticket,
                                    { onTicketClick(ticket.ticketId) },
                                    Modifier.readableWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(title: String) {
    Text(title.uppercase(), style = AppTextStyles.Overline, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun TicketCard(ticket: ClientTicket, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (ticket.isClosed) AppColors.SurfaceMuted else AppColors.Blush),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Chat,
                    contentDescription = null,
                    tint = if (ticket.isClosed) AppColors.TextMuted else AppColors.Terracotta,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    ticket.ticket.subject.ifBlank { "Support request" },
                    style = AppTextStyles.Label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    ticket.ticket.message,
                    style = AppTextStyles.BodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(
                        TicketStatuses.label(ticket.ticket.status),
                        AppColors.requestStatusColor(ticket.ticket.status)
                    )
                    Spacer(Modifier.weight(1f))
                    Text(ClientFormat.date(ticket.createdAt), style = AppTextStyles.Caption)
                }
            }
        }
    }
}
