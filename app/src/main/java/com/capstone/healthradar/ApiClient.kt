package com.capstone.healthradar

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://your-project.vercel.app/api" // ⚡ change to your FastAPI IP

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: NewsApiService by lazy {
        retrofit.create(NewsApiService::class.java)
    }
}
