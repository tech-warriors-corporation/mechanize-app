package com.mechanize

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitService {
    @POST("accounts/login")
    fun login(@Body body: Map<String, String>): Call<Payload<LoginPayload>>

    @POST("accounts/users")
    fun createAccount(@Body body: Map<String, String>): Call<Payload<Int>>
}
