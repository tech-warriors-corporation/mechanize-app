package com.mechanize

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitFactory {
    private val factory: Retrofit = Retrofit.Builder().baseUrl(BuildConfig.API_URL).addConverterFactory(GsonConverterFactory.create()).build()

    fun retrofitService(): RetrofitService{
        return factory.create(RetrofitService::class.java)
    }
}
