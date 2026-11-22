package com.example.tamisknits.features.authentication.components

import android.util.Patterns

internal const val MIN_PASSWORD_LENGTH = 6

internal object AuthValidation {
    fun email(value: String): String? = when {
        value.isBlank() -> "Enter your email"
        !Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches() -> "That email doesn't look right"
        else -> null
    }

    fun requiredPassword(value: String): String? =
        if (value.isBlank()) "Enter your password" else null

    fun newPassword(value: String): String? = when {
        value.isBlank() -> "Enter a password"
        value.length < MIN_PASSWORD_LENGTH -> "Use at least $MIN_PASSWORD_LENGTH characters"
        else -> null
    }

    fun confirmPassword(password: String, confirm: String): String? = when {
        confirm.isBlank() -> "Confirm your password"
        confirm != password -> "Passwords don't match"
        else -> null
    }

    fun name(value: String): String? = if (value.isBlank()) "Enter your name" else null

    fun phone(value: String): String? = when {
        value.isBlank() -> "Enter your phone number"
        value.count { it.isDigit() } < 7 -> "Enter a valid phone number"
        else -> null
    }
}
