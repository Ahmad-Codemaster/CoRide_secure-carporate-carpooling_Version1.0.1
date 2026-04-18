package com.coride.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.coride.R
import com.coride.data.repository.MockDataRepository
import com.coride.ui.main.MainActivity
import com.coride.utils.EmailNotificationHelper
import com.coride.utils.FirebaseSafetyHelper
import com.coride.utils.SmsSafetyHelper

/**
 * Foreground Service for active rides.
 * 
 * Shows a persistent notification in the status bar while a ride is in progress.
 * Tapping the notification returns the user to the ride screen.
 * 
 * When the service is destroyed (app killed), it sends an automated SMS
 * to all emergency contacts with the last known location.
 */
class RideForegroundService : Service() {

    companion object {
        const val TAG = "RideForegroundService"
        const val CHANNEL_ID = "coride_ride_channel"
        const val NOTIFICATION_ID = 42
        
        const val ACTION_START = "com.coride.action.START_RIDE_SERVICE"
        const val ACTION_STOP = "com.coride.action.STOP_RIDE_SERVICE"
        
        const val EXTRA_DRIVER_NAME = "driver_name"
        const val EXTRA_DESTINATION = "destination"
        const val EXTRA_RIDE_ID = "ride_id"
        const val EXTRA_LAST_LAT = "last_lat"
        const val EXTRA_LAST_LNG = "last_lng"

        private var isRunning = false
        fun isServiceRunning(): Boolean = isRunning

        /**
         * Start the ride foreground service.
         */
        fun startService(context: Context, rideId: String, driverName: String, destination: String, lat: Double, lng: Double) {
            val intent = Intent(context, RideForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RIDE_ID, rideId)
                putExtra(EXTRA_DRIVER_NAME, driverName)
                putExtra(EXTRA_DESTINATION, destination)
                putExtra(EXTRA_LAST_LAT, lat)
                putExtra(EXTRA_LAST_LNG, lng)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Stop the ride foreground service normally (ride completed/cancelled).
         */
        fun stopService(context: Context) {
            val intent = Intent(context, RideForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        /**
         * Update the last known location (called periodically during ride).
         */
        fun updateLocation(context: Context, rideId: String, lat: Double, lng: Double) {
            val intent = Intent(context, RideForegroundService::class.java).apply {
                putExtra(EXTRA_RIDE_ID, rideId)
                putExtra(EXTRA_LAST_LAT, lat)
                putExtra(EXTRA_LAST_LNG, lng)
            }
            context.startService(intent)
        }
    }

    private var rideId: String = ""
    private var driverName: String = ""
    private var destination: String = ""
    private var lastLat: Double = 0.0
    private var lastLng: Double = 0.0
    private var wasStoppedNormally = false
    private var lastPushTime: Long = 0L

    // ── Urgent SOS State (Volume & Shake) ──
    private var volumePressCount = 0
    private var lastVolumePressTime = 0L
    private val VOLUME_SOS_WINDOW_MS = 3000L
    private var volumeReceiver: android.content.BroadcastReceiver? = null

    private var sensorManager: android.hardware.SensorManager? = null
    private var accelerometer: android.hardware.Sensor? = null
    private var lastShakeTime = 0L
    private var shakeCount = 0
    private val SHAKE_THRESHOLD = 14.0f
    private val SHAKE_WINDOW_MS = 2000L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        com.coride.data.local.LocalPreferences.init(this)
        createNotificationChannel()
        setupUrgentSosListeners()
    }

    private fun setupUrgentSosListeners() {
        // 1. Volume SOS (1-2-3 Done)
        val filter = android.content.IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        volumeReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastVolumePressTime > VOLUME_SOS_WINDOW_MS) {
                    volumePressCount = 1
                } else {
                    volumePressCount++
                }
                lastVolumePressTime = currentTime
                
                if (volumePressCount >= 3) {
                    Log.i(TAG, "🔊 Volume SOS Triggered!")
                    triggerUrgentSos("Volume Button")
                    volumePressCount = 0
                }
            }
        }
        registerReceiver(volumeReceiver, filter)

        // 2. Shake SOS (Shake and Done)
        if (com.coride.data.local.LocalPreferences.isShakeSosEnabled()) {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
            accelerometer = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
            
            sensorManager?.registerListener(shakeListener, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_NORMAL)
            Log.i(TAG, "🌪️ Shake SOS Listener Active")
        }
    }

    private val shakeListener = object : android.hardware.SensorEventListener {
        override fun onSensorChanged(event: android.hardware.SensorEvent?) {
            if (event == null) return
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val acceleration = kotlin.math.sqrt(x*x + y*y + z*z) - android.hardware.SensorManager.GRAVITY_EARTH
            
            if (acceleration > SHAKE_THRESHOLD) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastShakeTime < 500) return
                if (currentTime - lastShakeTime > SHAKE_WINDOW_MS) {
                    shakeCount = 1
                } else {
                    shakeCount++
                }
                lastShakeTime = currentTime
                if (shakeCount >= 3) {
                    Log.i(TAG, "🌪️ Shake SOS Triggered!")
                    triggerUrgentSos("Shake Motion")
                    shakeCount = 0
                }
            }
        }
        override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
    }

    private fun triggerUrgentSos(source: String) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(1000, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(1000)
        }

        val context = this
        val user = MockDataRepository.getCurrentUser()
        val sosMessage = SmsSafetyHelper.buildSosMessage(rideId, lastLat, lastLng) + "\n(Sent via $source)"
        
        SmsSafetyHelper.sendToAllEmergencyContacts(context, sosMessage)
        EmailNotificationHelper.sendSosAlert(user, rideId, lastLat, lastLng)
        MockDataRepository.triggerSOS()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                rideId = intent.getStringExtra(EXTRA_RIDE_ID) ?: "demo_ride"
                driverName = intent.getStringExtra(EXTRA_DRIVER_NAME) ?: ""
                destination = intent.getStringExtra(EXTRA_DESTINATION) ?: ""
                lastLat = intent.getDoubleExtra(EXTRA_LAST_LAT, 0.0)
                lastLng = intent.getDoubleExtra(EXTRA_LAST_LNG, 0.0)
                wasStoppedNormally = false
                isRunning = true
                
                // Initial push (as "user")
                FirebaseSafetyHelper.pushLocationUpdate(rideId, "user", lastLat, lastLng)
                lastPushTime = System.currentTimeMillis()

                startForeground(NOTIFICATION_ID, buildNotification())
                Log.d(TAG, "Ride Foreground Service started — $driverName → $destination")
            }
            ACTION_STOP -> {
                wasStoppedNormally = true
                isRunning = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                Log.d(TAG, "Ride Foreground Service stopped normally")
            }
            else -> {
                // Location update
                val newRideId = intent?.getStringExtra(EXTRA_RIDE_ID)
                if (newRideId != null) rideId = newRideId

                val newLat = intent?.getDoubleExtra(EXTRA_LAST_LAT, 0.0) ?: 0.0
                val newLng = intent?.getDoubleExtra(EXTRA_LAST_LNG, 0.0) ?: 0.0
                if (newLat != 0.0) {
                    lastLat = newLat
                    lastLng = newLng

                    // Push to Firebase if at least 5 seconds passed
                    val now = System.currentTimeMillis()
                    if (now - lastPushTime >= 5000L) {
                        FirebaseSafetyHelper.pushLocationUpdate(rideId, "user", lastLat, lastLng)
                        lastPushTime = now
                    }
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        
        // Unregister volume listener
        volumeReceiver?.let {
            unregisterReceiver(it)
        }

        // Unregister shake listener
        sensorManager?.unregisterListener(shakeListener)
        
        // If service was destroyed WITHOUT a normal stop (app killed/crashed),
        // send emergency SMS to all contacts
        if (!wasStoppedNormally && lastLat != 0.0) {
            Log.w(TAG, "Service destroyed abnormally! Sending offline alert SMS.")
            val message = SmsSafetyHelper.buildOfflineAlertMessage(
                rideId, lastLat, lastLng
            )
            SmsSafetyHelper.sendToAllEmergencyContacts(this, message)
        }

        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "CoRide Active Ride",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when you have an active ride"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        // Tap notification → opens the app (returns to ride)
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("return_to_ride", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🚗 CoRide — Ride in progress")
            .setContentText("Heading to $destination with $driverName")
            .setSmallIcon(R.drawable.ic_car)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .build()
    }
}

