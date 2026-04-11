package com.indrive.clone.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.indrive.clone.R
import com.indrive.clone.ui.main.MainActivity
import com.indrive.clone.utils.SmsSafetyHelper
import com.indrive.clone.utils.SupabaseSafetyHelper

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
        
        const val ACTION_START = "com.indrive.clone.action.START_RIDE_SERVICE"
        const val ACTION_STOP = "com.indrive.clone.action.STOP_RIDE_SERVICE"
        
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

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
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
                
                // Initial push
                SupabaseSafetyHelper.pushLocationUpdate(rideId, lastLat, lastLng)
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

                    // Push to Supabase if at least 5 seconds passed
                    val now = System.currentTimeMillis()
                    if (now - lastPushTime >= 5000L) {
                        SupabaseSafetyHelper.pushLocationUpdate(rideId, lastLat, lastLng)
                        lastPushTime = now
                    }
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        
        // If service was destroyed WITHOUT a normal stop (app killed/crashed),
        // send emergency SMS to all contacts
        if (!wasStoppedNormally && lastLat != 0.0) {
            Log.w(TAG, "Service destroyed abnormally! Sending offline alert SMS.")
            val message = SmsSafetyHelper.buildOfflineAlertMessage(lastLat, lastLng)
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
