package com.coride.ui.booking

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coride.R
import com.coride.data.model.VehicleType
import com.coride.data.repository.MockDataRepository
import com.coride.ui.common.DirectionsHelper
import com.coride.ui.common.LocationHelper
import com.coride.ui.common.PlacesAutocompleteHelper
import com.coride.ui.common.SpringPhysicsHelper
import com.coride.ui.home.PlaceAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class BookingFragment : Fragment() {

    private var selectedType = VehicleType.CAR
    private var currentFare = 0.0
    private var pickupLocation: LatLng? = null
    private var destinationLocation: LatLng? = null
    private var routePoints: List<LatLng> = emptyList()
    private var isPickingDestination = false
    private var googleMap: GoogleMap? = null
    private var geocodeJob: Job? = null
    private var routeJob: Job? = null
    private var passengerCount = 1
    private var autocompleteHelper: PlacesAutocompleteHelper? = null

    // Integrated Search Views
    private lateinit var etBookingSearch: EditText
    private lateinit var btnClearSearchText: View
    private lateinit var rvIntegratedSearch: RecyclerView
    private lateinit var searchAdapter: PlaceAdapter
    private lateinit var behavior: BottomSheetBehavior<View>

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) fetchLiveLocation()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_booking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autocompleteHelper = PlacesAutocompleteHelper(requireContext())
        
        // --- Bottom Sheet Setup ---
        val sheet = view.findViewById<View>(R.id.bookingSheet)
        behavior = BottomSheetBehavior.from(sheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // View Bindings
        etBookingSearch = view.findViewById(R.id.etBookingSearch)
        btnClearSearchText = view.findViewById(R.id.btnClearSearchText)
        rvIntegratedSearch = view.findViewById(R.id.rvIntegratedSearch)
        val btnBack = view.findViewById<View>(R.id.btnBack)

        btnBack.setOnClickListener {
            if (rvIntegratedSearch.isVisible) {
                closeSearchMode()
            } else if (isPickingDestination) {
                exitPickMode()
            } else {
                findNavController().navigateUp()
            }
        }

        btnClearSearchText.setOnClickListener {
            etBookingSearch.setText("")
        }

        setupIntegratedSearch(view)
        setupUI(view)
        setupMap()

        // Read destination from arguments (if coming from search)
        val destName = arguments?.getString("destination_name")
        val destLat = arguments?.getDouble("destination_lat") ?: 0.0
        val destLng = arguments?.getDouble("destination_lng") ?: 0.0

        if (destLat != 0.0 && destLng != 0.0) {
            destinationLocation = LatLng(destLat, destLng)
            view.findViewById<TextView>(R.id.tvDestinationLocation).text = destName ?: "Destination Set"
            etBookingSearch.setText(destName ?: "")
        }

        if (LocationHelper.hasLocationPermission(requireContext())) {
            fetchLiveLocation()
        } else {
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private fun setupIntegratedSearch(view: View) {
        val tvDest = view.findViewById<TextView>(R.id.tvDestinationLocation)

        searchAdapter = PlaceAdapter(emptyList()) { place ->
            autocompleteHelper?.resolvePlace(place.id) { latLng ->
                if (latLng != null) {
                    destinationLocation = latLng
                    tvDest.text = place.name
                    etBookingSearch.setText(place.name)
                    closeSearchMode()
                    computeRouteAndFare()
                }
            }
        }

        rvIntegratedSearch.layoutManager = LinearLayoutManager(requireContext())
        rvIntegratedSearch.adapter = searchAdapter

        etBookingSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                btnClearSearchText.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                
                // Only trigger search visuals and network calls if the user is actively typing
                if (etBookingSearch.hasFocus()) {
                    if (query.isNotEmpty() && rvIntegratedSearch.visibility != View.VISIBLE) {
                        enterSearchMode()
                    }
                    performSearch(query)
                }
            }
        })

        etBookingSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && rvIntegratedSearch.visibility != View.VISIBLE) {
                enterSearchMode()
            }
        }
        
        // Deep click to navigate search mode
        tvDest.setOnClickListener {
            etBookingSearch.requestFocus()
        }
    }

    private fun enterSearchMode() {
        rvIntegratedSearch.visibility = View.VISIBLE
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun closeSearchMode() {
        rvIntegratedSearch.visibility = View.GONE
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        etBookingSearch.clearFocus()
    }

    private fun performSearch(query: String) {
        autocompleteHelper?.search(query, pickupLocation) { results ->
            if (!isAdded) return@search
            searchAdapter.updatePlaces(results)
            
            if (results.isNotEmpty() && rvIntegratedSearch.isVisible) {
                rvIntegratedSearch.post {
                    val views = mutableListOf<View>()
                    for (i in 0 until minOf(results.size, 5)) {
                        rvIntegratedSearch.getChildAt(i)?.let { views.add(it) }
                    }
                    if (views.isNotEmpty()) {
                        SpringPhysicsHelper.staggerSpringEntrance(views, 40L)
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchLiveLocation() {
        if (!LocationHelper.hasLocationPermission(requireContext())) return
        view?.findViewById<View>(R.id.locationProgressBar)?.visibility = View.VISIBLE
        
        LocationHelper.checkLocationSettings(requireContext(), 
            onSuccess = {
                LocationHelper.getCurrentLocation(requireContext()) { location ->
                    if (!isAdded) return@getCurrentLocation
                    view?.findViewById<View>(R.id.locationProgressBar)?.visibility = View.GONE
                    location?.let {
                        pickupLocation = LatLng(it.latitude, it.longitude)
                        try {
                            val geocoder = Geocoder(requireContext(), Locale.getDefault())
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                            view?.findViewById<TextView>(R.id.tvPickupLocation)?.text = addresses?.firstOrNull()?.getAddressLine(0) ?: "Your Location"
                        } catch (_: Exception) {}
                        if (destinationLocation == null) {
                            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation!!, 16f))
                        } else {
                            // If destination already exists, update camera to show the whole ride
                            updateMapMarkers() 
                        }
                        computeRouteAndFare()
                    }
                }
            },
            onResolutionRequired = {},
            onFailure = {}
        )
    }

    private fun setupUI(view: View) {
        val etFare = view.findViewById<EditText>(R.id.etFare)
        
        view.findViewById<View>(R.id.cardBike).setOnClickListener { selectedType = VehicleType.BIKE; updateVehicleSelectionUI(); SpringPhysicsHelper.springPressFeedback(it) }
        view.findViewById<View>(R.id.cardRickshaw).setOnClickListener { selectedType = VehicleType.RICKSHAW; updateVehicleSelectionUI(); SpringPhysicsHelper.springPressFeedback(it) }
        view.findViewById<View>(R.id.cardCar).setOnClickListener { selectedType = VehicleType.CAR; updateVehicleSelectionUI(); SpringPhysicsHelper.springPressFeedback(it) }

        view.findViewById<View>(R.id.btnPlus).setOnClickListener { currentFare += 10; etFare.setText(currentFare.toInt().toString()) }
        view.findViewById<View>(R.id.btnMinus).setOnClickListener { if (currentFare > 50) currentFare -= 10; etFare.setText(currentFare.toInt().toString()) }
        
        view.findViewById<MaterialButton>(R.id.btnConfirmLocation).setOnClickListener { confirmPickedDestination() }
        view.findViewById<FloatingActionButton>(R.id.fabMyLocation).setOnClickListener { fetchLiveLocation() }

        // Passenger Count Logic
        val tvPassengerCount = view.findViewById<TextView>(R.id.tvPassengerCount)
        view.findViewById<View>(R.id.btnPlusPassenger).setOnClickListener {
            if (passengerCount < 4) {
                passengerCount++
                tvPassengerCount.text = passengerCount.toString()
                SpringPhysicsHelper.springPressFeedback(it)
            } else {
                Toast.makeText(requireContext(), "Max 4 passengers", Toast.LENGTH_SHORT).show()
            }
        }
        view.findViewById<View>(R.id.btnMinusPassenger).setOnClickListener {
            if (passengerCount > 1) {
                passengerCount--
                tvPassengerCount.text = passengerCount.toString()
                SpringPhysicsHelper.springPressFeedback(it)
            }
        }

        view.findViewById<MaterialButton>(R.id.btnFindDriver).setOnClickListener {
            if (destinationLocation == null) {
                Toast.makeText(requireContext(), "Please set a destination", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val bundle = bundleOf(
                "fare" to (etFare.text.toString().toDoubleOrNull() ?: currentFare),
                "destination_name" to view.findViewById<TextView>(R.id.tvDestinationLocation).text.toString(),
                "destination_lat" to destinationLocation!!.latitude,
                "destination_lng" to destinationLocation!!.longitude,
                "pickup_lat" to (pickupLocation?.latitude ?: 0.0),
                "pickup_lng" to (pickupLocation?.longitude ?: 0.0),
                "ride_type" to selectedType.name,
                "passenger_count" to passengerCount
            )
            findNavController().navigate(R.id.action_booking_to_offers, bundle)
        }
        updateVehicleSelectionUI()
    }

    private fun updateVehicleSelectionUI() {
        listOf(R.id.cardBike to VehicleType.BIKE, R.id.cardRickshaw to VehicleType.RICKSHAW, R.id.cardCar to VehicleType.CAR).forEach { (id, type) ->
            val card = view?.findViewById<com.google.android.material.card.MaterialCardView>(id)
            val isSelected = selectedType == type
            
            card?.strokeWidth = if (isSelected) 6 else 1
            card?.strokeColor = if (isSelected) ContextCompat.getColor(requireContext(), R.color.primary) else ContextCompat.getColor(requireContext(), R.color.outline_variant)
            card?.cardElevation = if (isSelected) 8f else 0f
            card?.alpha = if (isSelected) 1.0f else 0.85f
        }
        computeRouteAndFare()
    }

    private fun computeRouteAndFare() {
        val p = pickupLocation ?: return
        val d = destinationLocation ?: return
        routeJob?.cancel()
        routeJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = DirectionsHelper.generateRoute(p, d)
                routePoints = result.polylinePoints
                val distanceKm = result.distanceMeters / 1000.0
                currentFare = MockDataRepository.getRecommendedFare(distanceKm, selectedType)
                updateMapMarkers()
                view?.findViewById<EditText>(R.id.etFare)?.setText(currentFare.toInt().toString())
                view?.findViewById<TextView>(R.id.tvRecommendedFare)?.text =
                    getString(
                        R.string.booking_recommended_fare_distance,
                        currentFare.toInt(),
                        DirectionsHelper.formatDistance(result.distanceMeters)
                    )
            } catch (_: Exception) {}
        }
    }

    private fun setupMap() {
        (childFragmentManager.findFragmentById(R.id.bookingMapView) as? SupportMapFragment)?.getMapAsync { map ->
            googleMap = map
            map.setOnCameraIdleListener { if (isPickingDestination) geocodeLiveAddress(map.cameraPosition.target) }
            pickupLocation?.let { map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f)) }
            updateMapMarkers()
        }
    }

    private fun exitPickMode() {
        isPickingDestination = false
        view?.apply {
            findViewById<View>(R.id.ivCenterPin).visibility = View.GONE
            findViewById<View>(R.id.pickingHeader).visibility = View.GONE
            findViewById<View>(R.id.btnConfirmLocation).visibility = View.GONE
            findViewById<View>(R.id.layoutBookingToolbar).visibility = View.VISIBLE
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        updateMapMarkers()
    }

    private fun confirmPickedDestination() {
        destinationLocation = googleMap?.cameraPosition?.target ?: return
        exitPickMode()
        computeRouteAndFare()
    }

    private fun geocodeLiveAddress(latLng: LatLng) {
        geocodeJob?.cancel()
        geocodeJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            delay(350)
            try {
                @Suppress("DEPRECATION")
                val addresses = Geocoder(requireContext(), Locale.getDefault()).getFromLocation(latLng.latitude, latLng.longitude, 1)
                val text = addresses?.firstOrNull()?.let { it.thoroughfare ?: it.featureName } ?: "Selected Location"
                launch(Dispatchers.Main) { if (isAdded && isPickingDestination) view?.findViewById<TextView>(R.id.tvPickingTask)?.text = text }
            } catch (_: Exception) {}
        }
    }

    private fun updateMapMarkers() {
        val map = googleMap ?: return
        map.clear()
        if (isPickingDestination) return
        pickupLocation?.let { map.addMarker(MarkerOptions().position(it).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))) }
        destinationLocation?.let { dest ->
            map.addMarker(MarkerOptions().position(dest).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
            if (routePoints.isNotEmpty()) map.addPolyline(PolylineOptions().addAll(routePoints).width(12f).color(ContextCompat.getColor(requireContext(), R.color.primary)))
            
            val p = pickupLocation
            if (p != null) {
                val bounds = LatLngBounds.Builder().include(p).include(dest).build()
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
            }
        }
    }

    override fun onDestroyView() {
        geocodeJob?.cancel()
        routeJob?.cancel()
        autocompleteHelper?.cancel()
        googleMap = null
        super.onDestroyView()
    }
}
