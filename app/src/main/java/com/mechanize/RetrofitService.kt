package com.mechanize

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RetrofitService {
    @Headers("Content-Type: application/json")
    @POST("accounts/login")
    fun login(@Body body: Map<String, String>): Call<Payload<LoginPayload>>
}
