package com.example.tamisknits.features.client.productdetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.common.ClientFormat
import com.example.tamisknits.features.client.ui.components.ClientImage
import com.example.tamisknits.features.client.ui.components.FavoriteButton
import com.example.tamisknits.features.client.ui.components.ProductCard
import com.example.tamisknits.features.client.ui.image.ClientImageResolver
import com.example.tamisknits.models.ClientProduct
import com.example.tamisknits.models.ProductReview
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppDimens
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.components.AppCard
import com.example.tamisknits.ui.components.AppChip
import com.example.tamisknits.ui.components.AppSnackbarHost
import com.example.tamisknits.ui.components.AppTextField
import com.example.tamisknits.ui.components.CircleIconButton
import com.example.tamisknits.ui.components.ColorSwatch
import com.example.tamisknits.ui.components.EmptyView
import com.example.tamisknits.ui.components.ErrorView
import com.example.tamisknits.ui.components.MessageEffect
import com.example.tamisknits.ui.components.OfflineBanner
import com.example.tamisknits.ui.components.PrimaryButton
import com.example.tamisknits.ui.components.QuantityStepper
import com.example.tamisknits.ui.components.RatingInput
import com.example.tamisknits.ui.components.RatingStars
import com.example.tamisknits.ui.components.SecondaryButton
import com.example.tamisknits.ui.components.SectionHeader
import com.example.tamisknits.ui.components.ShimmerBox
import com.example.tamisknits.ui.components.readableWidth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private const val VISIBLE_REVIEWS = 3

// Gallery and info come first in the list.
private const val REVIEWS_HEADER_INDEX = 2

@Composable
fun ProductDetailsPage(
    viewModel: ProductDetailsViewModel,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    onViewCart: () -> Unit,
    onRequestCustom: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    MessageEffect(uiState.message, snackbarHostState, viewModel::consumeMessage)

    LaunchedEffect(uiState.addToCartStatus) {
        if (uiState.addToCartStatus == AddToCartStatus.ADDED) {
            val result = snackbarHostState.showSnackbar(
                message = "Added to your cart",
                actionLabel = "View cart",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) onViewCart()
        }
    }

    Scaffold(
        containerColor = AppColors.Surface,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        bottomBar = {
            val product = uiState.product
            AnimatedVisibility(visible = product != null, enter = fadeIn(), exit = fadeOut()) {
                if (product != null) AddToCartBar(uiState, viewModel::addToCart)
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            Crossfade(
                targetState = when {
                    uiState.isLoading -> 0
                    uiState.errorMessage != null -> 1
                    uiState.notFound || uiState.product == null -> 2
                    else -> 3
                },
                label = "detailsState"
            ) { state ->
                when (state) {
                    0 -> DetailsSkeleton()
                    1 -> ErrorView(uiState.errorMessage.orEmpty(), viewModel::retry)
                    2 -> EmptyView(
                        icon = Icons.Rounded.Inventory2,
                        title = "This piece has moved on",
                        message = "It may have sold out or been removed from the shop.",
                        actionLabel = "Go back",
                        onAction = onBack
                    )

                    else -> DetailsContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        onProductClick = onProductClick,
                        onRequestCustom = onRequestCustom
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack,
                    background = AppColors.Surface.copy(alpha = 0.94f)
                )
                Spacer(Modifier.weight(1f))
                if (uiState.product != null) {
                    FavoriteButton(
                        isFavorite = uiState.isFavorite,
                        onToggle = viewModel::toggleFavorite,
                        buttonSize = 42.dp
                    )
                }
            }
        }
    }

    if (uiState.showReviewEditor) {
        ReviewEditorSheet(
            rating = uiState.reviewRating,
            comment = uiState.reviewComment,
            isSaving = uiState.isSavingReview,
            isEditing = uiState.myReview != null,
            onRatingChange = viewModel::onReviewRatingChange,
            onCommentChange = viewModel::onReviewCommentChange,
            onSave = viewModel::saveReview,
            onDelete = viewModel::deleteMyReview,
            onDismiss = viewModel::dismissReviewEditor
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsContent(
    uiState: ProductDetailsUiState,
    viewModel: ProductDetailsViewModel,
    onProductClick: (String) -> Unit,
    onRequestCustom: (String) -> Unit
) {
    val product = uiState.product ?: return
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showAllReviews by remember { mutableStateOf(false) }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        item(key = "gallery") {
            ProductGallery(product, uiState)
        }
        item(key = "info") {
            Column(
                modifier = Modifier
                    .readableWidth()
                    .offset(y = (-24).dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(AppColors.Surface)
                    .padding(horizontal = AppDimens.ScreenPadding, vertical = 24.dp)
            ) {
                OfflineBanner(uiState.isOffline)
                if (product.category.isNotBlank()) {
                    Text(product.category.uppercase(), style = AppTextStyles.Overline)
                    Spacer(Modifier.height(6.dp))
                }
                Text(product.name, style = AppTextStyles.Display)
                if (uiState.ratingSummary.count > 0) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            scope.launch { listState.animateScrollToItem(REVIEWS_HEADER_INDEX) }
                        }
                    ) {
                        RatingStars(uiState.ratingSummary.average)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "%.1f · %d review%s".format(
                                uiState.ratingSummary.average,
                                uiState.ratingSummary.count,
                                if (uiState.ratingSummary.count == 1) "" else "s"
                            ),
                            style = AppTextStyles.Caption
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        ClientFormat.price(uiState.selectedVariant?.price ?: product.minPrice),
                        style = AppTextStyles.PriceLarge
                    )
                    Spacer(Modifier.width(12.dp))
                    StockChip(uiState.availableStock, uiState.selectedVariant != null)
                }

                if (product.colors.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    OptionLabel("Colour", uiState.selectedColor)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(product.colors, key = { it }) { color ->
                            ColorSwatch(
                                colorName = color,
                                selected = uiState.selectedColor.equals(color, ignoreCase = true),
                                available = uiState.isColorAvailable(color),
                                onClick = { viewModel.selectColor(color) },
                                size = 46.dp
                            )
                        }
                    }
                }

                val sizes = uiState.sizesForSelectedColor
                if (sizes.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    OptionLabel("Size", uiState.selectedSize)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sizes.forEach { size ->
                            AppChip(
                                label = size,
                                selected = uiState.selectedSize.equals(size, ignoreCase = true),
                                enabled = uiState.isSizeAvailable(size),
                                onClick = { viewModel.selectSize(size) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Quantity", style = AppTextStyles.Label, modifier = Modifier.weight(1f))
                    QuantityStepper(
                        quantity = uiState.quantity,
                        onDecrease = viewModel::decreaseQuantity,
                        onIncrease = viewModel::increaseQuantity,
                        canIncrease = uiState.quantity < uiState.availableStock
                    )
                }

                if (product.description.isNotBlank()) {
                    Spacer(Modifier.height(28.dp))
                    Text("About this piece", style = AppTextStyles.Subtitle)
                    Spacer(Modifier.height(8.dp))
                    ExpandableText(product.description)
                }

                DetailsTable(product)

                if (product.product.isCustomizable) {
                    Spacer(Modifier.height(20.dp))
                    AppCard(
                        onClick = { onRequestCustom(product.productId) },
                        color = AppColors.Blush,
                        elevated = false
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = AppColors.Terracotta
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Want it your way?", style = AppTextStyles.Label)
                                Text(
                                    "Request this bag in your own colour or size.",
                                    style = AppTextStyles.BodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        item(key = "reviewsHeader") {
            Column(
                modifier = Modifier
                    .readableWidth()
                    .padding(horizontal = AppDimens.ScreenPadding)
            ) {
                HorizontalDivider(color = AppColors.OutlineSoft)
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Reviews", style = AppTextStyles.Subtitle)
                        if (uiState.ratingSummary.count > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "%.1f".format(uiState.ratingSummary.average),
                                    style = AppTextStyles.Label
                                )
                                Spacer(Modifier.width(6.dp))
                                RatingStars(uiState.ratingSummary.average)
                            }
                        } else {
                            Text("No reviews yet. Be the first!", style = AppTextStyles.Caption)
                        }
                    }
                    TextButton(onClick = viewModel::openReviewEditor) {
                        Text(
                            if (uiState.myReview != null) "Edit your review" else "Write a review",
                            style = AppTextStyles.LabelSmall.copy(color = AppColors.Terracotta)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        val reviews = if (showAllReviews) uiState.reviews else uiState.reviews.take(VISIBLE_REVIEWS)
        items(reviews, key = { "review_${it.reviewId}" }) { review ->
            ReviewItem(
                review,
                Modifier
                    .readableWidth()
                    .padding(horizontal = AppDimens.ScreenPadding)
            )
        }
        if (uiState.reviews.size > VISIBLE_REVIEWS && !showAllReviews) {
            item(key = "moreReviews") {
                TextButton(
                    onClick = { showAllReviews = true },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        "Show all ${uiState.reviews.size} reviews",
                        style = AppTextStyles.LabelSmall.copy(color = AppColors.Terracotta)
                    )
                }
            }
        }

        if (uiState.related.isNotEmpty()) {
            item(key = "related") {
                Column(Modifier.padding(top = 28.dp, bottom = 24.dp)) {
                    SectionHeader(
                        title = "You may also love",
                        modifier = Modifier.padding(horizontal = AppDimens.ScreenPadding)
                    )
                    Spacer(Modifier.height(14.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = AppDimens.ScreenPadding),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(uiState.related, key = { it.productId }) { related ->
                            ProductCard(
                                product = related,
                                isFavorite = related.productId in uiState.relatedFavoriteIds,
                                onClick = { onProductClick(related.productId) },
                                onToggleFavorite = { viewModel.toggleRelatedFavorite(related.productId) },
                                modifier = Modifier.width(150.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductGallery(product: ClientProduct, uiState: ProductDetailsUiState) {
    val context = LocalContext.current
    val images = remember(product) { ClientImageResolver.gallery(context, product) }
    val pageCount = images.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { pageCount })

    // Jump to the photo of the colour the client just picked, when there is one.
    val selectedVariant = uiState.selectedVariant
    LaunchedEffect(selectedVariant?.variantId) {
        val variantImage =
            ClientImageResolver.variantImage(context, product.product, selectedVariant)
        val index = images.indexOf(variantImage)
        if (index >= 0 && index != pagerState.currentPage) pagerState.animateScrollToPage(index)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.92f)
            .heightIn(max = 560.dp)
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            ClientImage(
                model = images.getOrNull(page),
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
                placeholderIconSize = 72.dp
            )
        }
        if (pageCount > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .clip(AppShapes.Pill)
                    .background(AppColors.Scrim)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                repeat(pageCount) { index ->
                    val selected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        if (selected) 18.dp else 6.dp,
                        label = "galleryDot"
                    )
                    Box(
                        Modifier
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (selected) AppColors.OnTerracotta else AppColors.OnTerracotta.copy(
                                    alpha = 0.5f
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun StockChip(stock: Int, hasVariant: Boolean) {
    val (label, color, background) = when {
        !hasVariant || stock <= 0 -> Triple(
            "Sold out",
            AppColors.Cancelled,
            AppColors.CancelButtonBackground
        )

        stock <= ClientProduct.LOW_STOCK_THRESHOLD -> Triple(
            "Only $stock left",
            AppColors.Pending,
            AppColors.PendingSoft
        )

        else -> Triple("In stock", AppColors.Delivered, AppColors.DeliveredSoft)
    }
    Text(
        text = label,
        style = AppTextStyles.LabelSmall.copy(color = color),
        modifier = Modifier
            .clip(AppShapes.Pill)
            .background(background)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

@Composable
private fun OptionLabel(title: String, value: String?) {
    Row(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(title, style = AppTextStyles.Label)
        if (!value.isNullOrBlank()) {
            Text(" · $value", style = AppTextStyles.Label.copy(color = AppColors.TextSecondary))
        }
    }
}

@Composable
private fun ExpandableText(text: String) {
    var expanded by remember { mutableStateOf(false) }
    var overflows by remember { mutableStateOf(false) }
    Column(Modifier.animateContentSize()) {
        Text(
            text = text,
            style = AppTextStyles.Body,
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!expanded) overflows = it.hasVisualOverflow }
        )
        if (overflows || expanded) {
            Text(
                text = if (expanded) "Show less" else "Read more",
                style = AppTextStyles.LabelSmall.copy(color = AppColors.Terracotta),
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clickable { expanded = !expanded }
            )
        }
    }
}

@Composable
private fun DetailsTable(product: ClientProduct) {
    val rows = buildList {
        add("Made" to "Handmade with love")
        if (product.materials.isNotBlank()) add("Materials" to product.materials)
        if (product.dimensions.isNotBlank()) add("Dimensions" to product.dimensions)
        if (product.category.isNotBlank()) add("Category" to product.category)
    }
    Spacer(Modifier.height(24.dp))
    AppCard(color = AppColors.SurfaceMuted, elevated = false) {
        rows.forEachIndexed { index, (label, value) ->
            Row(Modifier.padding(vertical = 6.dp)) {
                Text(label, style = AppTextStyles.BodySmall, modifier = Modifier.width(110.dp))
                Text(value, style = AppTextStyles.Label, modifier = Modifier.weight(1f))
            }
            if (index != rows.lastIndex) HorizontalDivider(color = AppColors.OutlineSoft)
        }
    }
}

@Composable
private fun ReviewItem(review: ProductReview, modifier: Modifier = Modifier) {
    val date = remember(review.createdAt) {
        if (review.createdAt > 0) SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(
            Date(
                review.createdAt
            )
        ) else ""
    }
    Column(modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                review.clientName.ifBlank { "Customer" },
                style = AppTextStyles.Label,
                modifier = Modifier.weight(1f)
            )
            Text(date, style = AppTextStyles.Caption)
        }
        Spacer(Modifier.height(4.dp))
        RatingStars(review.rating.toDouble(), starSize = 13.dp)
        if (review.comment.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(review.comment, style = AppTextStyles.Body)
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = AppColors.OutlineSoft)
    }
}

@Composable
private fun AddToCartBar(uiState: ProductDetailsUiState, onAddToCart: () -> Unit) {
    val status = uiState.addToCartStatus
    val unitPrice = uiState.selectedVariant?.price ?: 0.0
    Surface(color = AppColors.Surface, shadowElevation = 12.dp) {
        Row(
            modifier = Modifier
                .readableWidth()
                .navigationBarsPadding()
                .padding(horizontal = AppDimens.ScreenPadding, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Total", style = AppTextStyles.Caption)
                Text(
                    ClientFormat.price(unitPrice * uiState.quantity),
                    style = AppTextStyles.Title.copy(color = AppColors.Terracotta)
                )
            }
            PrimaryButton(
                text = when {
                    status == AddToCartStatus.ADDED -> "Added"
                    uiState.availableStock <= 0 -> "Sold out"
                    else -> "Add to cart"
                },
                icon = if (status == AddToCartStatus.ADDED) Icons.Rounded.Check else Icons.Rounded.ShoppingBag,
                onClick = onAddToCart,
                enabled = uiState.canAddToCart || status == AddToCartStatus.ADDED,
                loading = status == AddToCartStatus.ADDING,
                containerColor = if (status == AddToCartStatus.ADDED) AppColors.Delivered else AppColors.Terracotta,
                modifier = Modifier.weight(1.4f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewEditorSheet(
    rating: Int,
    comment: String,
    isSaving: Boolean,
    isEditing: Boolean,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.Surface,
        shape = AppShapes.Sheet
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppDimens.ScreenPadding)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (isEditing) "Edit your review" else "How do you like it?",
                style = AppTextStyles.Title
            )
            Spacer(Modifier.height(16.dp))
            RatingInput(rating = rating, onRatingChange = onRatingChange)
            Spacer(Modifier.height(20.dp))
            AppTextField(
                value = comment,
                onValueChange = onCommentChange,
                label = "Tell others what you think (optional)",
                singleLine = false,
                minLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = "Post review",
                onClick = onSave,
                loading = isSaving,
                modifier = Modifier.fillMaxWidth()
            )
            if (isEditing) {
                Spacer(Modifier.height(8.dp))
                SecondaryButton(
                    text = "Delete review",
                    onClick = onDelete,
                    contentColor = AppColors.Cancelled,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailsSkeleton() {
    Column(Modifier.fillMaxSize()) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.92f),
            shape = RoundedCornerShape(0.dp)
        )
        Column(Modifier.padding(AppDimens.ScreenPadding)) {
            ShimmerBox(Modifier
                .width(80.dp)
                .height(12.dp), RoundedCornerShape(4.dp))
            Spacer(Modifier.height(10.dp))
            ShimmerBox(Modifier
                .fillMaxWidth(0.7f)
                .height(30.dp), RoundedCornerShape(6.dp))
            Spacer(Modifier.height(14.dp))
            ShimmerBox(Modifier
                .width(110.dp)
                .height(26.dp), RoundedCornerShape(6.dp))
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(4) { ShimmerBox(Modifier.size(46.dp), CircleShape) }
            }
        }
    }
}
