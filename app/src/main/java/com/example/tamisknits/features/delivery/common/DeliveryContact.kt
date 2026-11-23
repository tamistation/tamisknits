package com.example.tamisknits.features.delivery.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.tamisknits.models.SavedAddress

// Opening the dialer, WhatsApp and maps for a delivery's customer.
object DeliveryContact {

    private const val LEBANON_CODE = "961"

    // WhatsApp needs the full international number without "+" or separators.
    // Lebanese numbers are often typed locally ("03 123 456" / "71123456"), so those get 961 prefixed.
    fun internationalDigits(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return when {
            digits.isEmpty() -> ""
            phone.trim().startsWith("+") -> digits
            digits.startsWith("00") -> digits.drop(2)
            digits.startsWith(LEBANON_CODE) && digits.length > 8 -> digits
            digits.startsWith("0") -> LEBANON_CODE + digits.drop(1)
            digits.length == 8 -> LEBANON_CODE + digits
            else -> digits
        }
    }

    fun dial(context: Context, phone: String): Boolean =
        context.launch(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phone.trim())}")))

    fun whatsApp(context: Context, phone: String): Boolean {
        val digits = internationalDigits(phone)
        if (digits.isEmpty()) return false
        return context.launch(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits")))
    }

    fun mapsQuery(address: SavedAddress): String =
        listOf(address.building, address.street, address.city)
            .filter { it.isNotBlank() }
            .joinToString(", ")

    // Prefers an installed maps app (geo:), falling back to Google Maps in the browser.
    fun openInMaps(context: Context, address: SavedAddress): Boolean {
        val query = Uri.encode(mapsQuery(address))
        if (query.isBlank()) return false
        return context.launch(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$query"))) ||
                context.launch(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")
                    )
                )
    }

    private fun Context.launch(intent: Intent): Boolean = try {
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
