package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.LauncherApplication
import com.example.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FocusTimerForegroundService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "focus_timer_channel"
        
        private val _remainingSeconds = MutableStateFlow(0)
        val remainingSeconds: StateFlow<Int> = _remainingSeconds

        private val _isTimerRunning = MutableStateFlow(false)
        val isTimerRunning: StateFlow<Boolean> = _isTimerRunning

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_DURATION_SECONDS = "EXTRA_DURATION_SECONDS"
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val durationSeconds = intent.getIntExtra(EXTRA_DURATION_SECONDS, 1500) // 25 min default
                startTimer(durationSeconds)
            }
            ACTION_STOP -> {
                stopTimer()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimer(durationSeconds: Int) {
        timerJob?.cancel()
        _remainingSeconds.value = durationSeconds
        _isTimerRunning.value = true

        // Acquire wake lock to keep cpu awake during crucial ticks
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Minilauncher:FocusTimerWakeLock").apply {
                acquire(durationSeconds * 1000L + 5000L)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Start Foreground Notification
        val notification = buildNotification(durationSeconds)
        startForeground(NOTIFICATION_ID, notification)

        // Count down loop
        timerJob = serviceScope.launch {
            val app = application as? LauncherApplication
            val repo = app?.container?.repository

            while (_remainingSeconds.value > 0) {
                delay(1000L)
                _remainingSeconds.value -= 1
                
                // Write progress to Settings so rest of ViewModel is updated
                repo?.saveSetting("focus_timer_seconds_remaining", _remainingSeconds.value.toString())

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, buildNotification(_remainingSeconds.value))
            }

            // Completed! Log the focus session
            repo?.let { r ->
                r.saveSetting("focus_mode_active", "false")
                r.saveSetting("focus_timer_seconds_remaining", "0")
                
                // Log complete Focus session
                r.logFocusSession("Deep Work Pomodoro", durationSeconds / 60, true)
            }

            _isTimerRunning.value = false
            stopTimer()
            stopSelf()
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
        _remainingSeconds.value = 0
        
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val app = application as? LauncherApplication
        val repo = app?.container?.repository
        serviceScope.launch {
            repo?.saveSetting("focus_timer_seconds_remaining", "0")
            repo?.saveSetting("focus_mode_active", "false")
        }
    }

    private fun buildNotification(remaining: Int): Notification {
        val minutes = remaining / 60
        val seconds = remaining % 60
        val timeStr = String.format("%02d:%02d", minutes, seconds)

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, FocusTimerForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelName = getString(R.string.notification_channel_name)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Deep Focus Session Engaged")
            .setContentText("Focus Period Remaining: $timeStr")
            .setSmallIcon(android.R.drawable.ic_media_play) // Use system core icons safely
            .setContentIntent(mainPendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "ABANDON SESSION", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        serviceScope.cancel()
    }
}
