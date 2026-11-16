package com.example.q_list.presentation.customer

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.q_list.R

object QueueTurnAlarm {

    private var mediaPlayer: MediaPlayer? = null

    fun play(context: Context) {
        stop()

        mediaPlayer = MediaPlayer.create(
            context.applicationContext,
            R.raw.turn_alarm
        )?.apply {
            setOnCompletionListener { player ->
                player.release()
                if (mediaPlayer === player) {
                    mediaPlayer = null
                }
            }

            start()
        }

        // Vibrate the phone.
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (vibrator?.hasVibrator() == true) {
            val pattern = longArrayOf(0, 350, 150, 350)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(pattern, -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }

    fun stop() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) player.stop()
            player.release()
        }
        mediaPlayer = null
    }
}