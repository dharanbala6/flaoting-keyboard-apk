package com.example.vicecheatoverlay

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import rikka.shizuku.Shizuku
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object ShizukuInputInjector {
    const val REQUEST_CODE = 1001

    fun isInstalled(context: Context): Boolean = try {
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

    @Throws(IOException::class, SecurityException::class)
    fun inject(context: Context, code: String, delayMs: Long = 40L) {
        require(hasPermission()) { "Grant injector access before sending cheats" }
        val normalized = code.uppercase().filter { it in 'A'..'Z' || it in '0'..'9' }
        require(normalized.isNotEmpty()) { "Cheat code is empty" }
        val latch = CountDownLatch(1)
        var remote: IInputUserService? = null
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                remote = IInputUserService.Stub.asInterface(service)
                latch.countDown()
            }
            override fun onServiceDisconnected(name: ComponentName?) { remote = null }
        }
        val args = Shizuku.UserServiceArgs(ComponentName(context, InputUserService::class.java))
            .daemon(false).processNameSuffix("input").version(1)
        Shizuku.bindUserService(args, connection)
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) throw IOException("Timed out connecting to Shizuku user service")
            remote?.inject(normalized, delayMs) ?: throw IOException("Shizuku user service disconnected")
        } finally {
            Shizuku.unbindUserService(args, connection, true)
        }
    }
}
