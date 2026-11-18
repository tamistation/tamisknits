package com.example.tamisknits.models

data class SavedAddress(
    val addressId: String = "",
    val label: String = "Home",
    val fullName: String = "",
    val phone: String = "",
    val street: String = "",
    val building: String = "",
    val city: String = "",
    val notes: String = "",
    val isDefault: Boolean = false
) {
    val singleLine: String
        get() = listOf(building, street, city)
            .filter { it.isNotBlank() }
            .joinToString(", ")

    val isComplete: Boolean
        get() = fullName.isNotBlank() && phone.isNotBlank() &&
                street.isNotBlank() && city.isNotBlank()

    // "city" is the key the admin order screens read.
    fun toShippingAddress(): Map<String, String> = mapOf(
        "label" to label,
        "fullName" to fullName,
        "phone" to phone,
        "street" to street,
        "building" to building,
        "city" to city,
        "notes" to notes
    )

    companion object {
        fun fromShippingAddress(map: Map<String, String>) = SavedAddress(
            label = map["label"].orEmpty(),
            fullName = map["fullName"].orEmpty(),
            phone = map["phone"].orEmpty(),
            street = map["street"].orEmpty(),
            building = map["building"].orEmpty(),
            city = map["city"].orEmpty(),
            notes = map["notes"].orEmpty()
        )
    }
}
