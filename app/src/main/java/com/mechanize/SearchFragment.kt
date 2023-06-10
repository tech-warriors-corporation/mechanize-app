package com.mechanize

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.mechanize.databinding.FragmentSearchBinding
import com.mechanize.enums.RoleText
import com.mechanize.enums.RoleValue
import com.mechanize.enums.TicketStatusValue
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var searchForText = ""
    private var latLng: LatLng? = null
    private var userId: Int? = null
    private var googleApiClient: GoogleApiClient? = null
    private var locationManager: LocationManager? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap

    private val isLocationEnabled: Boolean
        get(){
            locationManager = binding.root.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true || locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

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

        binding.logoutButton.setOnClickListener {
            Snackbar.make(binding.root, R.string.logout_success, Snackbar.LENGTH_SHORT).show()

            with(userSharedPreferences.edit()){
                remove("accessToken")
                remove("id")
                remove("name")
                remove("role")
                apply()
            }

            findNavController().navigate(R.id.action_SearchFragment_to_HomeFragment)
        }

        binding.searchMechanic.closeButton.setOnClickListener{
            closeSearchMechanic()
        }

        userId = userSharedPreferences.getInt("id", 0)
        val userName = userSharedPreferences.getString("name", "")
        var userRole = userSharedPreferences.getString("role", "")
        val isDriver = userRole == RoleValue.DRIVER.value
        val isMechanic = userRole == RoleValue.MECHANIC.value

        if(isDriver) {
            userRole = resources.getString(RoleText.DRIVER.value)
            searchForText = resources.getString(RoleText.MECHANIC.value).lowercase()
        } else if(isMechanic) {
            userRole = resources.getString(RoleText.MECHANIC.value)
            searchForText = resources.getString(R.string.service)
        }

        binding.user.text = "$userRole: $userName"
        binding.searchButton.text = "${binding.searchButton.text} $searchForText"
        binding.searching.searchingLabel.text = "${binding.searching.searchingLabel.text} $searchForText"
        binding.searching.searchingDescriptionLabel.text = "${binding.searching.searchingDescriptionLabel.text} $searchForText."

        binding.searchButton.setOnClickListener{
            if(isDriver) onSearchMechanic()
            else if(isMechanic) onSearchService()
        }

        binding.searching.cancelButton.setOnClickListener{
            showAllActions()
            binding.searching.root.visibility = View.INVISIBLE
        }

        binding.searchMechanic.callButton.setOnClickListener{
            createTicket()
        }

        val supportMapFragment = childFragmentManager.findFragmentById(R.id.location) as SupportMapFragment

        supportMapFragment.getMapAsync(this)

        googleApiClient = GoogleApiClient.Builder(binding.root.context)
                                         .addConnectionCallbacks(this)
                                         .addOnConnectionFailedListener(this)
                                         .addApi(LocationServices.API)
                                         .build()

        locationCallback = object : LocationCallback(){}
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(binding.root.context)

        checkLocation()
        startLocationUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()

        googleApiClient?.connect()
    }

    override fun onConnectionSuspended(p0: Int) {
        googleApiClient?.connect()
    }

    override fun onConnected(bundle: Bundle?) {
        if(
            ActivityCompat.checkSelfPermission(binding.root.context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(binding.root.context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Handler().postDelayed({
                Snackbar.make(binding.root, R.string.invalid_location, Snackbar.LENGTH_LONG).show()
            }, 1000)

            binding.actions.visibility = View.INVISIBLE
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    override fun onLocationChanged(location: Location) {}

    override fun onMapReady(map: GoogleMap) {
        updateLocation(map, null)
    }

    private fun checkLocation(): Boolean{
        return isLocationEnabled
    }

    private fun startLocationUpdates(){
        locationRequest = LocationRequest.create()
                                         .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                         .setInterval(20000)
                                         .setFastestInterval(2000)

        if(
            ActivityCompat.checkSelfPermission(binding.root.context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(binding.root.context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        fusedLocationClient.lastLocation.addOnSuccessListener{ location: Location? ->
            location?.let{
                updateLocation(googleMap, LatLng(location.latitude, location.longitude))
            }
        }
    }

    private fun updateLocation(map: GoogleMap, newLatLng: LatLng?){
        googleMap = map

        if(newLatLng == null) return

        latLng = newLatLng

        googleMap.addMarker(
            MarkerOptions().position(latLng!!)
                           .title(resources.getString(R.string.me))
                           .snippet(resources.getString(R.string.my_location))
                           .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng!!, 12.5f))

        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                val info = LinearLayout(binding.root.context.applicationContext)

                info.orientation = LinearLayout.VERTICAL

                val title = TextView(binding.root.context.applicationContext)

                title.setTextColor(resources.getColor(R.color.black))
                title.gravity = Gravity.START
                title.setTypeface(null, Typeface.BOLD)
                title.text = marker.title

                val snippet = TextView(binding.root.context.applicationContext)

                snippet.setTextColor(resources.getColor(R.color.black))
                snippet.text = marker.snippet

                info.addView(title)
                info.addView(snippet)

                return info
            }
        })
    }

    private fun onSearchMechanic(){
        closeSearchMechanic()
        binding.searchMechanic.root.visibility = View.VISIBLE
        hideAllActions()
    }

    private fun closeSearchMechanic(){
        showAllActions()
        binding.searchMechanic.root.visibility = View.INVISIBLE
        binding.searchMechanic.glassBroke.isChecked = false
        binding.searchMechanic.vehicleWithoutBattery.isChecked = false
        binding.searchMechanic.outOfFuel.isChecked = false
        binding.searchMechanic.vehicleField.editText?.setText("")
        binding.searchMechanic.problemField.editText?.setText("")
    }

    private fun onSearchService(){
        binding.searching.root.visibility = View.VISIBLE
        hideAllActions()
    }

    private fun hideAllActions(){
        binding.actions.visibility = View.INVISIBLE
        binding.logoutButton.visibility = View.INVISIBLE
    }

    private fun showAllActions(){
        binding.actions.visibility = View.VISIBLE
        binding.logoutButton.visibility = View.VISIBLE
    }

    private fun createTicket(){
        val view = binding.searchMechanic
        val glassBroke = view.glassBroke.isChecked
        val vehicleWithoutBattery = view.vehicleWithoutBattery.isChecked
        val outOfFuel = view.outOfFuel.isChecked
        val vehicle = view.vehicleField.editText?.text.toString()
        var problem = view.problemField.editText?.text.toString()
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(binding.root.rootView.windowToken, 0)

        if(vehicle == ""){
            Snackbar.make(binding.root, R.string.invalid_vehicle, Snackbar.LENGTH_LONG).show()
            return
        }

        if(problem == ""){
            Snackbar.make(binding.root, R.string.invalid_problem, Snackbar.LENGTH_LONG).show()
            return
        }

        if(glassBroke) problem = "$problem | ${view.glassBroke.text}"
        if(vehicleWithoutBattery) problem = "$problem | ${view.vehicleWithoutBattery.text}"
        if(outOfFuel) problem = "$problem | ${view.outOfFuel.text}"

        binding.searchMechanic.callButton.isEnabled = false
        binding.searchMechanic.ticketLoading.visibility = View.VISIBLE

        val body = mapOf(
            "driver_id" to userId.toString(),
            "vehicle" to vehicle,
            "description" to problem,
            "location" to "${latLng!!.latitude},${latLng!!.longitude}",
            "status" to TicketStatusValue.UNSOLVED.value,
        )

        val call = RetrofitFactory().retrofitHelpsService(binding.root.context).createTicket(body)

        call.enqueue(object : Callback<Payload<Int>> {
            override fun onResponse(call: Call<Payload<Int>>, response: Response<Payload<Int>>) {
                response.body().let{
                    val payload = it?.payload

                    if(payload == null){
                        Snackbar.make(binding.root, R.string.invalid_create_ticket, Snackbar.LENGTH_LONG).show()
                        binding.searchMechanic.callButton.isEnabled = true
                        binding.searchMechanic.ticketLoading.visibility = View.INVISIBLE

                        return
                    }

                    Snackbar.make(binding.root, R.string.created_ticket, Snackbar.LENGTH_LONG).show()

                    closeSearchMechanic()
                    onSearchService()

                    binding.searchMechanic.callButton.isEnabled = true
                    binding.searchMechanic.ticketLoading.visibility = View.INVISIBLE
                }
            }

            override fun onFailure(call: Call<Payload<Int>>, throwable: Throwable) {
                Snackbar.make(binding.root, R.string.invalid_create_ticket, Snackbar.LENGTH_LONG).show()
                binding.searchMechanic.callButton.isEnabled = true
                binding.searchMechanic.ticketLoading.visibility = View.INVISIBLE
            }
        })
    }
}
