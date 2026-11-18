package com.example.tamisknits.features.client.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.tamisknits.features.client.ui.image.ClientImageResolver
import com.example.tamisknits.models.ProductVariant
import com.example.tamisknits.models.Products
import com.example.tamisknits.ui.components.ProductImagePlaceholder
import com.example.tamisknits.ui.components.ShimmerBox

@Composable
fun ClientImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderIconSize: Dp = 40.dp
) {
    if (model == null) {
        ProductImagePlaceholder(modifier, placeholderIconSize)
        return
    }

    val context = LocalContext.current
    val request = remember(model) {
        ImageRequest.Builder(context)
            .data(model)
            .crossfade(300)
            .build()
    }
    var state by remember(model) {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }

    Box(modifier) {
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            contentScale = contentScale,
            onState = { state = it },
            modifier = Modifier.matchParentSize()
        )
        when (state) {
            is AsyncImagePainter.State.Empty,
            is AsyncImagePainter.State.Loading -> ShimmerBox(
                modifier = Modifier.matchParentSize(),
                shape = RectangleShape
            )

            is AsyncImagePainter.State.Error -> ProductImagePlaceholder(
                modifier = Modifier.matchParentSize(),
                iconSize = placeholderIconSize
            )

            is AsyncImagePainter.State.Success -> Unit
        }
    }
}

@Composable
fun ProductImage(
    product: Products,
    modifier: Modifier = Modifier,
    variant: ProductVariant? = null,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderIconSize: Dp = 40.dp
) {
    val context = LocalContext.current
    val model = remember(product.productId, product.imageUrl, product.name, variant?.variantId) {
        if (variant != null) ClientImageResolver.variantImage(context, product, variant)
        else ClientImageResolver.productImage(context, product)
    }
    ClientImage(
        model = model,
        contentDescription = product.name,
        modifier = modifier,
        contentScale = contentScale,
        placeholderIconSize = placeholderIconSize
    )
}
