package com.coride.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Centralized Network Configuration for Safety Features.
 * Manages API keys and Retrofit instances for ORS and Supabase.
 */
object SafetyNetworkModule {

    // --- KEYS (Provided by User) ---
    private const val ORS_API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImRkZmEzZDRhYzdmNDQ1NjQ4OTA1YjFmY2U0MGU5ZDExIiwiaCI6Im11cm11cjY0In0="
    
    // 🔥 FIREBASE PROJECT URL: Actual user-provided URL
    private const val FIREBASE_DATABASE_URL = "https://my-first-project-d0eac-default-rtdb.firebaseio.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // --- OpenRouteService ---
    private val orsRetrofit = Retrofit.Builder()
        .baseUrl("https://api.openrouteservice.org/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val orsApi: OpenRouteApi = orsRetrofit.create(OpenRouteApi::class.java)

    fun getOrsKey(): String = ORS_API_KEY

    // --- Firebase ---
    private val firebaseRetrofit = Retrofit.Builder()
        .baseUrl(FIREBASE_DATABASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val firebaseApi: FirebaseApi = firebaseRetrofit.create(FirebaseApi::class.java)
}

