package com.example.tamisknits.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.models.AvatarOptions
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppTextStyles

private data class AvatarStyle(val icon: ImageVector?, val background: Color, val tint: Color)

private fun avatarStyle(avatarId: String): AvatarStyle = when (avatarId) {
    AvatarOptions.YARN -> AvatarStyle(Icons.Rounded.Gesture, Color(0xFFF3E6D3), Color(0xFF8A5A3C))
    AvatarOptions.BAG -> AvatarStyle(
        Icons.Rounded.ShoppingBag,
        AppColors.Blush,
        AppColors.Terracotta
    )

    AvatarOptions.FLOWER -> AvatarStyle(
        Icons.Rounded.LocalFlorist,
        Color(0xFFE7EEE2),
        Color(0xFF5E7A45)
    )

    AvatarOptions.HEART -> AvatarStyle(Icons.Rounded.Favorite, Color(0xFFFBE3E6), Color(0xFFB3262E))
    AvatarOptions.STAR -> AvatarStyle(
        Icons.Rounded.AutoAwesome,
        Color(0xFFF8EFDC),
        AppColors.MarigoldGold
    )

    else -> AvatarStyle(null, AppColors.Terracotta, Color.White)
}

@Composable
fun UserAvatar(
    avatarId: String,
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val style = avatarStyle(avatarId)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(style.background),
        contentAlignment = Alignment.Center
    ) {
        if (style.icon != null) {
            Icon(
                style.icon,
                contentDescription = null,
                tint = style.tint,
                modifier = Modifier.size(size * 0.48f)
            )
        } else {
            Text(
                text = initials,
                style = AppTextStyles.Title.copy(
                    color = style.tint,
                    fontSize = (size.value * 0.36f).sp
                )
            )
        }
    }
}

@Composable
fun AvatarOption(
    avatarId: String,
    initials: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val ring by animateDpAsState(if (selected) 3.dp else 0.dp, label = "avatarRing")
    Box(
        modifier = modifier
            .size(size + 8.dp)
            .clip(CircleShape)
            .border(ring, AppColors.Terracotta, CircleShape)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        UserAvatar(avatarId = avatarId, initials = initials, size = size)
    }
}
