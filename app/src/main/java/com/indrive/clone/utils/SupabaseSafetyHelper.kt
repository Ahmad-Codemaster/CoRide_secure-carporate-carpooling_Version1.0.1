package com.indrive.clone.utils

import android.util.Log
import com.indrive.clone.data.network.SafetyNetworkModule
import com.indrive.clone.data.network.SupabaseApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Supabase Live Tracking Helper.
 * Broadcasts location updates to the Supabase database.
 */
object SupabaseSafetyHelper {

    private const val TAG = "SupabaseSafetyHelper"

    /**
     * Pushes a location update to Supabase.
     * Fires in a background coroutine (Fire-and-Forget).
     */
    fun pushLocationUpdate(rideId: String, lat: Double, lng: Double) {
        val apiKey = SafetyNetworkModule.getSupabaseKey()
        val bearerToken = "Bearer $apiKey"
        
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        val packet = SupabaseApi.LocationPacket(
            ride_id = rideId,
            latitude = lat,
            longitude = lng,
            timestamp = timestamp
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = SafetyNetworkModule.supabaseApi.updateLocation(
                    apiKey = apiKey,
                    bearerToken = bearerToken,
                    locationPacket = packet
                )

                if (response.isSuccessful) {
                    Log.d(TAG, "Location pushed to Supabase: [ID: $rideId] $lat, $lng")
                } else {
                    Log.e(TAG, "Failed to push to Supabase: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception pushing to Supabase: ${e.message}")
            }
        }
    }
}
