package com.coride.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Firebase Realtime Database API Interface via REST.
 * Used for lightweight live location broadcasting without SDK overhead.
 */
interface FirebaseApi {

    @PUT("rides/{rideId}/locations/{role}.json")
    suspend fun updateLocation(
        @Path("rideId") rideId: String,
        @Path("role") role: String,
        @Body locationPacket: LocationPacket
    ): Response<Unit>

    data class LocationPacket(
        val latitude: Double,
        val longitude: Double,
        val timestamp: Long
    )
}

