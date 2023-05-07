package com.mechanize

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class SearchFragment : Fragment(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
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
                remove("name")
                remove("role")
                apply()
            }

            findNavController().navigate(R.id.action_SearchFragment_to_HomeFragment)
        }

        val userName = userSharedPreferences.getString("name", "")
        var userRole = userSharedPreferences.getString("role", "")
        var searchForText = ""

        if(userRole == RoleValue.DRIVER.value) {
            userRole = resources.getString(RoleText.DRIVER.value)
            searchForText = resources.getString(RoleText.MECHANIC.value).lowercase()
        } else if(userRole == RoleValue.MECHANIC.value) {
            userRole = resources.getString(RoleText.MECHANIC.value)
            searchForText = resources.getString(R.string.service)
        }

        binding.user.text = "$userRole: $userName"
        binding.searchButton.text = "${binding.searchButton.text} $searchForText"

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
            binding.actions.visibility = View.INVISIBLE
            Snackbar.make(binding.root, R.string.invalid_location, Snackbar.LENGTH_LONG).show()
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

    private fun updateLocation(map: GoogleMap, latLng: LatLng?){
        googleMap = map

        if(latLng == null) return

        googleMap.addMarker(
            MarkerOptions().position(latLng)
                           .title(resources.getString(R.string.me))
                           .snippet(resources.getString(R.string.my_location))
                           .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.5f))

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
}
