package com.example.tamisknits.features.client.ui.image

import android.annotation.SuppressLint
import android.content.Context
import com.example.tamisknits.models.ClientProduct
import com.example.tamisknits.models.ProductVariant
import com.example.tamisknits.models.Products
import java.util.concurrent.ConcurrentHashMap

/**
 * Turns an image string into something Coil can load:
 *  - "https://..." stays a URL
 *  - a drawable name ("rosie_tote", "rosie_tote.jpg", "res/drawable/rosie_tote.png") becomes its resource id
 *  - otherwise the bundled naming convention is tried: product_<name>, product_<name>_<color>, product_<name>_2..6
 *  - content:// and file:// values are only readable on the device that saved them, so they're tried last
 * Returns null when nothing usable exists, and the caller shows the placeholder.
 */
object ClientImageResolver {

    private val resourceIds = ConcurrentHashMap<String, Int>()
    private const val MAX_EXTRA_IMAGES = 6

    fun resolve(context: Context, value: String, fallbackNames: List<String> = emptyList()): Any? {
        val trimmed = value.trim()
        if (isWebUrl(trimmed)) return trimmed
        drawableId(context, trimmed)?.let { return it }
        fallbackNames.firstNotNullOfOrNull { drawableId(context, it) }?.let { return it }
        if (trimmed.startsWith("content://") || trimmed.startsWith("file://")) return trimmed
        return null
    }

    fun productImage(context: Context, product: Products): Any? =
        resolve(context, product.imageUrl, listOf(productKey(product.name)))

    fun variantImage(context: Context, product: Products, variant: ProductVariant?): Any? {
        if (variant == null) return productImage(context, product)
        val colorKey = "${productKey(product.name)}_${slug(variant.color)}"
        return resolve(context, variant.imageUrl, listOf(colorKey)) ?: productImage(
            context,
            product
        )
    }

    fun gallery(context: Context, product: ClientProduct): List<Any> {
        val key = productKey(product.name)
        val main = productImage(context, product.product)
        val extras = product.images.mapNotNull { resolve(context, it) }
        val numbered = (2..MAX_EXTRA_IMAGES).mapNotNull { drawableId(context, "${key}_$it") }
        val variants = product.variants.mapNotNull { variant ->
            resolve(context, variant.imageUrl, listOf("${key}_${slug(variant.color)}"))
        }
        return (listOfNotNull(main) + extras + numbered + variants).distinct()
    }

    fun productKey(productName: String): String = "product_${slug(productName)}"

    fun slug(text: String): String = text.trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')

    private fun isWebUrl(value: String) =
        value.startsWith("https://", ignoreCase = true) || value.startsWith(
            "http://",
            ignoreCase = true
        )

    private fun toResourceName(raw: String): String = raw.trim()
        .substringAfterLast('/')
        .substringBeforeLast('.')
        .lowercase()
        .replace(Regex("[^a-z0-9_]+"), "_")
        .trim('_')

    // Image names come from Firestore at runtime, so a lookup by name is the only option here.
    @SuppressLint("DiscouragedApi")
    private fun drawableId(context: Context, raw: String): Int? {
        val name = toResourceName(raw)
        if (name.isEmpty() || !name.first().isLetter()) return null
        val id = resourceIds.getOrPut(name) {
            context.resources.getIdentifier(name, "drawable", context.packageName)
        }
        return id.takeIf { it != 0 }
    }
}
