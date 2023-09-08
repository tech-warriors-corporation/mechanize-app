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

data class TicketPayload(
    @SerializedName("id") val id: Int,
    @SerializedName("driver_name") val driverName: String,
    @SerializedName("vehicle") val vehicle: String,
    @SerializedName("location") val location: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("google_maps_link") val googleMapsLink: String,
    @SerializedName("description") val description: String,
    @SerializedName("created_date") val createdDate: String,
)

data class CurrentTicketPayload(
    @SerializedName("id") val id: Int,
    @SerializedName("driver_name") val driverName: String,
    @SerializedName("vehicle") val vehicle: String,
    @SerializedName("location") val location: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("google_maps_link") val googleMapsLink: String,
    @SerializedName("description") val description: String,
    @SerializedName("created_date") val createdDate: String,
    @SerializedName("status") val status: String,
    @SerializedName("mechanic_id") val mechanicId: Int?,
    @SerializedName("mechanic_name") val mechanicName: String?,
)

data class TicketStatusPayload(
    @SerializedName("status") val status: String,
    @SerializedName("description") val description: String,
    @SerializedName("created_date") val createdDate: String,
    @SerializedName("mechanic_id") val mechanicId: Int?,
    @SerializedName("mechanic_name") val mechanicName: String?,
)

data class ChangePasswordPayload(
    @SerializedName("changed") val changed: Boolean,
    @SerializedName("data") val data: UserPayload?,
    @SerializedName("error_type") val errorType: String?,
)

data class Payload<T>(
    @SerializedName("payload") val payload: T? = null
)
