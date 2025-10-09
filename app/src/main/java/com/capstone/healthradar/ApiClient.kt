package com.capstone.healthradar

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // ✅ remove "/api/"
    private const val BASE_URL = "https://health-radar2-dvi41ledi-vaughner123s-projects.vercel.app/"

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
