package com.example.myapplication.network

import com.example.myapplication.data.GeminiRequest
import com.example.myapplication.data.GeminiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import okhttp3.ResponseBody

interface GeminiApiService {

    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}