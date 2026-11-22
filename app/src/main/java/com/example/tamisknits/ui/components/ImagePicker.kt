package com.example.tamisknits.ui.components

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tamisknits.features.client.ui.components.ClientImage
import com.example.tamisknits.features.client.ui.image.ClientImageResolver
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.theme.AppShapes
import com.example.tamisknits.theme.AppTextStyles
import com.example.tamisknits.ui.image.DataUriImages
import kotlinx.coroutines.launch

// Picks a photo from the gallery or camera and hands back a small base64 data URI that can be
// saved straight into Firestore (no Firebase Storage needed). Shows a preview of the current image.
@Composable
fun ImagePicker(
    imageUri: String?,
    onImageSelected: (String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showOptions by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun process(uri: Uri, deleteAfter: Boolean) {
        isProcessing = true
        error = null
        scope.launch {
            val encoded = DataUriImages.fromUri(context, uri)
            if (deleteAfter) runCatching { context.contentResolver.delete(uri, null, null) }
            isProcessing = false
            if (encoded != null) onImageSelected(encoded) else error =
                "Couldn't read that photo. Try another one."
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        showOptions = false
        if (uri != null) process(uri, deleteAfter = false)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = cameraUri
        cameraUri = null
        showOptions = false
        if (uri == null) return@rememberLauncherForActivityResult
        // The photo is copied into the product, so the temporary gallery entry isn't kept.
        if (success) process(uri, deleteAfter = true)
        else runCatching { context.contentResolver.delete(uri, null, null) }
    }

    val model = remember(imageUri) {
        imageUri?.takeIf { it.isNotBlank() }?.let { ClientImageResolver.resolve(context, it) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(AppShapes.Card)
                .background(AppColors.SurfaceMuted)
                .border(1.dp, AppColors.Outline, AppShapes.Card)
                .clickable(enabled = !isProcessing) { showOptions = true },
            contentAlignment = Alignment.Center
        ) {
            if (model != null) {
                ClientImage(
                    model = model,
                    contentDescription = "Product image",
                    modifier = Modifier.matchParentSize()
                )
            } else if (!isProcessing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.AddAPhoto,
                        contentDescription = null,
                        tint = AppColors.Terracotta,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Tap to add a photo", style = AppTextStyles.LabelSmall)
                }
            }
            if (isProcessing) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(AppColors.Scrim),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.OnBrand, strokeWidth = 2.dp)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { showOptions = true }, enabled = !isProcessing) {
                Icon(
                    Icons.Rounded.AddAPhoto,
                    contentDescription = null,
                    tint = AppColors.Terracotta
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    if (model != null) "Change photo" else "Add photo",
                    style = AppTextStyles.Label.copy(color = AppColors.Terracotta)
                )
            }
            if (model != null) {
                TextButton(onClick = { onImageSelected(null) }, enabled = !isProcessing) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = AppColors.TextMuted
                    )
                    Spacer(Modifier.size(6.dp))
                    Text("Remove", style = AppTextStyles.Label.copy(color = AppColors.TextMuted))
                }
            }
        }

        error?.let {
            Text(
                it,
                style = AppTextStyles.Caption.copy(color = AppColors.Cancelled),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }

    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text("Product photo") },
            text = {
                Row {
                    TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(
                            Icons.Rounded.PhotoLibrary,
                            contentDescription = null,
                            tint = AppColors.Terracotta
                        )
                        Spacer(Modifier.size(6.dp))
                        Text("Gallery", color = AppColors.Terracotta)
                    }
                    TextButton(
                        onClick = {
                            val values = ContentValues().apply {
                                put(
                                    MediaStore.Images.Media.DISPLAY_NAME,
                                    "image_${System.currentTimeMillis()}.jpg"
                                )
                                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                            }
                            val uri = context.contentResolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                values
                            )
                            if (uri != null) {
                                cameraUri = uri
                                cameraLauncher.launch(uri)
                            }
                        }
                    ) {
                        Icon(
                            Icons.Rounded.CameraAlt,
                            contentDescription = null,
                            tint = AppColors.Terracotta
                        )
                        Spacer(Modifier.size(6.dp))
                        Text("Camera", color = AppColors.Terracotta)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOptions = false }) {
                    Text("Cancel", color = AppColors.TextMuted)
                }
            }
        )
    }
}
