package com.example.tamisknits.features.authentication.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppType

// The stacked "T A M I S / KNITS" wordmark from the splash. KNITS is sized at 2/3 of TAMIS.
@Composable
fun BrandWordmark(
    modifier: Modifier = Modifier,
    color: Color = AppColors.OnTerracotta,
    size: TextUnit = 54.sp
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("T A M I S", style = AppType.Brand.copy(color = color, fontSize = size))
        Spacer(Modifier.height(2.dp))
        Text("KNITS", style = AppType.Brand.copy(color = color, fontSize = size * (2f / 3f)))
    }
}
