package com.example.vicecheatoverlay

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var injectorButton: Button
    private var permissionDenied = false

    private val binderListener = Shizuku.OnBinderReceivedListener {
        Log.d(TAG, "Shizuku binder received")
        updateFromShizukuBinder("Shizuku not running")
    }
    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.d(TAG, "Shizuku binder died")
        showStatus("Shizuku not running")
    }
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == ShizukuInputInjector.REQUEST_CODE) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Shizuku permission granted")
                permissionDenied = false
                showAccessGranted()
            } else {
                Log.d(TAG, "Shizuku permission denied")
                permissionDenied = true
                showStatus("Permission denied")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.statusText)
        injectorButton = findViewById(R.id.shizukuButton)
        Shizuku.addBinderReceivedListener(binderListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionListener)
        refreshStatus()

        injectorButton.setOnClickListener {
            Log.d("SAInjector", "Get Injector Access clicked")
            requestInjectorAccess()
        }
        findViewById<Button>(R.id.overlayButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
        findViewById<Button>(R.id.startButton).setOnClickListener {
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2001)
            }
            if (Settings.canDrawOverlays(this)) {
                ContextCompat.startForegroundService(this, Intent(this, OverlayService::class.java))
                moveTaskToBack(true)
            } else {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
        }
        findViewById<Button>(R.id.stopButton).setOnClickListener { stopService(Intent(this, OverlayService::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    override fun onDestroy() {
        Shizuku.removeBinderReceivedListener(binderListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        super.onDestroy()
    }

    private fun requestInjectorAccess() {
        try {
            val available = Shizuku.pingBinder()
            Log.d(TAG, "Shizuku binder available = $available")

            if (!available) {
                permissionDenied = false
                showStatus("Waiting for Shizuku connection")
                return
            }

            val permission = Shizuku.checkSelfPermission()
            Log.d(TAG, "Shizuku permission result = $permission")
            if (permission == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Shizuku permission granted")
                permissionDenied = false
                showAccessGranted()
                return
            }

            Log.d(TAG, "Requesting Shizuku permission")
            Shizuku.requestPermission(ShizukuInputInjector.REQUEST_CODE)
        } catch (e: Throwable) {
            Log.e(TAG, "Injector access error", e)
            showStatus("Injector access error")
        }
    }

    private fun updateFromShizukuBinder(unavailableMessage: String) {
        try {
            val available = Shizuku.pingBinder()
            Log.d(TAG, "Shizuku binder available = $available")

            if (!available) {
                showStatus(unavailableMessage)
                return
            }

            val permission = Shizuku.checkSelfPermission()
            Log.d(TAG, "Shizuku permission result = $permission")
            if (permission == PackageManager.PERMISSION_GRANTED) {
                permissionDenied = false
                showAccessGranted()
            } else if (permissionDenied) {
                showStatus("Permission denied")
            } else {
                showStatus("Permission required")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Injector access error", e)
            showStatus("Shizuku not running")
        }
    }

    private fun refreshStatus() {
        if (!ShizukuInputInjector.isInstalled(this)) {
            showStatus("Shizuku not installed")
            return
        }

        updateFromShizukuBinder("Shizuku not running")
    }

    private fun showStatus(message: String) {
        runOnUiThread {
            val overlay = if (Settings.canDrawOverlays(this)) "granted" else "required"
            injectorButton.text = "Get Injector Access"
            statusText.text = "$message\nOverlay permission: $overlay"
        }
    }

    private fun showAccessGranted() {
        runOnUiThread {
            val message = "Injector access granted \u2713"
            val overlay = if (Settings.canDrawOverlays(this)) "granted" else "required"
            injectorButton.text = message
            statusText.text = "$message\nOverlay permission: $overlay"
        }
    }

    companion object {
        private const val TAG = "SAInjector"
    }
}
