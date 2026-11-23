package com.example.tamisknits.features.delivery.details

import com.example.tamisknits.models.DeliveryOrder
import com.example.tamisknits.models.OrderStatuses
import com.example.tamisknits.models.OrderStep
import com.example.tamisknits.models.PaymentMethods
import com.example.tamisknits.models.SavedAddress

enum class IssueReason(val label: String) {
    NOT_ANSWERING("Customer not answering"),
    WRONG_ADDRESS("Wrong address"),
    REFUSED("Customer refused"),
    OTHER("Other")
}

data class DeliveryDetailsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val notFound: Boolean = false,
    val order: DeliveryOrder? = null,
    val steps: List<OrderStep> = emptyList(),
    // Label of the next driver action ("Picked up" / "Start delivery" / "Mark delivered"), null when done.
    val actionLabel: String? = null,
    val isAdvancing: Boolean = false,
    // Confirm delivery sheet
    val showConfirmSheet: Boolean = false,
    val cashConfirmed: Boolean = false,
    // Report problem sheet
    val showReportSheet: Boolean = false,
    val issueReason: IssueReason? = null,
    val issueOtherText: String = "",
    val issueError: String? = null,
    val isReporting: Boolean = false,
    val message: String? = null
) {
    val address: SavedAddress
        get() = SavedAddress.fromShippingAddress(order?.order?.order?.shippingAddress.orEmpty())

    val isCashOnDelivery: Boolean
        get() = order?.order?.paymentMethod == PaymentMethods.CASH_ON_DELIVERY

    val total: Double
        get() = order?.order?.order?.totalPrice ?: 0.0

    val isDelivered: Boolean
        get() = order?.status == OrderStatuses.DELIVERED

    val canConfirmDelivery: Boolean
        get() = !isAdvancing && (!isCashOnDelivery || cashConfirmed)
}
