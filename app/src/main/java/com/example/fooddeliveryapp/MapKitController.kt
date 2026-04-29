package com.example.fooddeliveryapp

import android.content.Context
import com.yandex.mapkit.MapKitFactory

object MapKitController {
    private var initialized = false
    private var started = false

    fun initialize(context: Context) {
        if (initialized || BuildConfig.MAPKIT_API_KEY.isBlank()) return
        MapKitFactory.initialize(context.applicationContext)
        initialized = true
    }

    fun onStart(context: Context) {
        initialize(context)
        if (!initialized || started) return
        MapKitFactory.getInstance().onStart()
        started = true
    }

    fun onStop() {
        if (!initialized || !started) return
        MapKitFactory.getInstance().onStop()
        started = false
    }
}
