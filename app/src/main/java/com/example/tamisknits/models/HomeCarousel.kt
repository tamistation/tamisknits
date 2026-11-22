package com.example.tamisknits.models

// Where the admin-picked home carousel lives in Firestore: one document holding an ordered
// list of product ids. The list order is the order clients swipe through.
object HomeCarousel {
    const val COLLECTION = "AppSettings"
    const val DOCUMENT = "homeCarousel"
    const val FIELD_PRODUCT_IDS = "productIds"
    const val MAX_PRODUCTS = 10
}
