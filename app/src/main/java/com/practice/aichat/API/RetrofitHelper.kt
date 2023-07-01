package com.practice.aichat.API

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {

        val BASE_URL = "https://www.googleapis.com/customsearch/"

        fun create(): API_InterFace {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(API_InterFace::class.java)
        }
}