package com.mechanize

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Location
import android.location.LocationManager
import android.net.Uri
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
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val availableTickets = ArrayList<TicketPayload>()
    private var currentTicket: TicketPayload? = null
    private var indexAvailableTickets = 0
    private var searchForText = ""
    private var latLng: LatLng? = null
    private var driverLatLng: LatLng? = null
    private var userId: Int? = null
    private var isDriver = false
    private var isMechanic = false
    private var ticketId: Int? = null
    private var cachedTicketId: Int? = null
    private var mechanicId: Int? = null
    private var mechanicName: String? = null
    private var description: String? = null
    private var createdDate: String? = null
    private var rating = 5
    private var searchingJob: Job? = null
    private var ticketStatusJob: Job? = null
    private var googleApiClient: GoogleApiClient? = null
    private var locationManager: LocationManager? = null
    private var userSharedPreferences: SharedPreferences? = null
    private var hasLocation = true
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

        userSharedPreferences = EncryptedSharedPreferences.create(
            "user",
            BuildConfig.SHARED_PREF_KEY,
            binding.root.context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        binding.logoutButton.setOnClickListener {
            logout()
        }

        binding.focusToMeButton.setOnClickListener {
            if(isMechanic && ticketId != null) focus(driverLatLng!!)
            else focus()
        }

        binding.searchMechanic.closeButton.setOnClickListener{
            closeSearchMechanic()
        }

        binding.ratingTicket.closeButton.setOnClickListener{
            closeRatingTicket()
        }

        userId = userSharedPreferences!!.getInt("id", 0)
        val userName = userSharedPreferences!!.getString("name", "")
        var userRole = userSharedPreferences!!.getString("role", "")
        isDriver = userRole == RoleValue.DRIVER.value
        isMechanic = userRole == RoleValue.MECHANIC.value

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

        binding.cancelButton.setOnClickListener{
            cancelTicket()
        }

        binding.searching.cancelButton.setOnClickListener{
            cancelTicket()
        }

        binding.availableTickets.cancelButton.setOnClickListener{
            cancelChooseOfTicket()
        }

        binding.searchMechanic.callButton.setOnClickListener{
            createTicket()
        }

        binding.availableTickets.attendService.setOnClickListener{
            acceptTicket()
        }

        binding.availableTickets.nextService.setOnClickListener{
            indexAvailableTickets++
            setAvailableTicketsLayout()
        }

        binding.availableTickets.previousService.setOnClickListener{
            indexAvailableTickets--
            setAvailableTicketsLayout()
        }

        binding.availableTickets.googleMapsLink.setOnClickListener{
            openGoogleMapsLink()
        }

        binding.currentTicket.googleMapsLink.setOnClickListener{
            openGoogleMapsLink()
        }

        binding.concludeButton.setOnClickListener{
            concludeTicket()
        }

        binding.ratingTicket.starOne.setOnClickListener{
            rating = 1
            setRatingColors()
        }

        binding.ratingTicket.starTwo.setOnClickListener{
            rating = 2
            setRatingColors()
        }

        binding.ratingTicket.starThree.setOnClickListener{
            rating = 3
            setRatingColors()
        }

        binding.ratingTicket.starFour.setOnClickListener{
            rating = 4
            setRatingColors()
        }

        binding.ratingTicket.starFive.setOnClickListener{
            rating = 5
            setRatingColors()
        }

        binding.ratingTicket.ratingButton.setOnClickListener{
            sendRating()
        }

        binding.openPrivacyPolicyModal.setOnClickListener{
            binding.privacyPolicyModal.root.visibility = View.VISIBLE
        }

        binding.privacyPolicyModal.closeButton.setOnClickListener{
            binding.privacyPolicyModal.root.visibility = View.INVISIBLE
        }

        binding.privacyPolicyModal.openDeveloperLink.setOnClickListener{
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.DEVELOPER_LINK)))
        }

        binding.privacyPolicyModal.openManualPdf.setOnClickListener{
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.MANUAL_PDF)))
        }

        gettingCurrentTicket()

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
        cancelTicketStatus()
        cancelSearching()
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
            hasLocation = false

            Handler().postDelayed({
                Snackbar.make(binding.root, R.string.invalid_location, Snackbar.LENGTH_LONG).top()
            }, 1000)

            binding.actions.visibility = View.INVISIBLE
            binding.focusToMeButton.visibility = View.INVISIBLE
            binding.currentTicket.root.visibility = View.INVISIBLE
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
                latLng = LatLng(location.latitude, location.longitude)

                updateLocation(googleMap, latLng)
            }
        }
    }

    private fun updateLocation(
        map: GoogleMap,
        newLatLng: LatLng?,
        markerTitle: String? = resources.getString(R.string.me),
        markerSnippet: String? = resources.getString(R.string.my_location),
    ){
        googleMap = map

        if(newLatLng == null) return

        googleMap.clear()

        googleMap.addMarker(
            MarkerOptions().position(newLatLng)
                           .title(markerTitle)
                           .snippet(markerSnippet)
                           .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        focus(newLatLng)

        googleMap.uiSettings.isRotateGesturesEnabled = false

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
        closeFieldsSearchMechanic()
        binding.searchMechanic.root.visibility = View.VISIBLE
    }

    private fun closeFieldsSearchMechanic(){
        binding.searchMechanic.glassBroke.isChecked = false
        binding.searchMechanic.vehicleWithoutBattery.isChecked = false
        binding.searchMechanic.outOfFuel.isChecked = false
        binding.searchMechanic.vehicleField.editText?.setText("")
        binding.searchMechanic.problemField.editText?.setText("")

        closeKeyboard()
    }

    private fun closeSearchMechanic(){
        closeFieldsSearchMechanic()

        Handler().postDelayed({
            binding.searchMechanic.root.visibility = View.INVISIBLE
        }, 0)
    }

    private fun onSearchService(){
        binding.searching.root.visibility = View.VISIBLE

        if(isDriver) watchTicketStatus()
        else if(isMechanic) getAvailableTickets()
    }

    private fun createTicket(){
        val view = binding.searchMechanic
        val glassBroke = view.glassBroke.isChecked
        val vehicleWithoutBattery = view.vehicleWithoutBattery.isChecked
        val outOfFuel = view.outOfFuel.isChecked
        val vehicle = view.vehicleField.editText?.text.toString()
        var problem = view.problemField.editText?.text.toString()

        closeKeyboard()

        if(vehicle == ""){
            Snackbar.make(binding.root, R.string.invalid_vehicle, Snackbar.LENGTH_LONG).top()
            return
        }

        if(problem == ""){
            Snackbar.make(binding.root, R.string.invalid_problem, Snackbar.LENGTH_LONG).top()
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
                        Snackbar.make(binding.root, R.string.invalid_create_ticket, Snackbar.LENGTH_LONG).top()
                        binding.searchMechanic.callButton.isEnabled = true
                        binding.searchMechanic.ticketLoading.visibility = View.INVISIBLE

                        return
                    }

                    ticketId = payload

                    Snackbar.make(binding.root, R.string.created_ticket, Snackbar.LENGTH_LONG).top()

                    closeSearchMechanic()
                    onSearchService()

                    binding.searchMechanic.callButton.isEnabled = true
                    binding.searchMechanic.ticketLoading.visibility = View.INVISIBLE
                }
            }

            override fun onFailure(call: Call<Payload<Int>>, throwable: Throwable) {
                Snackbar.make(binding.root, R.string.invalid_create_ticket, Snackbar.LENGTH_LONG).top()
                binding.searchMechanic.callButton.isEnabled = true
                binding.searchMechanic.ticketLoading.visibility = View.INVISIBLE
            }
        })
    }

    private fun focus(latLon: LatLng = latLng!!){
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLon, 15f))
    }

    private fun cancelTicket(){
        if(isMechanic && isNotRunning()){
            cancelSearching()
            cancelChooseOfTicket()

            return
        }

        val canShowFinishingScreen = !isNotRunning() && binding.ticketActions.visibility == View.VISIBLE

        if(canShowFinishingScreen) showFinishingScreen(R.string.cancelling_title, R.string.cancelling_text)
        else binding.searching.cancelButton.visibility = View.INVISIBLE

        val call = RetrofitFactory().retrofitHelpsService(binding.root.context).cancelTicket(ticketId as Int)

        call.enqueue(object : Callback<Payload<Int>> {
            override fun onResponse(call: Call<Payload<Int>>, response: Response<Payload<Int>>) {
                response.body().let{
                    val payload = it?.payload

                    hideFinishingScreen()
                    binding.searching.cancelButton.visibility = View.VISIBLE

                    if(payload == null){
                        Snackbar.make(binding.root, R.string.error_to_cancel_ticket, Snackbar.LENGTH_LONG).top()
                        return
                    }

                    if(canShowFinishingScreen) Snackbar.make(binding.root, R.string.cancelled_ticket, Snackbar.LENGTH_LONG).top()

                    rollbackToYourVision(true)
                }
            }

            override fun onFailure(call: Call<Payload<Int>>, throwable: Throwable) {
                hideFinishingScreen()
                binding.searching.cancelButton.visibility = View.VISIBLE
                Snackbar.make(binding.root, R.string.error_to_cancel_ticket, Snackbar.LENGTH_LONG).top()
            }
        })
    }

    private fun cancelSearching(){
        binding.searching.root.visibility = View.INVISIBLE

        hideTicketContents()

        searchingJob?.cancel()
    }

    private fun hideTicketContents(){
        binding.currentTicket.root.visibility = View.INVISIBLE
        binding.ticketActions.visibility = View.INVISIBLE
        binding.searchButton.visibility = View.VISIBLE
    }

    private fun cancelChooseOfTicket(){
        hideAvailableTickets()
        currentTicket = null
        ticketId = null
    }

    private fun hideAvailableTickets(){
        binding.availableTickets.root.visibility = View.INVISIBLE
        availableTickets.clear()
        indexAvailableTickets = 0
    }

    private fun gettingCurrentTicket(){
        binding.gettingCurrentTicket.root.visibility = View.VISIBLE

        val call = RetrofitFactory().retrofitHelpsService(binding.root.context).getCurrentTicket()

        call.enqueue(object : Callback<Payload<CurrentTicketPayload>> {
            override fun onResponse(call: Call<Payload<CurrentTicketPayload>>, response: Response<Payload<CurrentTicketPayload>>) {
                response.body().let{
                    val payload = it?.payload

                    binding.gettingCurrentTicket.root.visibility = View.INVISIBLE

                    if(payload == null) return

                    ticketId = payload.id

                    if(isDriver) {
                        val status = payload.status
                        val isCancelled = status == TicketStatusValue.CANCELLED.value
                        val isConcluded = status == TicketStatusValue.SOLVED.value

                        initMechanic(
                            payload.mechanicId,
                            payload.mechanicName,
                            payload.description,
                            payload.createdDate
                        )

                        if(isCancelled || isConcluded) {
                            rollbackToYourVision(true)

                            return
                        }
                    } else {
                        currentTicket = TicketPayload(
                            id = ticketId!!,
                            driverName = payload.driverName,
                            vehicle = payload.vehicle,
                            location = payload.location,
                            lat = payload.lat,
                            lon = payload.lon,
                            googleMapsLink = payload.googleMapsLink,
                            description = payload.description,
                            createdDate = payload.createdDate
                        )

                        setCurrentTicketLayout()
                    }

                    watchTicketStatus(0L)
                }
            }

            override fun onFailure(call: Call<Payload<CurrentTicketPayload>>, throwable: Throwable) {
                binding.gettingCurrentTicket.root.visibility = View.INVISIBLE

                Handler().postDelayed({
                    logout()

                    Snackbar.make(binding.root, R.string.error_on_get_current_ticket, Snackbar.LENGTH_LONG).top()
                }, 100)
            }
        })
    }

    private fun isNotSearching(): Boolean {
        return binding.searching.root.visibility == View.INVISIBLE
    }

    private fun isNotRunning(): Boolean {
        return ticketId == null
    }

    private fun watchTicketStatus(ms: Long = 2000L){
        if(isNotRunning()) return

        ticketStatusJob = CoroutineScope(Dispatchers.Main).launch {
            delay(ms)

            val call = RetrofitFactory().retrofitHelpsService(binding.root.context).getTicketStatus(ticketId!!)

            call.enqueue(object : Callback<Payload<TicketStatusPayload>> {
                override fun onResponse(call: Call<Payload<TicketStatusPayload>>, response: Response<Payload<TicketStatusPayload>>) {
                    response.body().let{
                        if(isNotRunning()) return

                        val payload = it?.payload

                        if(payload != null){
                            val status = payload.status
                            val isCancelled = status == TicketStatusValue.CANCELLED.value
                            val isConcluded = status == TicketStatusValue.SOLVED.value
                            val newMechanicId = payload.mechanicId

                            if(isCancelled || isConcluded){
                                if(isCancelled) {
                                    Notifications.create(binding.root.context, getString(R.string.notification_service_cancelled_title), getString(R.string.notification_service_cancelled_message))
                                    Snackbar.make(binding.root, R.string.service_was_cancelled, Snackbar.LENGTH_LONG).top()
                                } else {
                                    Notifications.create(binding.root.context, getString(R.string.notification_service_concluded_title), getString(R.string.notification_service_concluded_message))
                                    Snackbar.make(binding.root, R.string.service_was_concluded, Snackbar.LENGTH_LONG).top()
                                }

                                rollbackToYourVision(isCancelled)
                            } else if(isDriver){
                                if(newMechanicId != mechanicId && newMechanicId != null){
                                    cancelSearching()

                                    initMechanic(
                                        newMechanicId,
                                        payload.mechanicName,
                                        payload.description,
                                        payload.createdDate
                                    )

                                    Notifications.create(binding.root.context, getString(R.string.notification_mechanic_found_title), getString(R.string.notification_mechanic_found_message))
                                    Snackbar.make(binding.root, R.string.mechanic_was_found, Snackbar.LENGTH_LONG).top()
                                } else if(newMechanicId == null && isNotSearching()) {
                                    if(mechanicId != null) {
                                        Notifications.create(binding.root.context, getString(R.string.notification_mechanic_cancelled_title), getString(R.string.notification_mechanic_cancelled_message))
                                        Snackbar.make(binding.root, R.string.back_search_because_mechanic_cancelled, Snackbar.LENGTH_LONG).top()
                                    } else Snackbar.make(binding.root, R.string.back_to_search_mechanic, Snackbar.LENGTH_LONG).top()

                                    rollbackToYourVision(true, true)
                                    onSearchService()

                                    return
                                }
                            }
                        }

                        watchTicketStatus()
                    }
                }

                override fun onFailure(call: Call<Payload<TicketStatusPayload>>, throwable: Throwable) {
                    if(isNotRunning()) return

                    watchTicketStatus()
                }
            })
        }
    }

    private fun initMechanic(newMechanicId: Int?, newMechanicName: String?, newDescription: String?, newCreatedDate: String?){
        mechanicId = newMechanicId
        mechanicName = newMechanicName
        description = newDescription
        createdDate = newCreatedDate

        if(!hasLocation || mechanicId == null) return

        showRunningContents()

        binding.waitingMechanic.mechanicMessage.text = getString(R.string.waiting_mechanic).format(mechanicName)
        binding.waitingMechanic.description.text = getString(R.string.ticket_description).format(description)
        binding.waitingMechanic.createdDate.text = getString(R.string.ticket_created_date).format(createdDate)
        binding.waitingMechanic.root.visibility = View.VISIBLE
    }

    private fun showRunningContents(){
        binding.ticketActions.visibility = View.VISIBLE
        binding.searchButton.visibility = View.INVISIBLE
    }

    private fun cancelTicketStatus(){
        ticketStatusJob?.cancel()
    }

    private fun setAvailableTicketsLayout(){
        currentTicket = availableTickets[indexAvailableTickets]
        ticketId = currentTicket!!.id

        val position = indexAvailableTickets + 1
        val size = availableTickets.size

        setCurrentTicketTexts()

        binding.availableTickets.availableTicketsTitle.text = getString(R.string.ticket_title).format(position, size)
        binding.availableTickets.availableTicketsLocation.text = getString(R.string.ticket_location).format(currentTicket!!.location)

        if(position != size){
            binding.availableTickets.nextService.isEnabled = true
            binding.availableTickets.nextService.alpha = 1F
        } else{
            binding.availableTickets.nextService.isEnabled = false
            binding.availableTickets.nextService.alpha = 0.5F
        }

        if(position != 1){
            binding.availableTickets.previousService.isEnabled = true
            binding.availableTickets.previousService.alpha = 1F
        } else{
            binding.availableTickets.previousService.isEnabled = false
            binding.availableTickets.previousService.alpha = 0.5F
        }

        binding.availableTickets.changeServiceActions.visibility = if(size > 1) View.VISIBLE else View.INVISIBLE
        binding.availableTickets.root.visibility = View.VISIBLE
    }

    private fun setCurrentTicketTexts(){
        val driverName = getString(R.string.ticket_driver_name).format(currentTicket!!.driverName)
        val vehicle = getString(R.string.ticket_vehicle).format(currentTicket!!.vehicle)
        val description = getString(R.string.ticket_description).format(currentTicket!!.description)
        val createdDate = getString(R.string.ticket_created_date).format(currentTicket!!.createdDate)

        binding.availableTickets.availableTicketsDriverName.text = driverName
        binding.availableTickets.availableTicketsVehicle.text = vehicle
        binding.availableTickets.availableTicketsDescription.text = description
        binding.availableTickets.availableTicketsCreatedDate.text = createdDate
        binding.currentTicket.driverName.text = driverName
        binding.currentTicket.vehicle.text = vehicle
        binding.currentTicket.description.text = description
        binding.currentTicket.createdDate.text = createdDate
    }

    private fun getAvailableTickets(){
        if(isNotSearching()) return

        searchingJob = CoroutineScope(Dispatchers.Main).launch {
            delay(2000L)

            val call = RetrofitFactory().retrofitHelpsService(binding.root.context).getAvailableTickets()

            call.enqueue(object : Callback<Payload<List<TicketPayload>>> {
                override fun onResponse(call: Call<Payload<List<TicketPayload>>>, response: Response<Payload<List<TicketPayload>>>) {
                    response.body().let{
                        if(isNotSearching()) return

                        val payload = it?.payload

                        if(payload.isNullOrEmpty()){
                            getAvailableTickets()
                            return
                        }

                        availableTickets.clear()
                        availableTickets.addAll(payload)

                        cancelSearching()
                        setAvailableTicketsLayout()
                    }
                }

                override fun onFailure(call: Call<Payload<List<TicketPayload>>>, throwable: Throwable) {
                    if(isNotSearching()) return

                    getAvailableTickets()
                }
            })
        }
    }

    private fun closeKeyboard(){
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(binding.root.rootView.windowToken, 0)
    }

    private fun resetAvailableTicketsActions(){
        binding.availableTickets.attendService.visibility = View.VISIBLE
        binding.availableTickets.changeServiceActions.visibility = View.VISIBLE
        binding.availableTickets.cancelButton.visibility = View.VISIBLE
        binding.availableTickets.acceptingTicketLoading.visibility = View.INVISIBLE
    }

    private fun acceptTicket(){
        binding.availableTickets.attendService.visibility = View.INVISIBLE
        binding.availableTickets.changeServiceActions.visibility = View.INVISIBLE
        binding.availableTickets.cancelButton.visibility = View.INVISIBLE
        binding.availableTickets.acceptingTicketLoading.visibility = View.VISIBLE

        val call = RetrofitFactory().retrofitHelpsService(binding.root.context).acceptTicket(ticketId as Int)

        call.enqueue(object : Callback<Payload<Int>> {
            override fun onResponse(call: Call<Payload<Int>>, response: Response<Payload<Int>>) {
                response.body().let{
                    val payload = it?.payload

                    resetAvailableTicketsActions()

                    if(payload == null){
                        Snackbar.make(binding.root, R.string.error_to_accept_ticket, Snackbar.LENGTH_LONG).top()
                        return
                    }

                    setCurrentTicketLayout()
                    hideAvailableTickets()
                    watchTicketStatus()

                    Snackbar.make(binding.root, R.string.success_to_accept_ticket, Snackbar.LENGTH_LONG).top()
                }
            }

            override fun onFailure(call: Call<Payload<Int>>, throwable: Throwable) {
                Snackbar.make(binding.root, R.string.error_to_accept_ticket, Snackbar.LENGTH_LONG).top()
                resetAvailableTicketsActions()
            }
        })
    }

    private fun setCurrentTicketLayout(){
        if(!hasLocation) return

        setCurrentTicketTexts()
        showRunningContents()

        binding.currentTicket.root.visibility = View.VISIBLE
        driverLatLng = LatLng(currentTicket!!.lat, currentTicket!!.lon)

        updateLocation(googleMap, driverLatLng, currentTicket!!.driverName, resources.getString(R.string.driver_location))
    }

    private fun openGoogleMapsLink(){
        val intent = Intent(Intent.ACTION_VIEW)

        intent.data = Uri.parse(currentTicket!!.googleMapsLink)

        startActivity(intent)
    }

    private fun rollbackToYourVision(skipShowRatingScreen: Boolean = false, skipCancelChooseOfTicket: Boolean = false){
        if(!skipShowRatingScreen) showRatingScreen()

        updateLocation(googleMap, latLng)
        hideTicketContents()

        binding.waitingMechanic.root.visibility = View.INVISIBLE
        driverLatLng = null
        mechanicId = null
        mechanicName = null
        description = null
        createdDate = null

        cancelTicketStatus()
        cancelSearching()

        if(!skipCancelChooseOfTicket) cancelChooseOfTicket()
    }

    private fun concludeTicket(){
        showFinishingScreen(R.string.concluding_title, R.string.concluding_text)

        val call = RetrofitFactory().retrofitHelpsService(binding.root.context).concludeTicket(ticketId as Int)

        call.enqueue(object : Callback<Payload<Int>> {
            override fun onResponse(call: Call<Payload<Int>>, response: Response<Payload<Int>>) {
                response.body().let{
                    val payload = it?.payload

                    hideFinishingScreen()

                    if(payload == null){
                        Snackbar.make(binding.root, R.string.error_to_conclude_ticket, Snackbar.LENGTH_LONG).top()
                        return
                    }

                    rollbackToYourVision()

                    Snackbar.make(binding.root, R.string.concluded_ticket, Snackbar.LENGTH_LONG).top()
                }
            }

            override fun onFailure(call: Call<Payload<Int>>, throwable: Throwable) {
                hideFinishingScreen()

                Snackbar.make(binding.root, R.string.error_to_conclude_ticket, Snackbar.LENGTH_LONG).top()
            }
        })
    }

    private fun showFinishingScreen(title: Int, text: Int){
        binding.finishing.title.text = getString(title)
        binding.finishing.text.text = getString(text)
        binding.finishing.root.visibility = View.VISIBLE
    }

    private fun hideFinishingScreen(){
        binding.finishing.root.visibility = View.INVISIBLE
    }

    private fun showRatingScreen(){
        if(!isDriver || mechanicId == null || !hasLocation) return

        cachedTicketId = ticketId
        rating = 5

        setRatingColors()

        binding.ratingTicket.mechanic.text = getString(R.string.ticket_mechanic).format(mechanicName)
        binding.ratingTicket.description.text = getString(R.string.ticket_description).format(description)
        binding.ratingTicket.createdDate.text = getString(R.string.ticket_created_date).format(createdDate)
        binding.ratingTicket.root.visibility = View.VISIBLE
    }

    private fun setRatingColors(){
        val yellowColor = ContextCompat.getColorStateList(binding.root.context, R.color.yellow)
        val lightGrayColor = ContextCompat.getColorStateList(binding.root.context, R.color.light_gray)

        binding.ratingTicket.starOne.iconTint = if (rating >= 1) yellowColor else lightGrayColor
        binding.ratingTicket.starTwo.iconTint = if (rating >= 2) yellowColor else lightGrayColor
        binding.ratingTicket.starThree.iconTint = if (rating >= 3) yellowColor else lightGrayColor
        binding.ratingTicket.starFour.iconTint = if (rating >= 4) yellowColor else lightGrayColor
        binding.ratingTicket.starFive.iconTint = if (rating >= 5) yellowColor else lightGrayColor
    }

    private fun sendRating(){
        binding.ratingTicket.ratingButton.isEnabled = false
        binding.ratingTicket.ratingLoading.visibility = View.VISIBLE

        val body = mapOf("rating" to rating)
        val call = RetrofitFactory().retrofitHelpsService(binding.root.context).ratingTicket(cachedTicketId as Int, body)

        call.enqueue(object : Callback<Payload<Int>> {
            override fun onResponse(call: Call<Payload<Int>>, response: Response<Payload<Int>>) {
                response.body().let{
                    val payload = it?.payload

                    binding.ratingTicket.ratingButton.isEnabled = true
                    binding.ratingTicket.ratingLoading.visibility = View.INVISIBLE

                    if(payload == null){
                        Snackbar.make(binding.root, R.string.error_on_rating, Snackbar.LENGTH_LONG).top()
                        return
                    }

                    closeRatingTicket()

                    Snackbar.make(binding.root, R.string.success_on_rating, Snackbar.LENGTH_LONG).top()
                }
            }

            override fun onFailure(call: Call<Payload<Int>>, throwable: Throwable) {
                binding.ratingTicket.ratingLoading.visibility = View.INVISIBLE
                Snackbar.make(binding.root, R.string.error_on_rating, Snackbar.LENGTH_LONG).top()
            }
        })
    }

    private fun closeRatingTicket(){
        cachedTicketId = null
        binding.ratingTicket.root.visibility = View.INVISIBLE
    }

    private fun logout(){
        with(userSharedPreferences!!.edit()){
            remove("accessToken")
            remove("id")
            remove("name")
            remove("role")
            apply()
        }

        findNavController().navigate(R.id.action_SearchFragment_to_HomeFragment)
    }
}
