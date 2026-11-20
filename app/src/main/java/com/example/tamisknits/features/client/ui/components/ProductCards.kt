package com.example.tamisknits.features.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.common.ClientFormat
import com.example.tamisknits.models.ClientProduct
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppCard
import com.example.tamisknits.ui.components.ShimmerBox
import com.example.tamisknits.ui.components.pressScale
import java.util.concurrent.TimeUnit

private const val NEW_ARRIVAL_DAYS = 14L

private fun ClientProduct.isNewArrival(): Boolean =
    createdAt > 0 && System.currentTimeMillis() - createdAt < TimeUnit.DAYS.toMillis(
        NEW_ARRIVAL_DAYS
    )

@Composable
fun ProductCard(
    product: ClientProduct,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    imageAspectRatio: Float = 0.8f
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .pressScale(interactionSource, 0.97f)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(imageAspectRatio)
                .clip(AppShapes.Image)
        ) {
            ProductImage(product = product.product, modifier = Modifier.matchParentSize())
            ProductBadge(product, Modifier
                .align(Alignment.TopStart)
                .padding(10.dp))
            FavoriteButton(
                isFavorite = isFavorite,
                onToggle = onToggleFavorite,
                buttonSize = 34.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        if (product.category.isNotBlank()) {
            Text(product.category.uppercase(), style = AppTextStyles.Overline, maxLines = 1)
            Spacer(Modifier.height(2.dp))
        }
        Text(
            text = product.name,
            style = AppTextStyles.Label,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = ClientFormat.price(product.minPrice),
                style = AppTextStyles.Price,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.weight(1f))
            ColorDots(product.colors)
        }
    }
}

@Composable
fun ProductListRow(
    product: ClientProduct,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(AppShapes.CardSmall)
            ) {
                ProductImage(
                    product = product.product,
                    modifier = Modifier.matchParentSize(),
                    placeholderIconSize = 28.dp
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (product.category.isNotBlank()) {
                    Text(product.category.uppercase(), style = AppTextStyles.Overline, maxLines = 1)
                }
                Text(
                    product.name,
                    style = AppTextStyles.Subtitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    ClientFormat.priceRange(product.minPrice, product.maxPrice),
                    style = AppTextStyles.Price
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StockLabel(product)
                    Spacer(Modifier.width(8.dp))
                    ColorDots(product.colors)
                }
            }
            FavoriteButton(
                isFavorite = isFavorite,
                onToggle = onToggleFavorite,
                buttonSize = 36.dp,
                background = AppColors.SurfaceMuted,
                modifier = Modifier.align(Alignment.Top)
            )
        }
    }
}

@Composable
fun ProductBadge(product: ClientProduct, modifier: Modifier = Modifier) {
    val (label, color) = when {
        !product.isInStock -> "Sold out" to AppColors.TextDark
        product.isLowStock -> "Only ${product.totalStock} left" to AppColors.Terracotta
        product.isNewArrival() -> "New" to AppColors.MarigoldGold
        else -> return
    }
    Text(
        text = label,
        style = AppTextStyles.Caption.copy(color = AppColors.Surface),
        modifier = modifier
            .clip(AppShapes.Pill)
            .background(color.copy(alpha = 0.88f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun StockLabel(product: ClientProduct) {
    val (label, color) = when {
        !product.isInStock -> "Sold out" to AppColors.Cancelled
        product.isLowStock -> "Only ${product.totalStock} left" to AppColors.Pending
        else -> "In stock" to AppColors.Delivered
    }
    Text(label, style = AppTextStyles.Caption.copy(color = color))
}

@Composable
fun ColorDots(colors: List<String>, modifier: Modifier = Modifier, maxDots: Int = 4) {
    if (colors.isEmpty()) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy((-4).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.take(maxDots).forEach { name ->
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(AppColors.Surface)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(AppColors.swatch(name))
                    .border(0.5.dp, AppColors.Outline, CircleShape)
            )
        }
        if (colors.size > maxDots) {
            Spacer(Modifier.width(8.dp))
            Text("+${colors.size - maxDots}", style = AppTextStyles.Caption)
        }
    }
}

@Composable
fun ProductCardSkeleton(modifier: Modifier = Modifier, imageAspectRatio: Float = 0.8f) {
    Column(modifier = modifier) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(imageAspectRatio),
            shape = AppShapes.Image
        )
        Spacer(Modifier.height(10.dp))
        ShimmerBox(Modifier
            .width(56.dp)
            .height(10.dp), RoundedCornerShape(4.dp))
        Spacer(Modifier.height(6.dp))
        ShimmerBox(Modifier
            .fillMaxWidth(0.85f)
            .height(14.dp), RoundedCornerShape(4.dp))
        Spacer(Modifier.height(6.dp))
        ShimmerBox(Modifier
            .width(64.dp)
            .height(14.dp), RoundedCornerShape(4.dp))
    }
}

@Composable
fun ProductRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShimmerBox(Modifier.size(104.dp), AppShapes.CardSmall)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            ShimmerBox(Modifier
                .width(60.dp)
                .height(10.dp), RoundedCornerShape(4.dp))
            Spacer(Modifier.height(8.dp))
            ShimmerBox(Modifier
                .fillMaxWidth(0.8f)
                .height(16.dp), RoundedCornerShape(4.dp))
            Spacer(Modifier.height(8.dp))
            ShimmerBox(Modifier
                .width(80.dp)
                .height(14.dp), RoundedCornerShape(4.dp))
        }
    }
}
