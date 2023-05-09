package com.mechanize

import android.content.Context
import com.mechanize.interceptors.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun getAuthClient(context: Context): OkHttpClient {
    return OkHttpClient.Builder().addInterceptor(AuthInterceptor(context)).build()
}

class RetrofitFactory {
    private val factoryAccounts = Retrofit.Builder().baseUrl(BuildConfig.API_URL_ACCOUNTS).addConverterFactory(GsonConverterFactory.create())
    private val factoryHelps = Retrofit.Builder().baseUrl(BuildConfig.API_URL_HELPS).addConverterFactory(GsonConverterFactory.create())

    fun retrofitAccountsService(context: Context): RetrofitAccountsService{
        return factoryAccounts.client(getAuthClient(context)).build().create(RetrofitAccountsService::class.java)
    }

    fun retrofitHelpsService(context: Context): RetrofitHelpsService{
        return factoryHelps.client(getAuthClient(context)).build().create(RetrofitHelpsService::class.java)
    }
}
