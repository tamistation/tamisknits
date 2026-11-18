package com.example.tamisknits.models

object AvatarOptions {
    const val INITIALS = "initials"
    const val YARN = "yarn"
    const val BAG = "bag"
    const val FLOWER = "flower"
    const val HEART = "heart"
    const val STAR = "star"

    val all = listOf(INITIALS, YARN, BAG, FLOWER, HEART, STAR)

    fun initials(name: String): String = name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }
}
