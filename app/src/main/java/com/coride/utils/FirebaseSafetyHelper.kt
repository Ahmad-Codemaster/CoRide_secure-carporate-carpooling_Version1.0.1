package com.coride.utils

import android.util.Log
import com.coride.data.network.SafetyNetworkModule
import com.coride.data.network.FirebaseApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FirebaseSafetyHelper {
    private const val TAG = "FirebaseSafetyHelper"

    fun pushLocationUpdate(rideId: String, role: String, lat: Double, lng: Double) {
        val packet = FirebaseApi.LocationPacket(
            latitude = lat,
            longitude = lng,
            timestamp = System.currentTimeMillis()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "[GPS PUSH] Pushing $role location to Firebase... [Ride: $rideId]")
                val response = SafetyNetworkModule.firebaseApi.updateLocation(rideId, role, packet)

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Firebase Success [$role]: $lat, $lng")
                } else {
                    Log.e(TAG, "❌ Firebase Error [$role]: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "🚨 Firebase Exception [$role]: ${e.message}")
            }
        }
    }
}

