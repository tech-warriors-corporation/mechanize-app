package com.mechanize

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.security.crypto.EncryptedSharedPreferences
import com.mechanize.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        verifyTimeToRetryLogin()
    }

    private fun verifyTimeToRetryLogin(){
        val timeToRetryLoginSharedPreferences = EncryptedSharedPreferences.create(
            "time_to_retry_login",
            BuildConfig.SHARED_PREF_KEY,
            binding.root.context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val timeToRetryLoginSharedPreferencesEditable = timeToRetryLoginSharedPreferences.edit()
        val resetTime = "0"
        var time = timeToRetryLoginSharedPreferences.getString("time", "") as String

        if(time == ""){
            with(timeToRetryLoginSharedPreferencesEditable){
                time = resetTime
                putString("time", time)
                apply()
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(1000L)

                time = timeToRetryLoginSharedPreferences.getString("time", resetTime) as String

                if(time != resetTime)
                    with(timeToRetryLoginSharedPreferencesEditable){
                        putString("time", (time.toInt() - 1).toString())
                        apply()
                    }
            }
        }
    }
}
