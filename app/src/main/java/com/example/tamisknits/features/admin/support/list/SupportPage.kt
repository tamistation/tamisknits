package com.example.tamisknits.features.admin.support.list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.models.SupportTickets
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.ui.components.AppSearchBar
import com.example.tamisknits.ui.components.DragToAdvanceRow
import com.example.tamisknits.ui.components.EmptyState
import com.example.tamisknits.ui.components.PageHeader
import com.example.tamisknits.ui.components.StatusPill
import com.example.tamisknits.ui.components.TabsWithPendingBadge
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.Snackbar
import androidx.compose.ui.draw.scale

@Composable
fun SupportPage(
    viewModel: TicketManagementViewModel,
    onOpenTicket: (ticketId: String, clientName: String, subject: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val tickets by viewModel.filteredTickets.collectAsState()
    val clientNames by viewModel.clientNames.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()

    SupportUI(
        uiState = uiState,
        tickets = tickets,
        clientNames = clientNames,
        pendingCount = pendingCount,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onTabSelected = viewModel::onTabSelected,
        onAdvance = { ticketId, status -> viewModel.advanceStatus(ticketId, status) },
        onGoBack = { ticketId, status -> viewModel.goBackStatus(ticketId, status) },
        onDragStart = viewModel::onDragStart,
        onDragEnd = viewModel::onDragEnd,
        onOpenTicket = onOpenTicket,
        onCloseTicket = viewModel::closeTicket,

        )
}

@Composable
private fun SupportUI(
    uiState: TicketManagementUiState,
    tickets: List<SupportTickets>,
    clientNames: Map<String, String>,
    pendingCount: Int,
    onSearchQueryChange: (String) -> Unit,
    onTabSelected: (TicketTab) -> Unit,
    onAdvance: (String, String) -> Unit,
    onGoBack: (String, String) -> Unit,
    onDragStart: (String) -> Unit,
    onDragEnd: () -> Unit,
    onOpenTicket: (ticketId: String, clientName: String, subject: String) -> Unit,
    onCloseTicket: (String) -> Unit,
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val previousStatuses = remember { mutableMapOf<String, String>() }

    Box(Modifier.background(AppColors.Cream)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 24.dp),
        ) {
            PageHeader(title = "Support", subtitle = "Help clients with their questions")

            AppSearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "Search by client or subject…",
            )

            TabsWithPendingBadge(
                tabs = listOf(TicketTab.PENDING, TicketTab.ACTIVE, TicketTab.DONE),
                labels = listOf("Pending", "Active", "Done"),
                selectedTab = uiState.selectedTab,
                onTabSelected = onTabSelected,
                pendingTab = TicketTab.PENDING,
                pendingCount = pendingCount,
            )

            HorizontalDivider(color = AppColors.DividerLight)

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Terracotta)
                }
            } else if (tickets.isEmpty()) {
                val (emoji, message) = when (uiState.selectedTab) {
                    TicketTab.PENDING -> "📭" to "No pending tickets"
                    TicketTab.ACTIVE -> "💬" to "No active tickets"
                    TicketTab.DONE -> "✅" to "No closed tickets yet"
                }
                EmptyState(emoji = emoji, message = message)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { Spacer(Modifier.height(8.dp)) }
                    items(tickets, key = { it.ticketId }) { ticket ->

                        LaunchedEffect(ticket.ticketId, ticket.status) {
                            val prev = previousStatuses[ticket.ticketId]
                            if (prev != null && prev != ticket.status) {
                                val label = ticket.status.replaceFirstChar { it.uppercase() }
                                scope.launch {
                                    snackbarHostState.showSnackbar("Ticket moved to $label")
                                }
                            }
                            previousStatuses[ticket.ticketId] = ticket.status
                        }
                        val currentIndex = ticketStatusFlow.indexOf(ticket.status)
                        TicketCard(
                            ticket = ticket,
                            clientName = clientNames[ticket.clientId] ?: "Loading…",
                            onAdvance = {
                                val nextStatus = ticketStatusFlow.getOrNull(currentIndex + 1)
                                if (nextStatus != null) {
                                    val newTab = statusToTab(nextStatus)
                                    if (newTab != uiState.selectedTab) {
                                        onTabSelected(newTab)
                                    }
                                }
                                onAdvance(ticket.ticketId, ticket.status)
                            },
                            isDragging = uiState.draggingTicketId == ticket.ticketId,
                            onDragStart = { onDragStart(ticket.ticketId) },
                            onDragEnd = { onDragEnd() },
                            onGoBack = {
                                val prevStatus = ticketStatusFlow.getOrNull(currentIndex - 1)
                                if (prevStatus != null) {
                                    val newTab = statusToTab(prevStatus)
                                    if (newTab != uiState.selectedTab) {
                                        onTabSelected(newTab)
                                    }
                                }
                                onGoBack(ticket.ticketId, ticket.status)
                            },
                            onOpenChat = {
                                onOpenTicket(
                                    ticket.ticketId,
                                    clientNames[ticket.clientId] ?: "Client",
                                    ticket.subject,
                                )
                            },
                            onCloseTicket = { onCloseTicket(ticket.ticketId) },
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }

                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            Snackbar(
                containerColor = AppColors.Terracotta,
                contentColor = Color.White
            ) {
                Text(data.visuals.message)
            }
        }
    }
}

@Composable
private fun TicketCard(
    ticket: SupportTickets,
    clientName: String,
    onAdvance: () -> Unit,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onGoBack: () -> Unit,
    onOpenChat: () -> Unit,
    onCloseTicket: () -> Unit,
) {
    val scale by animateFloatAsState(if (isDragging) 1.03f else 1f, label = "scale")
    val cardElevation by animateFloatAsState(if (isDragging) 8f else 2f, label = "elevation")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(cardElevation.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = clientName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AppColors.TextDark,
                    )
                    Text(
                        text = ticket.subject,
                        fontSize = 12.sp,
                        color = AppColors.TextMuted,
                    )
                }
                val (label, color) = ticketStatusPillStyle(ticket.status)
                StatusPill(label = label, color = color)
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = AppColors.DividerLightTwo)
            Spacer(Modifier.height(10.dp))

            Text(
                text = ticket.message,
                fontSize = 13.sp,
                color = AppColors.TextMuted,
                maxLines = 2,
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DragToAdvanceRow(
                    itemKey = ticket.ticketId,
                    onAdvance = onAdvance,
                    onGoBack = onGoBack,
                    onDragStart = onDragStart,
                    onDragEnd = onDragEnd,
                )

                Row {
                    IconButton(
                        onClick = onOpenChat,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AppColors.DragBackground),
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "Open chat",
                            tint = AppColors.Terracotta,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    if (ticket.status != "done") {
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = onCloseTicket,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(AppColors.CancelButtonBackground),
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Close ticket",
                                tint = AppColors.Delivered,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private val ticketStatusFlow = listOf("pending", "active", "done")

private fun statusToTab(status: String): TicketTab = when (status) {
    "pending", "open" -> TicketTab.PENDING
    "active" -> TicketTab.ACTIVE
    "done" -> TicketTab.DONE
    else -> TicketTab.PENDING
}

private fun ticketStatusPillStyle(status: String): Pair<String, Color> {//checks tickets status for color
    val color = when (status) {
        "pending", "open" -> AppColors.Pending
        "active" -> AppColors.Active
        "done" -> AppColors.Delivered
        else -> AppColors.UnknownStatus
    }
    val label = status.replaceFirstChar { it.uppercase() }
    return label to color
}