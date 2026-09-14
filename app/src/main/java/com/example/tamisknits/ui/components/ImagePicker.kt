package com.example.tamisknits.ui.components

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage


@Composable
fun ImagePicker(
    imageUri: String?,
    onImageSelected: (String?) -> Unit
) {
    val context = LocalContext.current

    var cameraUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->

        if (uri != null) {
            onImageSelected(uri.toString())
        }
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
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = {
                    galleryLauncher.launch("image/*")
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = if (imageUri.isNullOrEmpty()) {
                        "Gallery"
                    } else {
                        "Change"
                    }
                )
            }

            Button(
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
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text("Camera")
            }
        }

        if (!imageUri.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            AsyncImage(
                model = imageUri,
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}