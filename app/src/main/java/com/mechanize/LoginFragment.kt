package com.mechanize

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import com.google.android.material.snackbar.Snackbar
import com.mechanize.databinding.FragmentLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

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
        _binding = null
    }

    private fun login(){
        val email = binding.emailField.editText?.text.toString()

        if(email == ""){
            Snackbar.make(binding.root, R.string.invalid_email, Snackbar.LENGTH_LONG).show()

            return
        }

        val password = binding.passwordField.editText?.text.toString()

        if(password == ""){
            Snackbar.make(binding.root, R.string.invalid_password, Snackbar.LENGTH_LONG).show()

            return
        }

        binding.loading.visibility = View.VISIBLE

        val body = mapOf(
            "email" to email,
            "password" to password
        )

        val call = RetrofitFactory().retrofitService().login(body)

        call.enqueue(object : Callback<Payload<LoginPayload>> {
            override fun onResponse(call: Call<Payload<LoginPayload>>, response: Response<Payload<LoginPayload>>) {
                response.body().let{
                    val payload = it?.payload

                    if(payload == null){
                        Snackbar.make(binding.root, R.string.invalid_login, Snackbar.LENGTH_LONG).show()

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
                        putString("name", payload.user.name)
                        putString("role", payload.user.role)
                        apply()
                    }

                    Snackbar.make(binding.root, R.string.login_success, Snackbar.LENGTH_LONG).show()

                    binding.loading.visibility = View.INVISIBLE

                    findNavController().navigate(R.id.action_LoginFragment_to_SearchFragment)
                }
            }

            override fun onFailure(call: Call<Payload<LoginPayload>>, throwable: Throwable) {
                Snackbar.make(binding.root, R.string.invalid_login, Snackbar.LENGTH_LONG).show()

                binding.loading.visibility = View.INVISIBLE
            }
        })
    }
}
