package com.example.tamisknits.models


data class UserType(
    val usertypeId: String = "",
    val type: String = "",
    val permissionIds: List<String> = emptyList()
)
