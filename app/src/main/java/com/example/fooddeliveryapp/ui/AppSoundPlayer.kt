package com.example.fooddeliveryapp.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.fooddeliveryapp.R

class AppSoundPlayer(context: Context) {
    private val readySoundIds = mutableSetOf<Int>()
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val orderPlacedSoundId = soundPool.load(context.applicationContext, R.raw.order_success, 1)

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                readySoundIds += sampleId
            }
        }
    }

    fun playAddToCart() = Unit

    fun playOrderPlaced() {
        playIfReady(orderPlacedSoundId, rate = 1f)
    }

    fun playFavoriteAdded() = Unit

    fun playNotification() = Unit

    fun playDeliveryArrived() = Unit

    fun release() {
        soundPool.release()
        readySoundIds.clear()
    }

    private fun playIfReady(soundId: Int, rate: Float) {
        if (soundId !in readySoundIds) return
        soundPool.play(soundId, 0.42f, 0.42f, 0, 0, rate)
    }
}

@Composable
fun rememberAppSoundPlayer(): AppSoundPlayer {
    val context = LocalContext.current
    val soundPlayer = remember(context) { AppSoundPlayer(context) }

    DisposableEffect(soundPlayer) {
        onDispose { soundPlayer.release() }
    }

    return soundPlayer
}
