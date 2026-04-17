package com.coride.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>
}

// ── OpenWeather DTOs ──
data class WeatherResponse(
    val list: List<WeatherForecastItem>
)

data class WeatherForecastItem(
    val dt: Long,
    val main: MainData,
    val weather: List<WeatherInfo>,
    val dt_txt: String
)

data class MainData(
    val temp: Double,
    val temp_min: Double,
    val temp_max: Double
)

data class WeatherInfo(
    val main: String,
    val description: String,
    val icon: String
)

