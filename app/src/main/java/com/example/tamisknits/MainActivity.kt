package com.example.tamisknits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tamisknits.features.admin.orders.OrderManagementScreen
import com.example.tamisknits.features.admin.orders.OrderManagementViewModel
import com.example.tamisknits.repository.FirebaseRepository
import com.example.tamisknits.ui.theme.TamisknitsTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private lateinit var repository: FirebaseRepository
    private lateinit var orderViewModel: OrderManagementViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = FirebaseRepository(
            FirebaseAuth.getInstance(),
            FirebaseFirestore.getInstance()
        )

        // ViewModel needs the repository so we use a factory
        orderViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return OrderManagementViewModel(repository) as T
            }
        })[OrderManagementViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            TamisknitsTheme {
                OrderManagementScreen(viewModel = orderViewModel)


            }
        }

    }
}