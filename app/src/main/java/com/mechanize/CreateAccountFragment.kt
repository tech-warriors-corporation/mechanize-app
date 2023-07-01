package com.mechanize

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import com.google.android.material.snackbar.Snackbar
import com.mechanize.databinding.FragmentCreateAccountBinding
import com.mechanize.enums.RoleText
import com.mechanize.enums.RoleValue
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateAccountFragment : Fragment() {
    private var isCreateAccountCallCancelled = false
    private var createAccountCall: Call<Payload<Int>>? = null
    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ArrayAdapter(binding.root.context, android.R.layout.select_dialog_item, resources.getStringArray(R.array.roles))
        val roleAutoComplete = binding.root.findViewById<AutoCompleteTextView>(R.id.role_autocomplete)

        roleAutoComplete.setAdapter(adapter)
        roleAutoComplete.setText(roleAutoComplete.adapter.getItem(0).toString(), false)

        binding.homeButton.setOnClickListener {
            findNavController().navigate(R.id.action_CreateAccountFragment_to_HomeFragment)
        }

        binding.loginButton.setOnClickListener{
            createAccount()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isCreateAccountCallCancelled = true
        createAccountCall?.cancel()
        _binding = null
    }

    private fun createAccount(){
        val name = binding.nameField.editText?.text.toString()
        var role = binding.roleField.editText?.text.toString()
        val email = binding.emailField.editText?.text.toString()
        val password = binding.passwordField.editText?.text.toString()
        val passwordConfirmation = binding.passwordConfirmationField.editText?.text.toString()
        val acceptSaveData = binding.acceptSaveData.isChecked
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(binding.root.rootView.windowToken, 0)

        if(name == ""){
            Snackbar.make(binding.root, R.string.invalid_name, Snackbar.LENGTH_LONG).show()
            return
        }

        if(role == ""){
            Snackbar.make(binding.root, R.string.invalid_role, Snackbar.LENGTH_LONG).show()
            return
        }

        if(email == ""){
            Snackbar.make(binding.root, R.string.invalid_email, Snackbar.LENGTH_LONG).show()
            return
        }

        if(password == ""){
            Snackbar.make(binding.root, R.string.invalid_password, Snackbar.LENGTH_LONG).show()
            return
        }

        if(passwordConfirmation == ""){
            Snackbar.make(binding.root, R.string.invalid_password_confirmation, Snackbar.LENGTH_LONG).show()
            return
        }

        if(password != passwordConfirmation){
            Snackbar.make(binding.root, R.string.invalid_passwords, Snackbar.LENGTH_LONG).show()
            return
        }

        if(!acceptSaveData){
            Snackbar.make(binding.root, R.string.you_need_to_accept_save_data, Snackbar.LENGTH_LONG).show()
            return
        }

        binding.loginButton.isEnabled = false
        binding.loading.visibility = View.VISIBLE

        if(role == resources.getString(RoleText.DRIVER.value)) role = RoleValue.DRIVER.value
        else if(role == resources.getString(RoleText.MECHANIC.value)) role = RoleValue.MECHANIC.value

        val body = mapOf(
            "name" to name,
            "role" to role,
            "email" to email,
            "password" to password,
        )

        createAccountCall = RetrofitFactory().retrofitAccountsService(binding.root.context).createAccount(body)

        createAccountCall?.enqueue(object : Callback<Payload<Int>> {
            override fun onResponse(call: Call<Payload<Int>>, response: Response<Payload<Int>>) {
                if (isCreateAccountCallCancelled) return

                response.body().let{
                    val id = it?.payload

                    if(id == null){
                        Snackbar.make(binding.root, R.string.invalid_create_account, Snackbar.LENGTH_LONG).show()
                        binding.loginButton.isEnabled = true
                        binding.loading.visibility = View.INVISIBLE

                        return
                    }

                    login(email, password)
                }
            }

            override fun onFailure(call: Call<Payload<Int>>, throwable: Throwable) {
                if (isCreateAccountCallCancelled) return

                Snackbar.make(binding.root, R.string.invalid_create_account, Snackbar.LENGTH_LONG).show()
                binding.loginButton.isEnabled = true
                binding.loading.visibility = View.INVISIBLE
            }
        })
    }

    private fun login(email: String, password: String){
        val body = mapOf(
            "email" to email,
            "password" to password,
        )

        val call = RetrofitFactory().retrofitAccountsService(binding.root.context).login(body)

        call.enqueue(object : Callback<Payload<UserPayload>> {
            override fun onResponse(call: Call<Payload<UserPayload>>, response: Response<Payload<UserPayload>>) {
                response.body().let{
                    val payload = it?.payload

                    if(payload == null){
                        Snackbar.make(binding.root, R.string.invalid_create_account, Snackbar.LENGTH_LONG).show()
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

                    binding.loginButton.isEnabled = true
                    binding.loading.visibility = View.INVISIBLE

                    findNavController().navigate(R.id.action_CreateAccountFragment_to_SearchFragment)
                }
            }

            override fun onFailure(call: Call<Payload<UserPayload>>, throwable: Throwable) {
                Snackbar.make(binding.root, R.string.invalid_create_account, Snackbar.LENGTH_LONG).show()
                binding.loginButton.isEnabled = true
                binding.loading.visibility = View.INVISIBLE
            }
        })
    }
}
