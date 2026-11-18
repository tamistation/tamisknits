package com.example.tamisknits.features.client.common

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ClientFormat {
    private val currency = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    fun price(value: Double): String = "$${currency.format(value)}"

    fun priceRange(min: Double, max: Double): String =
        if (min == max) price(min) else "${price(min)} – ${price(max)}"

    fun date(millis: Long): String =
        if (millis <= 0L) "Just now" else SimpleDateFormat(
            "d MMM yyyy",
            Locale.getDefault()
        ).format(Date(millis))

    fun dateTime(millis: Long): String =
        if (millis <= 0L) "Just now" else SimpleDateFormat(
            "d MMM yyyy, HH:mm",
            Locale.getDefault()
        ).format(Date(millis))

    fun dayRange(start: Long, end: Long): String {
        val format = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
        return "${format.format(Date(start))} – ${format.format(Date(end))}"
    }

    fun time(millis: Long): String =
        if (millis <= 0L) "" else SimpleDateFormat(
            "HH:mm",
            Locale.getDefault()
        ).format(Date(millis))
}
