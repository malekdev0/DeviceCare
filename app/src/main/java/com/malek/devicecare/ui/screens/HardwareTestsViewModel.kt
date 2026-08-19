package com.malek.devicecare.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.os.*
import android.hardware.camera2.CameraManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.malek.devicecare.DeviceCareApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HardwareTestsViewModel(private val context: Context) : ViewModel() {

    private val _vibrationStatus = MutableStateFlow("Ready")
    val vibrationStatus: StateFlow<String> = _vibrationStatus.asStateFlow()

    private val _flashlightStatus = MutableStateFlow("Ready")
    val flashlightStatus: StateFlow<String> = _flashlightStatus.asStateFlow()
    private var isFlashlightOn = false

    private val _speakerStatus = MutableStateFlow("Ready")
    val speakerStatus: StateFlow<String> = _speakerStatus.asStateFlow()
    private var mediaPlayer: android.media.MediaPlayer? = null

    fun testFlashlight() {
        viewModelScope.launch {
            try {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val cameraId = cameraManager.cameraIdList[0]
                isFlashlightOn = !isFlashlightOn
                cameraManager.setTorchMode(cameraId, isFlashlightOn)
                _flashlightStatus.value = if (isFlashlightOn) "ON" else "OFF"
            } catch (e: Exception) {
                _flashlightStatus.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun testVibration() {
        viewModelScope.launch {
            try {
                _vibrationStatus.value = "Connecting..."
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                
                if (!vibrator.hasVibrator()) {
                    _vibrationStatus.value = "Hardware missing"
                    return@launch
                }

                val effect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
                
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                
                vibrator.vibrate(effect, audioAttributes)
                
                _vibrationStatus.value = "Vibrating (1s)..."
                delay(1100)
                _vibrationStatus.value = "Success"
                delay(2000)
                _vibrationStatus.value = "Ready"
            } catch (e: Exception) {
                _vibrationStatus.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun testSpeaker() {
        viewModelScope.launch {
            try {
                _speakerStatus.value = "Playing..."
                val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)
                toneGen.startTone(android.media.ToneGenerator.TONE_CDMA_PIP, 2000)
                delay(2000)
                _speakerStatus.value = "Success"
                delay(2000)
                _speakerStatus.value = "Ready"
            } catch (e: Exception) {
                _speakerStatus.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DeviceCareApplication)
                HardwareTestsViewModel(application)
            }
        }
    }
}
