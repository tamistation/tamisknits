package com.example.tamisknits.features.authentication.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamisknits.theme.AppColors
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Made with love, made for you",
            description = "Discover handmade crochet pieces crafted with care, attention to detail, and a little bit of love."
        ),
        OnboardingPage(
            title = "Find your perfect style",
            description = "Explore our bags and clutches, and find the colors, sizes, and designs that feel like you."
        ),
        OnboardingPage(
            title = "Your next favorite piece is waiting",
            description = "Choose your favorite piece, place your order, and let us take care of the rest."
        )
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Cream)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = if (pagerState.currentPage < pages.lastIndex) {
                    "Skip"
                } else {
                    ""
                },
                color = AppColors.TextMuted,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(8.dp)
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->

            OnboardingPageContent(
                page = pages[page]
            )
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            pages.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(
                            if (pagerState.currentPage == index) 9.dp else 7.dp
                        )
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) {
                                AppColors.Terracotta
                            } else {
                                AppColors.Blush
                            }
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (pagerState.currentPage == pages.lastIndex) {

                    viewModel.completeOnboarding()
                    onFinished()

                } else {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            pagerState.currentPage + 1
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Terracotta,
                contentColor = AppColors.Cream
            )
        ) {
            Text(
                text = if (pagerState.currentPage == pages.lastIndex) {
                    "Get Started"
                } else {
                    "Continue"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Image/illustration will go here later.

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = page.title,
            color = AppColors.TextDark,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            color = AppColors.TextMuted,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}