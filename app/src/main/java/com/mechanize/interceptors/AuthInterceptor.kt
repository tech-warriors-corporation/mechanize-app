package com.mechanize.interceptors

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import com.mechanize.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val userSharedPreferences = EncryptedSharedPreferences.create(
            "user",
            BuildConfig.SHARED_PREF_KEY,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val newRequest = chain.request()
                              .newBuilder()
                              .header("Authorization", userSharedPreferences.getString("accessToken", "") as String)
                              .build()

        return chain.proceed(newRequest)
    }
}
