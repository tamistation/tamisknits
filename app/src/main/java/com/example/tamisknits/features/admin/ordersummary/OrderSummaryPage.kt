package com.example.tamisknits.features.admin.ordersummary



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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tamisknits.models.OrderItem
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.ui.components.PageHeader
import com.example.tamisknits.ui.components.StatusPill

@Composable
fun OrderSummaryPage(
    orderId: String,
    viewModel: OrderSummaryViewModel,
    onBackClick: () -> Unit
) {
    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    val uiState by viewModel.uiState.collectAsState()

    OrderSummaryUI(
        uiState = uiState,
        onBackClick = onBackClick
    )
}

@Composable
private fun OrderSummaryUI(
    uiState: OrderSummaryState,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Cream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.TextDark
                    )
                }
                PageHeader(title = "Order Summary", subtitle = "Full order details")
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.Terracotta)
                    }
                }

                uiState.errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.errorMessage, color = AppColors.TextMuted, fontSize = 15.sp)
                    }
                }
                uiState.order != null -> {
                    OrderSummaryContent(uiState = uiState)
                }
            }
        }
    }
}
@Composable
private fun OrderSummaryContent(uiState: OrderSummaryState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Order #${uiState.order!!.orderId.take(8).uppercase()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppColors.TextDark
                        )
                        val (label, color) = summaryStatusPillStyle(uiState.order!!.status)
                        StatusPill(label = label, color = color)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (uiState.order!!.isCustomOrder) "✦ Custom order" else "Standard order",
                        fontSize = 13.sp,
                        color = if (uiState.order!!.isCustomOrder) AppColors.Terracotta else AppColors.TextMuted
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    SummaryRow("Client", uiState.order!!.clientId)
                    HorizontalDivider(
                        Modifier.padding(vertical = 8.dp),
                        color = AppColors.DividerLightTwo
                    )
                    SummaryRow("Total", "$${"%.2f".format(uiState.total)}")
                    HorizontalDivider(
                        Modifier.padding(vertical = 8.dp),
                        color = AppColors.DividerLightTwo
                    )
                    SummaryRow("City", uiState.order!!.shippingAddress["city"] ?: "—")
                }
            }
        }

        item {
            Text(
                "Items (${uiState.items.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AppColors.TextDark
            )
        }

        items(uiState.items) { orderItem ->
            OrderItemRow(orderItem)
        }


        item {
            OrderBreakdownCard(
                subtotal = uiState.subtotal,
                deliveryFee = uiState.deliveryFee,
                discountPercentage = uiState.discountPercentage,
                discountAmount = uiState.discountAmount,
                total = uiState.total
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun OrderBreakdownCard(
    subtotal: Double,
    deliveryFee: Double,
    discountPercentage: Double,
    discountAmount: Double,
    total: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            SummaryRow("Subtotal", "$${"%.2f".format(subtotal)}")
            Spacer(Modifier.height(8.dp))
            SummaryRow("Delivery fee", "$${"%.2f".format(deliveryFee)}")
            Spacer(Modifier.height(8.dp))
            SummaryRow(
                "Discount (${discountPercentage.toInt()}%)",
                "-$${"%.2f".format(discountAmount)}"
            )
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = AppColors.DividerLightTwo)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AppColors.TextDark
                )
                Text(
                    "$${"%.2f".format(total)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AppColors.Terracotta
                )
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.DragBackground)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = AppColors.TextDark
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = listOfNotNull(
                        item.size.takeIf { it.isNotBlank() },
                        item.color.takeIf { it.isNotBlank() }
                    ).joinToString(" • "),
                    fontSize = 12.sp,
                    color = AppColors.TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${item.price}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = AppColors.TextDark
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Qty ${item.quantity}",
                    fontSize = 12.sp,
                    color = AppColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = AppColors.TextMuted)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextDark)
    }
}

private fun summaryStatusPillStyle(status: String): Pair<String, Color> {
    val color = when (status) {
        "pending" -> AppColors.Pending
        "confirmed", "shipped", "in_transit" -> AppColors.Active
        "delivered" -> AppColors.Delivered
        "cancelled" -> AppColors.Cancelled
        else -> AppColors.UnknownStatus
    }
    val label = status.replace("_", " ").replaceFirstChar { it.uppercase() }
    return label to color
}