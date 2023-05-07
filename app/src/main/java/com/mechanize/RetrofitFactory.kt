package com.mechanize

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitFactory {
    private val factoryAccounts: Retrofit = Retrofit.Builder().baseUrl(BuildConfig.API_URL_ACCOUNTS).addConverterFactory(GsonConverterFactory.create()).build()
    private val factoryHelps: Retrofit = Retrofit.Builder().baseUrl(BuildConfig.API_URL_HELPS).addConverterFactory(GsonConverterFactory.create()).build()

    fun retrofitAccountsService(): RetrofitAccountsService{
        return factoryAccounts.create(RetrofitAccountsService::class.java)
    }

    fun retrofitHelpsService(): RetrofitHelpsService{
        return factoryHelps.create(RetrofitHelpsService::class.java)
    }
}
