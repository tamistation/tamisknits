package com.example.tamisknits.features.admin.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors
import com.example.tamisknits.ui.components.PageHeader
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsPage(onLoggedOut: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 24.dp),
    ) {
        PageHeader(title = "Settings", subtitle = "Manage your account & preferences")

        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
        ) {
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    onLoggedOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Terracotta,
                    contentColor = AppColors.Surface,
                ),
            ) {
                Text("Log out")
            }
        }
    }
}