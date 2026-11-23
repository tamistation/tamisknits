package com.example.tamisknits.models


object OrderStatuses {
    const val PENDING = "pending"
    const val CONFIRMED = "confirmed"
    const val SHIPPED = "shipped"
    const val IN_TRANSIT = "in_transit"
    const val CANCELLED = "cancelled"
    const val DELIVERED = "delivered"

    fun label(status: String): String =
        status.replace('_', ' ').replaceFirstChar { it.uppercase() }
}

enum class ClientOrderTab(val title: String) {
    ACTIVE("Active"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class StepState { COMPLETED, CURRENT, UPCOMING }

data class OrderStep(
    val status: String,
    val label: String,
    val state: StepState
)

fun OrderStatusConfig.tabFor(status: String): ClientOrderTab = when {
    status == OrderStatuses.CANCELLED -> ClientOrderTab.CANCELLED
    status in previous -> ClientOrderTab.COMPLETED
    else -> ClientOrderTab.ACTIVE
}

// Same rule the admin uses for its "pending" tab: once an order is confirmed, the client can't cancel it.
fun OrderStatusConfig.canClientCancel(status: String): Boolean = status in pending

fun OrderStatusConfig.stepsFor(status: String): List<OrderStep> {
    val currentIndex = flow.indexOf(status)
    return flow.mapIndexed { index, step ->
        val state = when {
            currentIndex == -1 -> StepState.UPCOMING
            index < currentIndex -> StepState.COMPLETED
            index == currentIndex && index == flow.lastIndex -> StepState.COMPLETED
            index == currentIndex -> StepState.CURRENT
            else -> StepState.UPCOMING
        }
        OrderStep(status = step, label = OrderStatuses.label(step), state = state)
    }
}

fun OrderStatusConfig.progressFor(status: String): Float {
    val index = flow.indexOf(status)
    if (index < 0 || flow.size < 2) return 0f
    return index.toFloat() / flow.lastIndex
}
