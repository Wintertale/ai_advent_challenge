package com.aiadvent.day1.service

import com.aiadvent.day1.model.YandexGptRequest
import com.aiadvent.day1.model.YandexGptResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface YandexGptService {
    @POST("foundationModels/v1/completion")
    @Headers("Content-Type: application/json")
    fun sendMessage(
        @Header("Authorization") auth: String,
        @Body request: YandexGptRequest
    ): Call<YandexGptResponse>
}
