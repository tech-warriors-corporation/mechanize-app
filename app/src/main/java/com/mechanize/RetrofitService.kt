package com.mechanize

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitAccountsService {
    @POST("login")
    fun login(@Body body: Map<String, String>): Call<Payload<LoginPayload>>

    @POST("users")
    fun createAccount(@Body body: Map<String, String>): Call<Payload<Int>>
}

interface RetrofitHelpsService {
    @POST("tickets")
    fun createTicket(@Body body: Map<String, String>): Call<Payload<Int>>
}
