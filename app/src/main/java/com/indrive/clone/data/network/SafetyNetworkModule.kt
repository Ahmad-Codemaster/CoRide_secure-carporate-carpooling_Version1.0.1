package com.indrive.clone.data.network

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
    private const val SUPABASE_URL = "https://ollduovaiqlcmmqebliv.supabase.co/"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9sbGR1b3ZhaXFsY21tcWVibGl2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU2NDgzMzUsImV4cCI6MjA5MTIyNDMzNX0.6LMQNW6kHgiRrHc-MPp-6xvYLwA8h07UGB8nrnCTkPc"

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

    // --- Supabase ---
    private val supabaseRetrofit = Retrofit.Builder()
        .baseUrl(SUPABASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val supabaseApi: SupabaseApi = supabaseRetrofit.create(SupabaseApi::class.java)

    fun getSupabaseKey(): String = SUPABASE_ANON_KEY
}
