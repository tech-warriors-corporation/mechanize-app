package com.mechanize

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import com.google.android.material.snackbar.Snackbar
import com.mechanize.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed(){}
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkAuthUser()

        Notifications.init(binding.root.context)

        if(
            ActivityCompat.checkSelfPermission(binding.root.context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(binding.root.context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.shouldShowRequestPermissionRationale(binding.root.context as Activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(binding.root.context as Activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_LoginFragment)
        }

        binding.createAccountButton.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_CreateAccountFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkAuthUser(){
        val context = binding.root.context
        val userSharedPreferences = EncryptedSharedPreferences.create(
            "user",
            BuildConfig.SHARED_PREF_KEY,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val accessToken = userSharedPreferences.getString("accessToken", "")

        if(accessToken == "") return

        binding.globalContent.visibility = View.VISIBLE

        val call = RetrofitFactory().retrofitAccountsService(context).getUserByToken()

        call.enqueue(object : Callback<Payload<UserPayload>> {
            override fun onResponse(call: Call<Payload<UserPayload>>, response: Response<Payload<UserPayload>>) {
                response.body().let{
                    val payload = it?.payload

                    if(payload == null){
                        Snackbar.make(binding.root, R.string.invalid_auth_user, Snackbar.LENGTH_LONG).show()

                        with(userSharedPreferences.edit()){
                            remove("accessToken")
                            remove("id")
                            remove("name")
                            remove("role")
                            apply()
                        }

                        binding.globalContent.visibility = View.INVISIBLE

                        return
                    }

                    with(userSharedPreferences.edit()){
                        putString("accessToken", payload.accessToken)
                        putInt("id", payload.user.id)
                        putString("name", payload.user.name)
                        putString("role", payload.user.role)
                        apply()
                    }

                    val navController = findNavController()

                    navController.navigate(R.id.action_HomeFragment_to_SearchFragment)
                }
            }

            override fun onFailure(call: Call<Payload<UserPayload>>, throwable: Throwable) {
                Snackbar.make(binding.root, R.string.invalid_auth_user, Snackbar.LENGTH_LONG).show()
                binding.globalContent.visibility = View.INVISIBLE
            }
        })
    }
}
