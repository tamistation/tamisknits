package com.example.tamisknits.models

data class HomeSections(
    val featured: List<ClientProduct> = emptyList(),
    val newArrivals: List<ClientProduct> = emptyList(),
    val bestSellers: List<ClientProduct> = emptyList(),
    val recentlyViewed: List<ClientProduct> = emptyList(),
    val categories: List<String> = emptyList()
)
