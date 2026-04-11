package com.indrive.clone.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Supabase API Interface via PostgREST.
 * Used for lightweight live location broadcasting.
 */
interface SupabaseApi {

    @POST("rest/v1/ride_live_location")
    suspend fun updateLocation(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Prefer") prefer: String = "return=minimal",
        @Body locationPacket: LocationPacket
    ): Response<Unit>

    data class LocationPacket(
        val ride_id: String,
        val latitude: Double,
        val longitude: Double,
        val timestamp: String // ISO 8601
    )
}
