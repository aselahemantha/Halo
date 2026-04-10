package com.exoticstech.halo.services

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
import android.util.Log
import androidx.core.app.NotificationCompat
import com.exoticstech.halo.MainActivity
import com.exoticstech.halo.R
import com.exoticstech.halo.domain.model.Alarm
import com.exoticstech.halo.domain.repository.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@AndroidEntryPoint
class LocationForegroundService : Service() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var userPreferencesRepository: com.exoticstech.halo.data.repository.UserPreferencesRepository

    @Inject
    lateinit var geofenceManager: com.exoticstech.halo.domain.GeofenceManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var notificationManager: NotificationManager
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val recentlyTriggeredAlarms: MutableSet<Long> = ConcurrentHashMap.newKeySet()
    private var autoTimeoutJob: Job? = null

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
                    serviceScope.launch {
                        val alarm = alarmRepository.getAlarmById(alarmId.toLongOrNull() ?: -1)
                        if (alarm != null) {
                            triggerAlarm(alarm)
                        }
                    }
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

    private fun triggerAlarm(alarm: Alarm) {
        // Prevent re-triggering within cooldown
        if (recentlyTriggeredAlarms.contains(alarm.id)) {
            Log.d("LocationService", "Alarm ${alarm.name} recently triggered. Skipping.")
            return
        }

        // Schedule Logic Check
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentMinutesSinceMidnight = currentHour * 60 + currentMinute

        // 1. Check Day of Week
        if (alarm.daysOfWeek.isNotEmpty() && !alarm.daysOfWeek.contains(currentDayOfWeek)) {
            Log.d("LocationService", "Alarm ${alarm.name} schedule mismatch (Day of week). Skipping.")
            return
        }

        // 2. Check Time Window
        if (alarm.startTimeHour != null && alarm.startTimeMinute != null && alarm.endTimeHour != null && alarm.endTimeMinute != null) {
            val startMinutesSinceMidnight = alarm.startTimeHour * 60 + alarm.startTimeMinute
            val endMinutesSinceMidnight = alarm.endTimeHour * 60 + alarm.endTimeMinute
            
            val isWithinWindow = if (startMinutesSinceMidnight <= endMinutesSinceMidnight) {
                // Normal window (e.g., 8:00 AM to 5:00 PM)
                currentMinutesSinceMidnight in startMinutesSinceMidnight..endMinutesSinceMidnight
            } else {
                // Overnight window (e.g., 10:00 PM to 6:00 AM)
                currentMinutesSinceMidnight >= startMinutesSinceMidnight || currentMinutesSinceMidnight <= endMinutesSinceMidnight
            }

            if (!isWithinWindow) {
                Log.d("LocationService", "Alarm ${alarm.name} schedule mismatch (Time window). Skipping.")
                return
            }
        }

        // --- Execute Trigger ---

        Log.d("LocationService", "Triggering alarm: ${alarm.name}")
        serviceScope.launch {
            recentlyTriggeredAlarms.add(alarm.id)
            launch {
                delay(ALARM_COOLDOWN_MILLIS)
                recentlyTriggeredAlarms.remove(alarm.id)
            }

            val alarmName = alarm.name.ifBlank { getString(R.string.notif_location_reached) }

            // Full Screen Intent — use alarm ID as request code to avoid collisions
            val fullScreenIntent = Intent(this@LocationForegroundService, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "trigger_screen")
                putExtra("alarm_id", alarm.id.toString())
            }
            val fullScreenPendingIntent = PendingIntent.getActivity(
                this@LocationForegroundService, 
                REQUEST_CODE_FULLSCREEN_BASE + alarm.id.toInt(), 
                fullScreenIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Stop intent — use a distinct fixed request code
            val stopIntent = Intent(this@LocationForegroundService, LocationForegroundService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            val stopPendingIntent = PendingIntent.getService(
                this@LocationForegroundService,
                REQUEST_CODE_STOP,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Build notification with generic background text
            val notificationBuilder = NotificationCompat.Builder(this@LocationForegroundService, CHANNEL_ID)
                .setContentTitle(getString(R.string.notif_app_active))
                .setContentText(getString(R.string.notif_app_monitoring))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(fullScreenPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Keep high for full-screen intent
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setAutoCancel(true)
                // Removed redundant actions as they are in the full-screen UI

            // Only set fullScreenIntent if the permission is granted (Android 14+).
            // Otherwise, the high-priority heads-up notification serves as fallback.
            val canUseFullScreen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                notificationManager.canUseFullScreenIntent()
            } else {
                true
            }

            if (canUseFullScreen) {
                notificationBuilder.setFullScreenIntent(fullScreenPendingIntent, true)
            }

            val notification = notificationBuilder.build()
            startForeground(NOTIFICATION_ID, notification)
            
            // Explicitly start the activity for popup
            try {
                startActivity(fullScreenIntent)
            } catch (e: Exception) {
                Log.w("LocationService", "Could not start trigger activity", e)
            }
            
            // Determine sound to play
            val soundUri = if (!alarm.soundUri.isNullOrEmpty()) {
                android.net.Uri.parse(alarm.soundUri)
            } else {
                // Fallback to global preference
                val (globalUriString, _) = userPreferencesRepository.alarmSound.first()
                if (globalUriString.isNotEmpty()) android.net.Uri.parse(globalUriString) 
                else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

            // Log the trigger history
            val history = com.exoticstech.halo.domain.model.AlarmHistory(
                alarmId = alarm.id,
                alarmName = alarm.name,
                triggerTime = System.currentTimeMillis(),
                latitude = alarm.latitude,
                longitude = alarm.longitude
            )
            alarmRepository.insertAlarmHistory(history)

            // Disable the alarm and remove geofence since it was triggered
            val updatedAlarm = alarm.copy(isEnabled = false)
            alarmRepository.updateAlarm(updatedAlarm)
            geofenceManager.removeGeofence(alarm.id)

            playAlarmSound(soundUri)
            vibrate()

            // Auto-timeout: stop sound and vibration after 5 minutes to prevent battery drain
            autoTimeoutJob?.cancel()
            autoTimeoutJob = launch {
                delay(ALARM_AUTO_TIMEOUT_MILLIS)
                Log.d("LocationService", "Auto-timeout reached, stopping alarm feedback")
                stopAlarmFeedback()
            }
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
        val intent = Intent(this, com.exoticstech.halo.receivers.SnoozeReceiver::class.java).apply {
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
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notif_channel_desc)
                // setSound, vibrationPattern etc
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val ALARM_COOLDOWN_MILLIS = 300_000L // 5 minutes
        const val ALARM_AUTO_TIMEOUT_MILLIS = 300_000L // 5 minutes — auto-stop sound/vibration
        const val ACTION_TRIGGER_ALARM = "ACTION_TRIGGER_ALARM"
        const val ACTION_STOP_ALARM = "ACTION_STOP_ALARM"
        const val ACTION_SNOOZE = "ACTION_SNOOZE"
        const val ACTION_TOGGLE_SOUND = "ACTION_TOGGLE_SOUND"
        const val ACTION_TOGGLE_VIBRATION = "ACTION_TOGGLE_VIBRATION"
        const val EXTRA_ALARM_ID = "EXTRA_ALARM_ID"
        const val CHANNEL_ID = "halo_alarm_channel"
        const val NOTIFICATION_ID = 1
        // Unique PendingIntent request codes to avoid collisions
        private const val REQUEST_CODE_FULLSCREEN_BASE = 1000
        private const val REQUEST_CODE_STOP = 2000
        private const val REQUEST_CODE_SNOOZE_BASE = 3000
    }
}
