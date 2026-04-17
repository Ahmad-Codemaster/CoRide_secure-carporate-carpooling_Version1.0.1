package com.coride.ui.booking

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.coride.R
import com.coride.data.model.VehicleType
import com.coride.data.repository.MockDataRepository
import com.coride.ui.common.DirectionsHelper
import com.coride.ui.common.LocationHelper
import com.coride.ui.common.SpringPhysicsHelper
import java.util.Locale

class BookingFragment : Fragment() {

    private var selectedType = VehicleType.ECONOMY
    private var currentFare = 0.0
    private var pickupLocation: LatLng? = null
    private var destinationLocation: LatLng? = null
    private var routePoints: List<LatLng> = emptyList()
    private var routeDistanceMeters: Int = 0
    private var routeDurationSeconds: Int = 0
    private var isPickingDestination = false
    private var googleMap: GoogleMap? = null
    private var geocodeJob: Job? = null
    private var routeJob: Job? = null

    // ── Permission Launcher ──
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (fineGranted) {
            fetchLiveLocation()
        } else {
            val shouldShowRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            if (!shouldShowRationale) {
                // Permanently denied
                showPermissionSettingsDialog()
            } else {
                Toast.makeText(requireContext(), "Location permission required for pickup", Toast.LENGTH_LONG).show()
                // Fall back to first mock place
                val fallback = MockDataRepository.getPlaces().firstOrNull()
                pickupLocation = fallback?.let { LatLng(it.latitude, it.longitude) }
                view?.findViewById<TextView>(R.id.tvPickupLocation)?.text = fallback?.name ?: "Unknown"
                pickupLocation?.let { googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15f)) }
            }
        }
    }

    private val gpsResolutionLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            fetchLiveLocation()
        } else {
            Toast.makeText(requireContext(), "GPS is required for accurate location", Toast.LENGTH_SHORT).show()
            hideLoading()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_booking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ensure bottom sheet starts Expanded
        val sheet = view.findViewById<View>(R.id.bookingSheet)
        val behavior = BottomSheetBehavior.from(sheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isHideable = true

        val btnBringUpSheet = view.findViewById<View>(R.id.btnBringUpSheet)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN && !isPickingDestination) {
                    btnBringUpSheet.visibility = View.VISIBLE
                    btnBringUpSheet.alpha = 0f
                    btnBringUpSheet.animate().alpha(1f).setDuration(200).start()
                } else {
                    btnBringUpSheet.visibility = View.GONE
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        btnBringUpSheet.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // Read destination from arguments (if coming from search)
        val destName = arguments?.getString("destination_name") ?: ""
        val destLat = arguments?.getDouble("destination_lat") ?: 0.0
        val destLng = arguments?.getDouble("destination_lng") ?: 0.0

        if (destLat != 0.0 && destLng != 0.0) {
            destinationLocation = LatLng(destLat, destLng)
        }

        view.findViewById<TextView>(R.id.tvPickupLocation).text = "Locating you…"
        view.findViewById<TextView>(R.id.tvDestinationLocation).text =
            if (destName.isNotEmpty()) destName else "Tap to set destination"

        // Destination tap → enter pick mode
        view.findViewById<View>(R.id.tvDestinationLocation).setOnClickListener {
            enterPickMode()
        }

        setupUI(view)
        setupMap()

        // Request location permission or fetch immediately
        if (LocationHelper.hasLocationPermission(requireContext())) {
            fetchLiveLocation()
        } else {
            locationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    // ── Live GPS Fetch ──
    @SuppressLint("SetTextI18n")
    private fun fetchLiveLocation() {
        if (!LocationHelper.hasLocationPermission(requireContext())) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
            return
        }

        showLoading()

        LocationHelper.checkLocationSettings(
            context = requireContext(),
            onSuccess = {
                LocationHelper.getCurrentLocation(requireContext()) { location ->
                    if (!isAdded) return@getCurrentLocation
                    hideLoading()
                    location?.let {
                        pickupLocation = LatLng(it.latitude, it.longitude)

                        // Reverse geocode for address
                        try {
                            val geocoder = Geocoder(requireContext(), Locale.getDefault())
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                            val addressText = addresses?.firstOrNull()?.let { addr ->
                                addr.getAddressLine(0) ?: "Your Location"
                            } ?: "Your Location"
                            view?.findViewById<TextView>(R.id.tvPickupLocation)?.text = addressText
                        } catch (e: Exception) {
                            view?.findViewById<TextView>(R.id.tvPickupLocation)?.text = "Your Location"
                        }

                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation!!, 16f))
                        computeRouteAndFare()
                        updateMapMarkers()
                    } ?: run {
                        Toast.makeText(requireContext(), "Weak GPS signal. Tap again.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onResolutionRequired = { exception ->
                try {
                    val intentSenderRequest = androidx.activity.result.IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    gpsResolutionLauncher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    hideLoading()
                }
            },
            onFailure = {
                hideLoading()
                Toast.makeText(requireContext(), "GPS error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showLoading() {
        view?.findViewById<View>(R.id.locationProgressBar)?.visibility = View.VISIBLE
        view?.findViewById<View>(R.id.fabMyLocation)?.isEnabled = false
        view?.findViewById<TextView>(R.id.tvPickupLocation)?.text = "Locating you…"
    }

    private fun hideLoading() {
        view?.findViewById<View>(R.id.locationProgressBar)?.visibility = View.GONE
        view?.findViewById<View>(R.id.fabMyLocation)?.isEnabled = true
    }

    private fun showPermissionSettingsDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Location Permission Needed")
            .setMessage("Location access is permanently denied. Please enable it in app settings to calculate fares and pickup points.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupUI(view: View) {
        val etFare = view.findViewById<EditText>(R.id.etFare)
        val tvRecommended = view.findViewById<TextView>(R.id.tvRecommendedFare)
        val btnBack = view.findViewById<View>(R.id.btnBackContainer)
        val btnPlus = view.findViewById<View>(R.id.btnPlus)
        val btnMinus = view.findViewById<View>(R.id.btnMinus)
        val btnConfirmLocation = view.findViewById<MaterialButton>(R.id.btnConfirmLocation)
        val fabMyLocation = view.findViewById<FloatingActionButton>(R.id.fabMyLocation)
        val btnFindDriver = view.findViewById<MaterialButton>(R.id.btnFindDriver)

        etFare.setText(currentFare.toInt().toString())
        refreshFareText(tvRecommended)

        btnBack.setOnClickListener {
            if (isPickingDestination) exitPickMode() else findNavController().navigateUp()
        }

        btnPlus.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(view.findViewById(R.id.btnPlusContainer))
            currentFare += 10
            etFare.setText(currentFare.toInt().toString())
        }

        btnMinus.setOnClickListener {
            if (currentFare > 50) {
                SpringPhysicsHelper.springPressFeedback(view.findViewById(R.id.btnMinusContainer))
                currentFare -= 10
                etFare.setText(currentFare.toInt().toString())
            }
        }

        btnConfirmLocation.setOnClickListener { confirmPickedDestination() }

        fabMyLocation.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            fetchLiveLocation()
        }

        btnFindDriver.setOnClickListener { btn ->
            if (pickupLocation == null) {
                Toast.makeText(requireContext(), "Waiting for GPS location…", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (destinationLocation == null) {
                Toast.makeText(requireContext(), "Please set a destination", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            SpringPhysicsHelper.springPressFeedback(btn)
            val fareValue = etFare.text.toString().toDoubleOrNull() ?: currentFare

            val bundle = bundleOf(
                "fare" to fareValue,
                "destination_name" to view.findViewById<TextView>(R.id.tvDestinationLocation).text.toString(),
                "destination_lat" to destinationLocation!!.latitude,
                "destination_lng" to destinationLocation!!.longitude,
                "pickup_lat" to pickupLocation!!.latitude,
                "pickup_lng" to pickupLocation!!.longitude,
                "ride_type" to selectedType.name
            )
            findNavController().navigate(R.id.action_booking_to_offers, bundle)
        }

        setupServiceTypes(view, etFare, tvRecommended)
    }

    private fun setupServiceTypes(view: View, etFare: EditText, tvRecommended: TextView) {
        val chipGroup = view.findViewById<ChipGroup>(R.id.rideTypeChipGroup)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedType = when {
                checkedIds.contains(R.id.chipEconomy) -> VehicleType.ECONOMY
                checkedIds.contains(R.id.chipComfort) -> VehicleType.COMFORT
                checkedIds.contains(R.id.chipXL) -> VehicleType.XL
                else -> VehicleType.ECONOMY
            }
            computeRouteAndFare()
            etFare.setText(currentFare.toInt().toString())
            refreshFareText(tvRecommended)
        }
    }

    // ── Map Pick Destination ──
    private fun enterPickMode() {
        isPickingDestination = true
        view?.apply {
            findViewById<View>(R.id.ivCenterPin).visibility = View.VISIBLE
            findViewById<View>(R.id.pickingHeader).visibility = View.VISIBLE
            findViewById<TextView>(R.id.tvPickingTask).text = "Drop pin to select"
            findViewById<View>(R.id.btnConfirmLocation).visibility = View.VISIBLE
            
            // Hide the bottom sheet to focus on map
            val sheet = findViewById<View>(R.id.bookingSheet)
            val behavior = BottomSheetBehavior.from(sheet)
            behavior.isHideable = true
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        
        googleMap?.clear()
        
        val focusLoc = destinationLocation ?: pickupLocation
        if (focusLoc != null) {
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(focusLoc, 16f))
        }
    }

    private fun exitPickMode() {
        isPickingDestination = false
        view?.apply {
            findViewById<View>(R.id.ivCenterPin).visibility = View.GONE
            findViewById<View>(R.id.pickingHeader).visibility = View.GONE
            findViewById<View>(R.id.btnConfirmLocation).visibility = View.GONE
            
            // Bring sheet back up
            val sheet = findViewById<View>(R.id.bookingSheet)
            val behavior = BottomSheetBehavior.from(sheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        updateMapMarkers()
    }

    @SuppressLint("SetTextI18n")
    private fun confirmPickedDestination() {
        val target = googleMap?.cameraPosition?.target ?: return
        destinationLocation = target

        // Use the live preview text we already fetched in the background
        val liveText = view?.findViewById<TextView>(R.id.tvPickingTask)?.text?.toString()
        val finalName = if (liveText.isNullOrEmpty() || liveText == "Locating..." || liveText == "Drop pin to select") {
            "Selected Location"
        } else {
            liveText
        }
        
        view?.findViewById<TextView>(R.id.tvDestinationLocation)?.text = finalName

        exitPickMode()
        computeRouteAndFare()
        refreshFareUI()
    }

    // ── Route & Fare ──
    private fun computeRouteAndFare() {
        val p = pickupLocation ?: return
        val d = destinationLocation ?: return

        routeJob?.cancel()
        routeJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = DirectionsHelper.generateRoute(p, d)
                routePoints = result.polylinePoints
                routeDistanceMeters = result.distanceMeters
                routeDurationSeconds = result.durationSeconds

                val distanceKm = routeDistanceMeters / 1000.0
                currentFare = MockDataRepository.getRecommendedFare(distanceKm, selectedType)
                updateMapMarkers()
                refreshFareUI()
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    private fun refreshFareUI() {
        val etFare = view?.findViewById<EditText>(R.id.etFare)
        val tvRecommended = view?.findViewById<TextView>(R.id.tvRecommendedFare)
        etFare?.setText(currentFare.toInt().toString())
        refreshFareText(tvRecommended)
    }

    @SuppressLint("SetTextI18n")
    private fun refreshFareText(tvRecommended: TextView?) {
        if (currentFare > 0 && routeDistanceMeters > 0) {
            val distStr = DirectionsHelper.formatDistance(routeDistanceMeters)
            val durStr = DirectionsHelper.formatDuration(routeDurationSeconds)
            tvRecommended?.text = "Recommended: Rs. ${currentFare.toInt()}  •  $distStr  •  $durStr"
        } else if (currentFare > 0) {
            tvRecommended?.text = getString(R.string.recommended_fare, "Rs. ${currentFare.toInt()}")
        } else {
            tvRecommended?.text = "Select destination to see fare"
        }
    }

    // ── Map ──
    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.bookingMapView) as? SupportMapFragment
        mapFragment?.getMapAsync { map ->
            googleMap = map
            map.uiSettings.isZoomControlsEnabled = false
            map.uiSettings.isMyLocationButtonEnabled = false
            
            // Map picking listeners
            map.setOnCameraMoveStartedListener {
                if (isPickingDestination) {
                    view?.findViewById<TextView>(R.id.tvPickingTask)?.text = "Locating..."
                    // Hop the pin up
                    view?.findViewById<View>(R.id.ivCenterPin)?.animate()?.translationY(-40f)?.setDuration(150)?.start()
                }
            }

            map.setOnCameraIdleListener {
                if (isPickingDestination) {
                    // Drop the pin back
                    view?.findViewById<View>(R.id.ivCenterPin)?.animate()?.translationY(0f)?.setDuration(150)?.start()
                    geocodeLiveAddress(map.cameraPosition.target)
                }
            }

            map.setOnMapClickListener { latLng ->
                if (isPickingDestination) {
                    map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                }
            }
            
            pickupLocation?.let { map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f)) }
            updateMapMarkers()
        }
    }

    private fun geocodeLiveAddress(latLng: LatLng) {
        geocodeJob?.cancel()
        geocodeJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            delay(350) // Debounce rapid map movement
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                
                val addressText = if (!addresses.isNullOrEmpty()) {
                    val addr = addresses.first()
                    addr.thoroughfare ?: addr.featureName ?: addr.locality ?: "Selected Location"
                } else {
                    "Selected Location"
                }

                launch(Dispatchers.Main) {
                    if (isAdded && isPickingDestination) {
                        view?.findViewById<TextView>(R.id.tvPickingTask)?.text = addressText
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    if (isAdded && isPickingDestination) {
                        view?.findViewById<TextView>(R.id.tvPickingTask)?.text = "Selected Location"
                    }
                }
            }
        }
    }

    private fun updateMapMarkers() {
        val map = googleMap ?: return
        map.clear()
        if (isPickingDestination) return

        val p = pickupLocation ?: return
        map.addMarker(MarkerOptions()
            .position(p)
            .title("Pickup")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))

        val d = destinationLocation
        if (d != null) {
            map.addMarker(MarkerOptions()
                .position(d)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

            // Draw curved route polyline
            if (routePoints.isNotEmpty()) {
                map.addPolyline(PolylineOptions()
                    .addAll(routePoints)
                    .width(12f)
                    .color(ContextCompat.getColor(requireContext(), R.color.primary))
                    .geodesic(false))
            }

            val bounds = LatLngBounds.Builder().include(p).include(d).build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
        } else {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(p, 16f))
        }
    }
}

