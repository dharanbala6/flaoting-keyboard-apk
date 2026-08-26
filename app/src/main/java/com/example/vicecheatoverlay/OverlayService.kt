package com.example.vicecheatoverlay

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlin.concurrent.thread
import kotlin.math.abs

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var bubble: View? = null
    private var menu: View? = null
    private lateinit var bubbleParams: WindowManager.LayoutParams

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Vice Cheat Overlay")
            .setContentText("Floating bubble is active")
            .setContentIntent(pendingIntent).setOngoing(true).build()
        startForeground(NOTIFICATION_ID, notification)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        showBubble()
    }

    private fun showBubble() {
        if (bubble != null) return
        val view = LayoutInflater.from(this).inflate(R.layout.overlay_bubble, null)
        bubbleParams = WindowManager.LayoutParams(
            dp(56), dp(56), WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = dp(16); y = dp(160) }
        var downX = 0; var downY = 0; var touchX = 0f; var touchY = 0f
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { downX = bubbleParams.x; downY = bubbleParams.y; touchX = event.rawX; touchY = event.rawY; true }
                MotionEvent.ACTION_MOVE -> { bubbleParams.x = downX + (event.rawX - touchX).toInt(); bubbleParams.y = downY + (event.rawY - touchY).toInt(); windowManager.updateViewLayout(view, bubbleParams); true }
                MotionEvent.ACTION_UP -> { if (abs(event.rawX - touchX) < 12 && abs(event.rawY - touchY) < 12) showMenu(); true }
                else -> false
            }
        }
        bubble = view
        windowManager.addView(view, bubbleParams)
    }

    private fun showMenu() {
        if (menu != null) return
        bubble?.visibility = View.GONE
        val view = LayoutInflater.from(this).inflate(R.layout.overlay_menu, null)
        val params = WindowManager.LayoutParams(
            dp(280), WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND, PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER; dimAmount = 0.25f; softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE }
        val list = view.findViewById<LinearLayout>(R.id.cheatList)
        CheatRepository.cheats.forEach { cheat ->
            val button = Button(this).apply {
                isAllCaps = false
                text = "${cheat.description}\n${cheat.code}"
                setOnClickListener { sendCheat(cheat.code) }
            }
            list.addView(button)
        }
        val manual = view.findViewById<EditText>(R.id.manualCheat)
        view.findViewById<Button>(R.id.sendManual).setOnClickListener {
            val code = manual.text.toString()
            if (code.any { it.isLetterOrDigit() }) sendCheat(code)
        }
        view.findViewById<Button>(R.id.closeMenu).setOnClickListener { closeMenu() }
        menu = view
        windowManager.addView(view, params)
    }

    private fun sendCheat(rawCode: String) {
        val code = rawCode.uppercase().filter { it in 'A'..'Z' || it in '0'..'9' }
        closeMenu()
        thread(name = "cheat-injector") {
            Thread.sleep(350) // Let Android return input focus to the game first.
            try {
                ShizukuInputInjector.inject(this, code, 40)
            } catch (error: Throwable) {
                android.os.Handler(mainLooper).post { Toast.makeText(this, error.message ?: "Injection failed", Toast.LENGTH_LONG).show() }
            }
        }
    }

    private fun closeMenu() {
        menu?.let { if (it.isAttachedToWindow) windowManager.removeView(it) }
        menu = null
        bubble?.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        menu?.let { if (it.isAttachedToWindow) windowManager.removeView(it) }
        bubble?.let { if (it.isAttachedToWindow) windowManager.removeView(it) }
        menu = null; bubble = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Overlay service", NotificationManager.IMPORTANCE_LOW))
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    companion object { const val CHANNEL_ID = "vice_overlay"; const val NOTIFICATION_ID = 7 }
}
