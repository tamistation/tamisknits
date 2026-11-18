package com.example.tamisknits.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tamisknits.theme.AppColors

@Composable
fun AppSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = AppColors.TextDark,
            contentColor = AppColors.OnTerracotta,
            actionColor = AppColors.BlushDeep
        )
    }
}

// Shows a one-off message from UI state, then tells the ViewModel it was consumed.
@Composable
fun MessageEffect(
    message: String?,
    hostState: SnackbarHostState,
    onShown: () -> Unit,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    val currentOnShown = rememberUpdatedState(onShown)
    val currentOnAction = rememberUpdatedState(onAction)
    LaunchedEffect(message) {
        if (message != null) {
            val result = hostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) currentOnAction.value()
            currentOnShown.value()
        }
    }
}
