package com.example.vicecheatoverlay

import android.view.KeyEvent
import java.io.IOException

/** Runs in Shizuku's privileged user-service process (shell/root identity). */
class InputUserService : IInputUserService.Stub() {
    override fun inject(code: String, delayMs: Long) {
        code.forEach { character ->
            val keyCode = KeyEvent.keyCodeFromString("KEYCODE_$character")
            if (keyCode == KeyEvent.KEYCODE_UNKNOWN) throw IllegalArgumentException("Unsupported character: $character")
            val process = Runtime.getRuntime().exec(arrayOf("input", "keyevent", keyCode.toString()))
            val exit = process.waitFor()
            if (exit != 0) throw IOException("input keyevent failed with exit code $exit")
            Thread.sleep(delayMs)
        }
    }
}
