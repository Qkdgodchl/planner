package com.example.myapplication.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // OpenWeatherMap
    private val weatherRetrofit =
        Retrofit.Builder()
            .baseUrl(
                "https://api.openweathermap.org/data/2.5/"
            )
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()

    private val okHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

    // Gemini
    private val geminiRetrofit =
        Retrofit.Builder()
            .baseUrl(
                "https://generativelanguage.googleapis.com/"
            )
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()

    val weatherApi: WeatherApi by lazy {

        weatherRetrofit.create(
            WeatherApi::class.java
        )
    }

    val geminiApi: GeminiApiService by lazy {

        geminiRetrofit.create(
            GeminiApiService::class.java
        )
    }
}