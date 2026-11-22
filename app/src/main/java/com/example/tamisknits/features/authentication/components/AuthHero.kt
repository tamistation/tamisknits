package com.example.tamisknits.features.authentication.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppType

internal val AuthHeroHeight = 300.dp
internal val AuthHeroCardTop = 220.dp

// Straight-edged burgundy band behind the top of the form card.
@Composable
internal fun AuthHero(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AuthHeroHeight)
            .background(AppColors.BrandBurgundy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("T A M I S", style = AppType.Brand.copy(color = AppColors.OnBrand))
            Spacer(Modifier.height(2.dp))
            Text("KNITS", style = AppType.Brand.copy(color = AppColors.OnBrand, fontSize = 24.sp))
        }
    }
}
