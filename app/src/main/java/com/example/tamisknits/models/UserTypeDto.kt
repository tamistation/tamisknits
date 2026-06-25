package com.example.tamisknits.models


data class UserType(
    val usertypeiid: String = "",
    val uid: String = "",
    val type: String = "",
    val permissions: List<String> = emptyList()
)