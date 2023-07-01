package com.practice.aichat.API

import com.practice.aichat.Models.Google_Response
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface API_InterFace {
    @GET("v1")
    fun urlHit(
        @Query("key") apiKey: String,
        @Query("cx") cx: String,
        @Query("q") query: String
    ): Call<Google_Response>

}