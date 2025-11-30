package com.smarttype.aikeyboard.keyboard

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages haptic feedback for keyboard interactions.
 * Provides vibration feedback for key presses and long presses.
 */
@Singleton
class HapticFeedbackManager @Inject constructor(
    private val context: Context
) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
    
    private var hapticEnabled = true
    
    /**
     * Enable or disable haptic feedback.
     */
    fun setHapticEnabled(enabled: Boolean) {
        hapticEnabled = enabled
    }
    
    /**
     * Provides light haptic feedback for regular key presses.
     */
    fun keyPress() {
        val v = vibrator ?: return
        if (!hapticEnabled) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            )
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(10) // 10ms vibration
        }
    }
    
    /**
     * Provides medium haptic feedback for special key presses (Enter, Space, etc.).
     */
    fun specialKeyPress() {
        val v = vibrator ?: return
        if (!hapticEnabled) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            )
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(20) // 20ms vibration
        }
    }
    
    /**
     * Provides haptic feedback for long press actions.
     */
    fun longPress() {
        val v = vibrator ?: return
        if (!hapticEnabled) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 50, 50, 50),
                    -1
                )
            )
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(longArrayOf(0, 50, 50, 50), -1)
        }
    }
    
    /**
     * Provides haptic feedback for errors or invalid actions.
     */
    fun error() {
        val v = vibrator ?: return
        if (!hapticEnabled) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 50, 100),
                    -1
                )
            )
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(longArrayOf(0, 100, 50, 100), -1)
        }
    }
}

