package com.example.tamisknits.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "client",
)
