package com.example.tamisknits.models

data class Delivery(
    val deliveryId: String = "",
    val orderId: String = "",
    val clientId: String = "",
    val deliveryPersonId: String = "",
    val address: String = "",
    val status: String = "pending"
)