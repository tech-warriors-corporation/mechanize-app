package com.mechanize

import com.google.gson.annotations.SerializedName

data class LoginUserPayload(
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String
)

data class LoginPayload(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("user") val user: LoginUserPayload
)

data class Payload<T>(
    @SerializedName("payload") val payload: T? = null
)
