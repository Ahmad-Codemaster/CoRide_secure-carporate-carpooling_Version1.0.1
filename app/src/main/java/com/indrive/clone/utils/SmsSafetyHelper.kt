package com.indrive.clone.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.indrive.clone.data.model.TrustedContact
import com.indrive.clone.data.repository.MockDataRepository

/**
 * Silent SMS Safety System for CoRide.
 * Sends automated background SMS to all emergency contacts — no user interaction needed.
 *
 * Uses Android's built-in SmsManager which sends via the phone's SIM card.
 * Cost: Uses the user's existing carrier SMS plan (most PK carriers include free SMS bundles).
 */
object SmsSafetyHelper {

    private const val TAG = "SmsSafetyHelper"
    const val SMS_PERMISSION_REQUEST_CODE = 1001

    /**
     * Check if SMS permission is granted.
     */
    fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request SMS permission from the user.
     */
    fun requestSmsPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.SEND_SMS),
            SMS_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Send a silent background SMS to a single phone number.
     * No UI is opened — the message is dispatched directly via the SIM card.
     */
    fun sendSilentSms(context: Context, phoneNumber: String, message: String): Boolean {
        if (!hasSmsPermission(context)) {
            Log.w(TAG, "SMS permission not granted. Cannot send.")
            return false
        }

        return try {
            val cleanPhone = phoneNumber.replace(Regex("[^0-9+]"), "")
            if (cleanPhone.isEmpty()) {
                Log.w(TAG, "Invalid phone number: $phoneNumber")
                return false
            }

            val smsManager = SmsManager.getDefault()
            // Split long messages into parts (SMS limit is 160 chars)
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)

            Log.d(TAG, "SMS sent successfully to $cleanPhone")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $phoneNumber: ${e.message}")
            false
        }
    }

    /**
     * Send SMS to ALL trusted/emergency contacts simultaneously.
     * Fully automated — fires in the background without any user interaction.
     */
    fun sendToAllEmergencyContacts(context: Context, message: String): Int {
        val contacts = MockDataRepository.getTrustedContacts()
        val user = MockDataRepository.getCurrentUser()

        // Also include the user's emergency contact if set
        val allContacts = contacts.toMutableList()
        if (user.emergencyContactPhone.isNotEmpty()) {
            allContacts.add(
                TrustedContact(
                    name = user.emergencyContactName.ifEmpty { "Emergency Contact" },
                    phone = user.emergencyContactPhone
                )
            )
        }

        if (allContacts.isEmpty()) {
            Log.w(TAG, "No emergency contacts configured. SMS not sent.")
            return 0
        }

        var successCount = 0
        allContacts.forEach { contact ->
            if (sendSilentSms(context, contact.phone, message)) {
                successCount++
            }
        }

        Log.d(TAG, "Sent SMS to $successCount/${allContacts.size} emergency contacts")
        return successCount
    }

    // ── Pre-built Message Templates ──

    /**
     * Generate the ride details message sent when a ride starts.
     */
    fun buildRideStartMessage(
        rideId: String,
        driverName: String,
        vehicleInfo: String,
        plateNumber: String,
        destination: String,
        destLat: Double,
        destLng: Double
    ): String {
        val userName = MockDataRepository.getCurrentUser().name
        
        // 🚨 CONFIGURATION: Replace this URL with your hosted live_tracker.html URL from Supabase Storage
        val trackerBaseUrl = "https://your-project-id.supabase.co/storage/v1/object/public/tracker/live_tracker.html"
        val liveLink = "$trackerBaseUrl?ride=$rideId"

        return """
🚗 CoRide Safety Alert
$userName has started a ride.

👤 Driver: $driverName (Verified ✅)
🚙 Vehicle: $vehicleInfo
🔢 Plate: $plateNumber
📍 Heading to: $destination

🌐 TRACK LIVE: $liveLink
📍 Google Maps: https://maps.google.com/?q=$destLat,$destLng

This is an automated safety message from CoRide.
        """.trimIndent()
    }

    /**
     * Generate the SOS emergency message.
     */
    fun buildSosMessage(rideId: String, lat: Double, lng: Double): String {
        val userName = MockDataRepository.getCurrentUser().name
        
        // 🚨 CONFIGURATION: Replace this URL with your hosted live_tracker.html URL from Supabase Storage
        val trackerBaseUrl = "https://your-project-id.supabase.co/storage/v1/object/public/tracker/live_tracker.html"
        val liveLink = "$trackerBaseUrl?ride=$rideId"

        return """
🆘 EMERGENCY — CoRide SOS
$userName has triggered an emergency alert during a ride!

🌐 TRACK LIVE: $liveLink
📍 Google Maps: https://maps.google.com/?q=$lat,$lng

Please check on them immediately or call emergency services (15).

This is an automated emergency alert from CoRide.
        """.trimIndent()
    }

    /**
     * Generate the "user went offline" alert message.
     */
    fun buildOfflineAlertMessage(rideId: Double, lat: Double, lng: Double): String {
        val userName = MockDataRepository.getCurrentUser().name
        
        // 🚨 CONFIGURATION: Replace this URL with your hosted live_tracker.html URL from Supabase Storage
        val trackerBaseUrl = "https://your-project-id.supabase.co/storage/v1/object/public/tracker/live_tracker.html"
        val liveLink = "$trackerBaseUrl?ride=$rideId"

        return """
⚠️ CoRide Safety Warning
$userName may be offline during an active ride.

The CoRide app has stopped responding.

🌐 LAST KNOWN LIVE: $liveLink
📍 Google Maps: https://maps.google.com/?q=$lat,$lng

Please check on them immediately.

This is an automated safety alert from CoRide.
        """.trimIndent()
    }
}
