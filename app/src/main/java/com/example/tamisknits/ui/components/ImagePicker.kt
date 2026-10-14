package com.example.tamisknits.ui.components

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors

@Composable
fun ImagePicker(
    imageUri: String?,
    onImageSelected: (String?) -> Unit
) {
    val context = LocalContext.current

    var showOptions by remember {
        mutableStateOf(false)
    }

    var cameraUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(uri.toString())
        }

        showOptions = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraUri != null) {
            onImageSelected(cameraUri.toString())
        } else if (!success && cameraUri != null) {
            context.contentResolver.delete(
                cameraUri!!,
                null,
                null
            )
        }

        cameraUri = null
        showOptions = false
    }

    Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "Add product image",
        tint = AppColors.Terracotta,
        modifier = Modifier
            .size(28.dp)
            .clickable {
                showOptions = true
            }
    )

    if (showOptions) {
        AlertDialog(
            onDismissRequest = {
                showOptions = false
            },
            title = {
                Text("Choose Product Image")
            },
            text = {
                Row {
                    TextButton(
                        onClick = {
                            galleryLauncher.launch("image/*")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = AppColors.Terracotta
                        )

                        Spacer(modifier = Modifier.size(6.dp))

                        Text(
                            text = "Gallery",
                            color = AppColors.Terracotta
                        )
                    }

                    TextButton(
                        onClick = {
                            val values = ContentValues().apply {
                                put(
                                    MediaStore.Images.Media.DISPLAY_NAME,
                                    "image_${System.currentTimeMillis()}.jpg"
                                )

                                put(
                                    MediaStore.Images.Media.MIME_TYPE,
                                    "image/jpeg"
                                )
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
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = AppColors.Terracotta
                        )

                        Spacer(modifier = Modifier.size(6.dp))

                        Text(
                            text = "Camera",
                            color = AppColors.Terracotta
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOptions = false
                    }
                ) {
                    Text(
                        text = "Cancel",
                        color = AppColors.TextMuted
                    )
                }
            }
        )
    }
}