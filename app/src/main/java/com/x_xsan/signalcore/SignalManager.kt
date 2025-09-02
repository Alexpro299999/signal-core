package com.x_xsan.signalcore

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.*

class SignalManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val vibrator = (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)

    private var ringtone: Ringtone? = null
    private var flashlightJob: Job? = null

    fun startSignal() {
        Log.d("SignalManager", "Starting signal!")
        startVibration()
        startFlashlight()
        startSound()
    }

    fun stopSignal() {
        Log.d("SignalManager", "Stopping signal!")
        stopVibration()
        stopFlashlight()
        stopSound()
    }



    private fun startVibration() {
        if (vibrator.hasVibrator()) {
            val vibrationEffect = VibrationEffect.createWaveform(longArrayOf(500, 500), 0)
            vibrator.vibrate(vibrationEffect)
        }
    }

    private fun stopVibration() {
        vibrator.cancel()
    }

    private fun startFlashlight() {
        try {
            val cameraId = cameraManager.cameraIdList[0]
            flashlightJob = CoroutineScope(Dispatchers.Default).launch {
                while (isActive) {
                    cameraManager.setTorchMode(cameraId, true)
                    delay(250)
                    cameraManager.setTorchMode(cameraId, false)
                    delay(250)
                }
            }
        } catch (e: Exception) {
            Log.e("SignalManager", "Failed to start flashlight", e)
        }
    }

    private fun stopFlashlight() {
        flashlightJob?.cancel()
        try {
            cameraManager.setTorchMode(cameraManager.cameraIdList[0], false)
        } catch (e: Exception) {
        }
    }

    private fun startSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(context, alarmUri)

            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

            ringtone?.play()
        } catch (e: Exception) {
            Log.e("SignalManager", "Failed to play sound", e)
        }
    }

    private fun stopSound() {
        ringtone?.stop()
    }
}