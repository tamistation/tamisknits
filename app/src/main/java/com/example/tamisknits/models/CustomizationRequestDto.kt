package com.example.tamisknits.models


data class CustomizationRequest(
    val requestId: String = "",
    val clientId: String = "",
    val adminId: String = "",
    val productId: String = "",
    val details: String = "",
    val colorPreference: String = "",
    val sizePreference: String = "",
    val status: String = "pending"
)
