package com.example.tamisknits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tamisknits.navigation.AppNavHost
import com.example.tamisknits.theme.TamisKnitsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint //hilt
class MainActivity : ComponentActivity() { //entry point of the app
    //onCreate() is the first lifecycle method that fires when the Activity is created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()//to fill all edges
        setContent {
            TamisKnitsTheme {
                AppNavHost()
            }
        }
    }
}