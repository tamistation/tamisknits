package com.example.tamisknits.features.delivery.deliveries

import com.example.tamisknits.models.DeliveryOrder

enum class DeliveryTab(val title: String) {
    TO_PICK_UP("To pick up"),
    ON_THE_WAY("On the way"),
    DONE("Done")
}

data class DeliveriesUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val driverName: String = "",
    val selectedTab: DeliveryTab = DeliveryTab.TO_PICK_UP,
    // Already split by tab in the ViewModel, from the single listenToMyOrders stream.
    val toPickUp: List<DeliveryOrder> = emptyList(),
    val onTheWay: List<DeliveryOrder> = emptyList(),
    val done: List<DeliveryOrder> = emptyList()
) {
    // Every assignment that still needs the driver; orders have no scheduled day to narrow by.
    val toDeliverCount: Int get() = toPickUp.size + onTheWay.size

    val visibleOrders: List<DeliveryOrder>
        get() = when (selectedTab) {
            DeliveryTab.TO_PICK_UP -> toPickUp
            DeliveryTab.ON_THE_WAY -> onTheWay
            DeliveryTab.DONE -> done
        }
}
