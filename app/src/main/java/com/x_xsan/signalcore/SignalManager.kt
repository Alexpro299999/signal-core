
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.x_xsan.signalcore.R
import kotlinx.coroutines.*

class SignalManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val vibrator = (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)

    private var ringtone: Ringtone? = null
    private var flashlightJob: Job? = null
    private var signalJob: Job? = null

    fun startSignal() {
        signalJob?.cancel()
        signalJob = CoroutineScope(Dispatchers.Default).launch {
            Log.d("SignalManager", "Starting 4-second signal!")
            startVibration()
            startFlashlight()
            startSound()
            delay(4000L)
            Log.d("SignalManager", "Auto-stopping signal after 2 seconds.")
            stopSignal()
        }
    }

    fun stopSignal() {
        stopVibration()
        stopFlashlight()
        stopSound()
    }

    @SuppressLint("NewApi")
    private fun startSound() {
        try {
            val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.notification_sound}")
            Log.d("SignalManager", "Playing sound from raw resource. URI: $soundUri")

            ringtone = RingtoneManager.getRingtone(context, soundUri)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            ringtone?.audioAttributes = audioAttributes

            ringtone?.isLooping = true

            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

            ringtone?.play()

        } catch (e: Exception) {
            Log.e("SignalManager", "Failed to play sound from raw", e)
        }
    }

    private fun startVibration() {
        if (vibrator.hasVibrator()) {
            val vibrationEffect = VibrationEffect.createWaveform(longArrayOf(500, 500), 0)
            vibrator.vibrate(vibrationEffect)
        }
    }

    private fun startFlashlight() {
        flashlightJob?.cancel()
        try {
            val cameraId = cameraManager.cameraIdList[0]
            flashlightJob = CoroutineScope(Dispatchers.Default).launch {
                while (isActive) {
                    cameraManager.setTorchMode(cameraId, true); delay(250)
                    cameraManager.setTorchMode(cameraId, false); delay(250)
                }
            }
        } catch (e: Exception) {
            Log.e("SignalManager", "Failed to start flashlight", e)
        }
    }

    private fun stopSound() { ringtone?.stop() }
    private fun stopVibration() { vibrator.cancel() }
    private fun stopFlashlight() {
        flashlightJob?.cancel()
        try {
            cameraManager.setTorchMode(cameraManager.cameraIdList[0], false)
        } catch (e: Exception) {}
    }
}