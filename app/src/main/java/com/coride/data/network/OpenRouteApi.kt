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

    @GET("geocode/autocomplete")
    suspend fun getAutocomplete(
        @Query("api_key") apiKey: String,
        @Query("text") text: String,
        @Query("focus.point.lat") lat: Double? = null,
        @Query("focus.point.lon") lon: Double? = null,
        @Query("size") size: Int = 10
    ): Response<GeocodeResponse>

    data class ORSResponse(
        @SerializedName("features") val features: List<Feature>?
    )

    data class GeocodeResponse(
        @SerializedName("features") val features: List<GeocodeFeature>?
    )

    data class GeocodeFeature(
        @SerializedName("geometry") val geometry: GeocodeGeometry?,
        @SerializedName("properties") val properties: GeocodeProperties?
    )

    data class GeocodeGeometry(
        @SerializedName("coordinates") val coordinates: List<Double>? // [lon, lat]
    )

    data class GeocodeProperties(
        @SerializedName("label") val label: String?, // Full address
        @SerializedName("name") val name: String?,   // Feature name
        @SerializedName("id") val id: String?
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
