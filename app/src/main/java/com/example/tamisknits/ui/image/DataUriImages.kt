package com.example.tamisknits.ui.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Stores product photos inside Firestore as "data:image/jpeg;base64,..." strings, so the demo works
 * without Firebase Storage. Photos are shrunk hard (longest side 640px, ~90 KB) because a Firestore
 * document is capped at 1 MB and order items copy the image string too.
 */
object DataUriImages {

    private const val PREFIX = "data:image/jpeg;base64,"
    private const val MAX_EDGE = 640
    private const val TARGET_BYTES = 90_000

    // Decoded bytes, keyed by the string itself, so lists don't re-decode base64 on every scroll.
    private val decoded = object : LruCache<String, ByteArray>(8 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ByteArray) = value.size
    }

    fun isDataUri(value: String): Boolean = value.startsWith("data:image", ignoreCase = true)

    /** Bytes Coil can load directly, or null when the value isn't a valid data URI. */
    fun toBytes(value: String): ByteArray? {
        if (!isDataUri(value)) return null
        decoded.get(value)?.let { return it }
        val base64 = value.substringAfter(",", missingDelimiterValue = "")
        val bytes = runCatching { Base64.decode(base64, Base64.DEFAULT) }.getOrNull() ?: return null
        decoded.put(value, bytes)
        return bytes
    }

    /** Reads the picked image, fixes its rotation, shrinks it and returns a data URI (null on failure). */
    suspend fun fromUri(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

            // Power-of-two pre-scale while decoding keeps memory low for big camera photos.
            var sample = 1
            while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_EDGE) sample *= 2
            val raw = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(
                    it,
                    null,
                    BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return@runCatching null

            val rotation = resolver.openInputStream(uri)?.use { stream ->
                when (ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f

            val scale = MAX_EDGE.toFloat() / max(raw.width, raw.height)
            val matrix = Matrix().apply {
                if (scale < 1f) postScale(scale, scale)
                if (rotation != 0f) postRotate(rotation)
            }
            val bitmap = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)

            var quality = 80
            var bytes: ByteArray
            do {
                bytes = ByteArrayOutputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    out.toByteArray()
                }
                quality -= 10
            } while (bytes.size > TARGET_BYTES && quality >= 40)

            PREFIX + Base64.encodeToString(bytes, Base64.NO_WRAP)
        }.getOrNull()
    }
}
