package com.example.eventmanagement.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.apply
import kotlin.jvm.java

/**
 * Retrofit Client Singleton
 * Sesuai dengan Class Diagram: RetrofitClient
 * Membuat instance Retrofit dan ApiService
 */
object RetrofitClient {

    // Base URL dari dokumentasi API
    private const val BASE_URL = "http://104.248.153.158/event-api/"

    /**
     * Logging Interceptor untuk debugging
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * OkHttp Client dengan konfigurasi timeout dan logging
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Retrofit instance
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * API Service instance
     * Digunakan oleh EventRepository
     */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}