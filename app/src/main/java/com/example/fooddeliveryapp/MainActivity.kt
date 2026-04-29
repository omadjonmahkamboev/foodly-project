package com.example.fooddeliveryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fooddeliveryapp.ui.FoodDeliveryApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitController.initialize(this)
        enableEdgeToEdge()
        setContent {
            FoodDeliveryApp()
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitController.onStart(this)
    }

    override fun onStop() {
        MapKitController.onStop()
        super.onStop()
    }
}






