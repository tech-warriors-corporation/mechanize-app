package com.mechanize

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface RetrofitAccountsService {
    @POST("login")
    fun login(@Body body: Map<String, String>): Call<Payload<UserPayload>>

    @GET("get-user-by-token")
    fun getUserByToken(): Call<Payload<UserPayload>>

    @POST("users")
    fun createAccount(@Body body: Map<String, String>): Call<Payload<Int>>
}

interface RetrofitHelpsService {
    @POST("tickets")
    fun createTicket(@Body body: Map<String, String>): Call<Payload<Int>>

    @PATCH("tickets/{id}/cancel-ticket")
    fun cancelTicket(@Path("id") id: Int): Call<Payload<Int>>
}
