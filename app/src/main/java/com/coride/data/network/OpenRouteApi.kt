package com.coride.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * OpenRouteService API Interface.
 * Fetches real street-following polylines and road data.
 */
interface OpenRouteApi {

    @GET("v2/directions/driving-car")
    suspend fun getDirections(
        @Header("Authorization") apiKey: String,
        @Query("start") start: String, // lon,lat
        @Query("end") end: String      // lon,lat
    ): Response<ORSResponse>

    data class ORSResponse(
        @SerializedName("features") val features: List<Feature>?
    )

    data class Feature(
        @SerializedName("geometry") val geometry: Geometry?,
        @SerializedName("properties") val properties: Properties?
    )

    data class Geometry(
        @SerializedName("coordinates") val coordinates: List<List<Double>>? // List of [lon, lat]
    )

    data class Properties(
        @SerializedName("summary") val summary: Summary?
    )

    data class Summary(
        @SerializedName("distance") val distance: Double?,
        @SerializedName("duration") val duration: Double?
    )
}

