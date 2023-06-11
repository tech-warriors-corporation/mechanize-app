package com.mechanize

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import com.google.android.material.snackbar.Snackbar
import com.mechanize.databinding.FragmentLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.*

class LoginFragment : Fragment() {
    private var timeToRetryLoginSharedPreferences: SharedPreferences? = null
    private var timeToRetryLoginSharedPreferencesEditable: SharedPreferences.Editor? = null
    private var timeToRetryLoginJob: Job? = null
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        timeToRetryLoginSharedPreferences = EncryptedSharedPreferences.create(
            "time_to_retry_login",
            BuildConfig.SHARED_PREF_KEY,
            binding.root.context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        timeToRetryLoginSharedPreferencesEditable = timeToRetryLoginSharedPreferences!!.edit()

        watchTimeToRetryLoginJob()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.homeButton.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_HomeFragment)
        }

        binding.loginButton.setOnClickListener{
            login()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timeToRetryLoginJob?.cancel()
        _binding = null
    }

    private fun login(){
        val email = binding.emailField.editText?.text.toString()
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(binding.root.rootView.windowToken, 0)

        if(email == ""){
            Snackbar.make(binding.root, R.string.invalid_email, Snackbar.LENGTH_LONG).show()

            return
        }

        val password = binding.passwordField.editText?.text.toString()

        if(password == ""){
            Snackbar.make(binding.root, R.string.invalid_password, Snackbar.LENGTH_LONG).show()

            return
        }

        binding.loginButton.isEnabled = false
        binding.loading.visibility = View.VISIBLE

        val body = mapOf(
            "email" to email,
            "password" to password
        )

        val call = RetrofitFactory().retrofitAccountsService(binding.root.context).login(body)

        call.enqueue(object : Callback<Payload<LoginPayload>> {
            override fun onResponse(call: Call<Payload<LoginPayload>>, response: Response<Payload<LoginPayload>>) {
                response.body().let{
                    val payload = it?.payload

                    if(payload == null){
                        Snackbar.make(binding.root, R.string.invalid_login, Snackbar.LENGTH_LONG).show()

                        with(timeToRetryLoginSharedPreferencesEditable){
                            this?.putString("time", "10")
                            this?.apply()
                        }

                        binding.loginButton.isEnabled = true
                        binding.loading.visibility = View.INVISIBLE

                        return
                    }

                    val userSharedPreferences = EncryptedSharedPreferences.create(
                        "user",
                        BuildConfig.SHARED_PREF_KEY,
                        binding.root.context,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )

                    with(userSharedPreferences.edit()){
                        putString("accessToken", payload.accessToken)
                        putInt("id", payload.user.id)
                        putString("name", payload.user.name)
                        putString("role", payload.user.role)
                        apply()
                    }

                    Snackbar.make(binding.root, R.string.login_success, Snackbar.LENGTH_LONG).show()
                    binding.loginButton.isEnabled = true
                    binding.loading.visibility = View.INVISIBLE

                    findNavController().navigate(R.id.action_LoginFragment_to_SearchFragment)
                }
            }

            override fun onFailure(call: Call<Payload<LoginPayload>>, throwable: Throwable) {
                Snackbar.make(binding.root, R.string.invalid_login, Snackbar.LENGTH_LONG).show()
                binding.loginButton.isEnabled = true
                binding.loading.visibility = View.INVISIBLE
            }
        })
    }

    private fun watchTimeToRetryLoginJob(){
        val resetTime = "0"

        timeToRetryLoginJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(1000L)

                val time = timeToRetryLoginSharedPreferences?.getString("time", resetTime) as String
                val isEnabled = time == resetTime

                if(binding.loading.visibility == View.INVISIBLE)
                    binding.loginButton.isEnabled = isEnabled

                binding.timeToRetryLoginMessage.text = if(isEnabled) "" else getString(R.string.time_to_retry_login_message, time)
            }
        }
    }
}
