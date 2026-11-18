package com.example.tamisknits.features.client.support.create

enum class TicketTopic(val label: String) {
    ORDER("Order issue"),
    DELIVERY("Delivery"),
    CUSTOM("Custom request"),
    PRODUCT("Product question"),
    OTHER("Something else")
}

enum class CreateTicketField { SUBJECT, BODY }

data class CreateTicketUiState(
    val topic: TicketTopic? = null,
    val subject: String = "",
    val body: String = "",
    val fieldErrors: Map<CreateTicketField, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val createdTicketId: String? = null,
    val message: String? = null
)
