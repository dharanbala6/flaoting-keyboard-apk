package com.example.vicecheatoverlay

import android.os.SystemClock
import android.util.Log
import android.view.InputDevice
import android.view.InputEvent
import android.view.KeyCharacterMap
import android.view.KeyEvent

/** Runs in Shizuku's privileged user-service process. */
class InputUserService : IInputUserService.Stub() {
    override fun inject(code: String, delayMs: Long) {
        try {
            Log.d(TAG, "Starting injection: $code")
            code.forEach { character ->
                val keyCode = KeyEvent.keyCodeFromString("KEYCODE_$character")
                if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
                    throw IllegalArgumentException("Unsupported character: $character")
                }

                Log.d(TAG, "Injecting key: $character")
                val downResult = injectKey(keyCode, KeyEvent.ACTION_DOWN)
                Log.d(TAG, "keyDown result = $downResult")
                val upResult = injectKey(keyCode, KeyEvent.ACTION_UP)
                Log.d(TAG, "keyUp result = $upResult")

                if (!downResult || !upResult) {
                    throw IllegalStateException("InputManager rejected key event for $character")
                }
                Thread.sleep(delayMs)
            }
            Log.d(TAG, "Injection finished")
        } catch (error: Throwable) {
            Log.e(TAG, "Injection failed", error)
            throw error
        }
    }

    private fun injectKey(keyCode: Int, action: Int): Boolean {
        val eventTime = SystemClock.uptimeMillis()
        val event = KeyEvent(
            eventTime,
            eventTime,
            action,
            keyCode,
            0,
            0,
            KeyCharacterMap.VIRTUAL_KEYBOARD,
            0,
            KeyEvent.FLAG_FROM_SYSTEM,
            InputDevice.SOURCE_KEYBOARD
        )
        return injectInputEvent(event)
    }

    private fun injectInputEvent(event: InputEvent): Boolean {
        val inputManagerClass = Class.forName("android.hardware.input.InputManager")
        val inputManager = inputManagerClass.getDeclaredMethod("getInstance").invoke(null)
        val injectInputEvent = inputManagerClass.getMethod(
            "injectInputEvent",
            InputEvent::class.java,
            Int::class.javaPrimitiveType
        )
        return injectInputEvent.invoke(inputManager, event, INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH) as Boolean
    }

    companion object {
        private const val TAG = "SAInjector"
        private const val INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2
    }
}
