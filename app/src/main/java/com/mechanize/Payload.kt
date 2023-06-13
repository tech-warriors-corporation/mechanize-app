package com.mechanize

import com.google.gson.annotations.SerializedName

data class UserAccountPayload(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String
)

data class UserPayload(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("user") val user: UserAccountPayload
)

data class Payload<T>(
    @SerializedName("payload") val payload: T? = null
)
