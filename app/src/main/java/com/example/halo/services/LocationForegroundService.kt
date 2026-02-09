package com.example.halo.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.halo.MainActivity
import com.example.halo.R
import com.example.halo.domain.repository.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationForegroundService : Service() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var userPreferencesRepository: com.example.halo.data.repository.UserPreferencesRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var notificationManager: NotificationManager
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TRIGGER_ALARM -> {
                val alarmId = intent.getStringExtra(EXTRA_ALARM_ID)
                if (alarmId != null) {
                    triggerAlarm(alarmId)
                }
            }
            ACTION_STOP_ALARM -> {
                // Stop sound and vibration
                stopAlarmFeedback()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_SNOOZE -> {
                val alarmId = intent.getStringExtra(EXTRA_ALARM_ID)
                if (alarmId != null) {
                    snoozeAlarm(alarmId)
                }
                stopAlarmFeedback()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_TOGGLE_SOUND -> {
                toggleSound()
            }
            ACTION_TOGGLE_VIBRATION -> {
                toggleVibration()
            }
        }
        return START_NOT_STICKY
    }

    private fun triggerAlarm(alarmId: String) {
        serviceScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId.toLongOrNull() ?: -1)
            val alarmName = alarm?.name ?: "Location Reached"

            // Full Screen Intent
            val fullScreenIntent = Intent(this@LocationForegroundService, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "trigger_screen") // Simple way to tell UI to show trigger
                putExtra("alarm_id", alarmId)
            }
            val pendingIntent = PendingIntent.getActivity(
                this@LocationForegroundService, 
                0, 
                fullScreenIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val stopIntent = Intent(this@LocationForegroundService, LocationForegroundService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            val stopPendingIntent = PendingIntent.getService(
                this@LocationForegroundService,
                0,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this@LocationForegroundService, CHANNEL_ID)
                .setContentTitle("Destination Reached")
                .setContentText(alarmName)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "End Alarm", stopPendingIntent)
                .build()

            startForeground(NOTIFICATION_ID, notification)
            
            // Explicitly start the activity for popup
            try {
                startActivity(fullScreenIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Determine sound to play
            val soundUri = if (!alarm?.soundUri.isNullOrEmpty()) {
                android.net.Uri.parse(alarm?.soundUri)
            } else {
                // Fallback to global preference
                val (globalUriString, _) = userPreferencesRepository.alarmSound.first()
                if (globalUriString.isNotEmpty()) android.net.Uri.parse(globalUriString) 
                else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

            playAlarmSound(soundUri)
            vibrate()
        }
    }

    private fun playAlarmSound(alert: android.net.Uri) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alert)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibrate() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 500, 1000), 0)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 500, 1000), 0)
        }
    }

    private fun snoozeAlarm(alarmId: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(this, com.example.halo.receivers.SnoozeReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze for 5 minutes
        val triggerTime = System.currentTimeMillis() + 5 * 60 * 1000
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                 alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                 alarmManager.setAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
             alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    private fun toggleSound() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        } else {
            mediaPlayer?.start()
        }
    }

    private fun toggleVibration() {
        // Checking if vibrating is tricky without tracking state manually since vibrate() is fire-and-forget mostly
        // Simple toggle: if null, start. If exists, cancel.
        if (vibrator != null) {
             vibrator?.cancel()
             vibrator = null
        } else {
            vibrate()
        }
    }

    private fun stopAlarmFeedback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
        vibrator?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        stopAlarmFeedback()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Location Alarms"
                // setSound, vibrationPattern etc
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val ACTION_TRIGGER_ALARM = "ACTION_TRIGGER_ALARM"
        const val ACTION_STOP_ALARM = "ACTION_STOP_ALARM"
        const val ACTION_SNOOZE = "ACTION_SNOOZE"
        const val ACTION_TOGGLE_SOUND = "ACTION_TOGGLE_SOUND"
        const val ACTION_TOGGLE_VIBRATION = "ACTION_TOGGLE_VIBRATION"
        const val EXTRA_ALARM_ID = "EXTRA_ALARM_ID"
        const val CHANNEL_ID = "halo_alarm_channel"
        const val NOTIFICATION_ID = 1
    }
}
