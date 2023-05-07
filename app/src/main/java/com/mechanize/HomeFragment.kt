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
import com.mechanize.databinding.FragmentHomeBinding

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

        val userSharedPreferences = EncryptedSharedPreferences.create(
            "user",
            BuildConfig.SHARED_PREF_KEY,
            binding.root.context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val accessToken = userSharedPreferences.getString("accessToken", "")
        val id = userSharedPreferences.getInt("id", 0)
        val name = userSharedPreferences.getString("name", "")
        val role = userSharedPreferences.getString("role", "")

        if(accessToken != "" && id != 0 && name != "" && role != ""){
            findNavController().navigate(R.id.action_HomeFragment_to_SearchFragment)

            return
        }

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
}
