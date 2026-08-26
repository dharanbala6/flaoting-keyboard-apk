package com.example.vicecheatoverlay

import android.content.pm.PackageManager
import android.view.KeyEvent
import rikka.shizuku.Shizuku
import java.io.IOException

object ShizukuInputInjector {
    const val REQUEST_CODE = 1001

    fun isInstalled(context: android.content.Context): Boolean = try {
        context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    fun isRunning(): Boolean = try { Shizuku.pingBinder() } catch (_: Throwable) { false }

    fun hasPermission(): Boolean = try {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (_: Throwable) { false }

    fun requestPermission() {
        if (isRunning() && !hasPermission() && !Shizuku.shouldShowRequestPermissionRationale()) {
            Shizuku.requestPermission(REQUEST_CODE)
        }
    }

    @Suppress("DEPRECATION")
    @Throws(IOException::class, SecurityException::class)
    fun inject(code: String, delayMs: Long = 40L) {
        require(hasPermission()) { "Shizuku permission is not granted" }
        code.uppercase().filter { it in 'A'..'Z' || it in '0'..'9' }.forEach { character ->
            val keyCode = KeyEvent.keyCodeFromString("KEYCODE_$character")
            require(keyCode != KeyEvent.KEYCODE_UNKNOWN) { "Unsupported character: $character" }
            val process = Shizuku.newProcess(arrayOf("input", "keyevent", keyCode.toString()), null, null)
            val exit = process.waitFor()
            if (exit != 0) throw IOException("input keyevent failed with exit code $exit")
            Thread.sleep(delayMs)
        }
    }
}
