package com.example.tamisknits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tamisknits.features.admin.orders.OrderManagementPage
import dagger.hilt.android.AndroidEntryPoint
import com.example.tamisknits.navigation.AppNavHost

@AndroidEntryPoint //hilt
class MainActivity : ComponentActivity() { //entry point of the app
    //onCreate() is the first lifecycle method that fires when the Activity is created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()//to fill all edges
        setContent {
            AppNavHost()
        }
    }
}