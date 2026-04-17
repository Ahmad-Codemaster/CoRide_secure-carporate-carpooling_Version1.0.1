package com.coride.ui.ride

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.coride.R
import com.coride.data.model.RideState
import com.coride.data.repository.MockDataRepository
import com.coride.service.RideForegroundService
import com.coride.ui.common.DirectionsHelper
import com.coride.ui.common.SpringPhysicsHelper
import com.coride.utils.FirebaseSafetyHelper
import com.coride.utils.SmsSafetyHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RideFragment : Fragment() {

    private var pickupLocation: LatLng? = null
    private var destinationLocation: LatLng? = null
    private var driverStartLocation: LatLng? = null
    private var googleMap: GoogleMap? = null
    private var driverMarker: Marker? = null

    // Route polylines
    private var approachPath: List<LatLng> = emptyList()  // driver → pickup

    // Safety system state
    private var safetyCheckShowing = false
    private var currentDriverPosition: LatLng? = null
    private var ridePath: List<LatLng> = emptyList()       // pickup → destination

    // State machine
    private var currentState: RideState = RideState.SearchingDrivers
    private var countDownTimer: CountDownTimer? = null
    private var rideId: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ride, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request Notification Permission for Android 13+ (Essential for Foreground Service)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }


        // Read ALL coordinates from bundle — no hardcoded defaults
        val driverName = arguments?.getString("driver_name") ?: "Driver"
        val driverRating = arguments?.getFloat("driver_rating") ?: 4.8f
        val driverPhone = arguments?.getString("driver_phone") ?: ""
        val vehicleInfo = arguments?.getString("vehicle_info") ?: ""
        val plateNumber = arguments?.getString("plate_number") ?: ""
        val fare = arguments?.getDouble("fare") ?: 300.0
        val eta = arguments?.getInt("eta") ?: 5
        val destName = arguments?.getString("destination_name") ?: ""
        val destLat = arguments?.getDouble("destination_lat") ?: 0.0
        val destLng = arguments?.getDouble("destination_lng") ?: 0.0
        val pickupLat = arguments?.getDouble("pickup_lat") ?: 0.0
        val pickupLng = arguments?.getDouble("pickup_lng") ?: 0.0
        val driverLat = arguments?.getDouble("driver_lat") ?: 0.0
        val driverLng = arguments?.getDouble("driver_lng") ?: 0.0

        pickupLocation = if (pickupLat != 0.0) LatLng(pickupLat, pickupLng) else LatLng(31.5204, 74.3587)
        destinationLocation = if (destLat != 0.0) LatLng(destLat, destLng) else null
        driverStartLocation = if (driverLat != 0.0) LatLng(driverLat, driverLng) else null

        // Generate unique ride ID
        rideId = "ride_${System.currentTimeMillis()}"

        viewLifecycleOwner.lifecycleScope.launch {
            // Generate real street approach path (driver → pickup)
            driverStartLocation?.let { dStart ->
                pickupLocation?.let { pLoc ->
                    approachPath = DirectionsHelper.generateApproachPath(dStart, pLoc, 40)
                    googleMap?.let { drawInitialMap(it) } // Redraw once path is ready
                }
            }

            // Generate real street ride path (pickup → destination)
            pickupLocation?.let { pLoc ->
                destinationLocation?.let { dLoc ->
                    val route = DirectionsHelper.generateRoute(pLoc, dLoc, 60)
                    ridePath = route.polylinePoints
                    googleMap?.let { drawInitialMap(it) } // Redraw once path is ready
                }
            }
        }

        // Populate UI
        view.findViewById<TextView>(R.id.tvDriverName).text = driverName
        view.findViewById<TextView>(R.id.tvDriverRating).text = driverRating.toString()
        view.findViewById<TextView>(R.id.tvVehicleInfo).text = vehicleInfo
        view.findViewById<TextView>(R.id.tvPlateNumber).text = plateNumber
        view.findViewById<TextView>(R.id.tvDestination).text = destName
        view.findViewById<TextView>(R.id.tvFare).text = "₨ ${fare.toInt()}"

        setupButtons(view, driverName, driverPhone, vehicleInfo, plateNumber, destName, fare, driverRating)
        setupMap()
        setupWeatherFeature(view)

        // Begin the ride lifecycle
        startRideLifecycle()
    }

    private fun setupButtons(view: View, driverName: String, driverPhone: String, vehicleInfo: String, plateNumber: String, destName: String, fare: Double, driverRating: Float) {
        val fabShare = view.findViewById<FloatingActionButton>(R.id.fabShare)
        val fabSos = view.findViewById<FloatingActionButton>(R.id.fabSos)
        val btnActionRide = view.findViewById<MaterialButton>(R.id.btnCancelRide)

        view.findViewById<ImageView>(R.id.btnCall).setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$driverPhone")))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Cannot make call", Toast.LENGTH_SHORT).show()
            }
        }

        fabShare.setOnClickListener {
            val shareText = "🚗 CoRide Safety Share\n\nI'm riding with $driverName (✅ Verified)\nVehicle: $vehicleInfo ($plateNumber)\nHeading to: $destName\n\nTrack me on CoRide!"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, "Share ride with..."))
        }

        fabSos.setOnClickListener {
            val safetyDialog = SafetyCheckDialogFragment.newInstance(
                rideId, 
                currentDriverPosition?.latitude ?: 0.0, 
                currentDriverPosition?.longitude ?: 0.0
            )
            safetyDialog.show(childFragmentManager, "sos_dialog")
        }

        btnActionRide.setOnClickListener {
            when (currentState) {
                is RideState.RideCompleted -> {
                    // Navigate to completion
                    val bundle = Bundle()
                    bundle.apply {
                        putString("driver_name", driverName)
                        putFloat("driver_rating", driverRating)
                        putDouble("fare", fare)
                        putString("destination_name", destName)
                        putString("vehicle_info", vehicleInfo)
                        
                        if (pickupLocation != null && destinationLocation != null) {
                            viewLifecycleOwner.lifecycleScope.launch {
                                val route = DirectionsHelper.generateRoute(pickupLocation!!, destinationLocation!!)
                                putDouble("distance_km", route.distanceMeters / 1000.0)
                                putInt("duration_seconds", route.durationSeconds)
                                findNavController().navigate(R.id.action_ride_to_complete, bundle)
                            }
                        } else {
                            putDouble("distance_km", 0.0)
                            putInt("duration_seconds", 0)
                            findNavController().navigate(R.id.action_ride_to_complete, bundle)
                        }
                    }
                }
                is RideState.RideInProgress -> {
                    Toast.makeText(requireContext(), "Ride is in progress. Please wait until destination is reached.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Cancel ride
                    val cancelledRide = com.coride.data.model.Ride(
                        id = "ride_${System.currentTimeMillis()}",
                        pickup = com.coride.data.model.Place("p1", "Pickup", "Your Location", pickupLocation!!.latitude, pickupLocation!!.longitude),
                        destination = com.coride.data.model.Place("p2", destName, destName, destinationLocation?.latitude ?: 0.0, destinationLocation?.longitude ?: 0.0),
                        driver = MockDataRepository.getDrivers().firstOrNull { it.name == driverName },
                        status = com.coride.data.model.RideStatus.CANCELLED,
                        requestedFare = fare,
                        finalFare = 0.0,
                        date = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
                    )
                    MockDataRepository.addRide(cancelledRide)
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
            }
        }

        SpringPhysicsHelper.springFabEntrance(fabShare, delay = 200L)
        SpringPhysicsHelper.springFabEntrance(fabSos, delay = 300L)
    }

    // ── Map Setup ──
    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.rideMapView) as? SupportMapFragment
        mapFragment?.getMapAsync { map ->
            googleMap = map
            map.uiSettings.isZoomControlsEnabled = false
            drawInitialMap(map)
        }
    }

    private fun drawInitialMap(map: GoogleMap) {
        map.clear()
        driverMarker = null

        // Apply Premium Monochrome Map Style
        val mapStyle = """
            [
              { "elementType": "geometry", "stylers": [ { "color": "#f5f5f5" } ] },
              { "elementType": "labels.icon", "stylers": [ { "visibility": "off" } ] },
              { "elementType": "labels.text.fill", "stylers": [ { "color": "#616161" } ] },
              { "elementType": "labels.text.stroke", "stylers": [ { "color": "#f5f5f5" } ] },
              { "featureType": "administrative.land_parcel", "elementType": "labels.text.fill", "stylers": [ { "color": "#bdbdbd" } ] },
              { "featureType": "poi", "elementType": "geometry", "stylers": [ { "color": "#eeeeee" } ] },
              { "featureType": "poi", "elementType": "labels.text.fill", "stylers": [ { "color": "#757575" } ] },
              { "featureType": "road", "elementType": "geometry", "stylers": [ { "color": "#ffffff" } ] },
              { "featureType": "road.arterial", "elementType": "labels.text.fill", "stylers": [ { "color": "#757575" } ] },
              { "featureType": "road.highway", "elementType": "geometry", "stylers": [ { "color": "#dadada" } ] },
              { "featureType": "road.highway", "elementType": "labels.text.fill", "stylers": [ { "color": "#616161" } ] },
              { "featureType": "water", "elementType": "geometry", "stylers": [ { "color": "#c9c9c9" } ] },
              { "featureType": "water", "elementType": "labels.text.fill", "stylers": [ { "color": "#9e9e9e" } ] }
            ]
        """.trimIndent()
        map.setMapStyle(com.google.android.gms.maps.model.MapStyleOptions(mapStyle))

        val pLoc = pickupLocation ?: return

        // Pickup marker (Monochrome Azure)
        map.addMarker(MarkerOptions()
            .position(pLoc)
            .title("Pickup")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

        // Destination marker + route
        destinationLocation?.let { dLoc ->
            map.addMarker(MarkerOptions()
                .position(dLoc)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)))

            // Draw ride route (pickup → destination) as dimmed line
            if (ridePath.isNotEmpty()) {
                map.addPolyline(PolylineOptions()
                    .addAll(ridePath)
                    .width(14f)
                    .color(android.graphics.Color.parseColor("#555555"))
                    .geodesic(false))
            }

            // Draw approach route (driver → pickup) highlighted in Solid Black
            if (approachPath.isNotEmpty()) {
                map.addPolyline(PolylineOptions()
                    .addAll(approachPath)
                    .width(14f)
                    .color(android.graphics.Color.parseColor("#111111"))
                    .geodesic(false))
            }

            val boundsBuilder = LatLngBounds.Builder().include(pLoc).include(dLoc)
            driverStartLocation?.let { boundsBuilder.include(it) }
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120))
        } ?: run {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pLoc, 15f))
        }
    }

    // ── State Machine Ride Lifecycle ──
    @SuppressLint("SetTextI18n")
    private fun startRideLifecycle() {
        viewLifecycleOwner.lifecycleScope.launch {
            // ─── PHASE 1: Driver Arriving (5 seconds) ───
            updateState(RideState.DriverArriving(0f, 5))

            // Snapshot the path to prevent race conditions if it updates mid-loop
            val currentApproach = approachPath.toList()
            val arrivalSteps = if (currentApproach.isNotEmpty()) currentApproach.size else 50
            val arrivalDelayPerStep = 5000L / arrivalSteps // Total 5 seconds

            for (i in 0 until arrivalSteps) {
                if (!isAdded) return@launch

                val pos = if (currentApproach.isNotEmpty()) {
                    currentApproach[minOf(i, currentApproach.size - 1)]
                } else {
                    val dStart = driverStartLocation ?: pickupLocation!!
                    interpolateLatLng(dStart, pickupLocation!!, i.toFloat() / arrivalSteps)
                }

                updateDriverMarker(pos)

                // Calculate remaining distance
                val remaining = if (currentApproach.isNotEmpty()) {
                    var dist = 0.0
                    for (j in i until currentApproach.size - 1) {
                        dist += DirectionsHelper.haversineDistance(currentApproach[j], currentApproach[j + 1])
                    }
                    dist
                } else {
                    DirectionsHelper.haversineDistance(pos, pickupLocation!!)
                }

                val remainingKm = (remaining / 1000f).toFloat()
                val etaMin = maxOf(1, (5 * (1 - i.toFloat() / arrivalSteps)).toInt())
                updateState(RideState.DriverArriving(remainingKm, etaMin))

                delay(arrivalDelayPerStep)
            }

            // ─── PHASE 2: Driver Arrived (2 seconds pause) ───
            if (!isAdded) return@launch
            updateState(RideState.DriverArrived)
            updateDriverMarker(pickupLocation!!)
            delay(2000)

            // ─── PHASE 3: Ride In Progress (120 seconds for demo → auto complete) ───
            if (!isAdded) return@launch
            updateState(RideState.RideInProgress(0f, 2))

            // ── AUTO-SEND ride details to emergency contacts via SMS ──
            val driverNameVal = arguments?.getString("driver_name") ?: "Driver"
            val vehicleInfoVal = arguments?.getString("vehicle_info") ?: ""
            val plateNumberVal = arguments?.getString("plate_number") ?: ""
            val destNameVal = arguments?.getString("destination_name") ?: ""
            val pLat = pickupLocation?.latitude ?: 0.0
            val pLng = pickupLocation?.longitude ?: 0.0
            val dLat = destinationLocation?.latitude ?: 0.0
            val dLng = destinationLocation?.longitude ?: 0.0

            if (SmsSafetyHelper.hasSmsPermission(requireContext())) {
                val rideMsg = SmsSafetyHelper.buildRideStartMessage(
                    rideId, driverNameVal, vehicleInfoVal, plateNumberVal, destNameVal,
                    dLat, dLng
                )
                val count = SmsSafetyHelper.sendToAllEmergencyContacts(requireContext(), rideMsg)
                if (count > 0) {
                    Toast.makeText(requireContext(), "✅ Ride details sent to $count emergency contacts", Toast.LENGTH_SHORT).show()
                }
                Log.d("RideSafety", "Auto-sent ride details to $count contacts")
            } else {
                Log.w("RideSafety", "SMS permission not granted. Requesting...")
                activity?.let { SmsSafetyHelper.requestSmsPermission(it) }
            }

            // ── Start Foreground Service (persistent notification + Supabase live push) ──
            RideForegroundService.startService(
                requireContext(), rideId, driverNameVal, destNameVal, pLat, pLng
            )

            // Redraw map: highlight ride route, dim approach
            googleMap?.let { map ->
                map.clear()
                driverMarker = null

                pickupLocation?.let { pLoc ->
                    map.addMarker(MarkerOptions()
                        .position(pLoc)
                        .title("Pickup")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                }
                destinationLocation?.let { dLoc ->
                    map.addMarker(MarkerOptions()
                        .position(dLoc)
                        .title("Destination")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)))
                }

                if (ridePath.isNotEmpty()) {
                    map.addPolyline(PolylineOptions()
                        .addAll(ridePath)
                        .width(14f)
                        .color(android.graphics.Color.parseColor("#111111"))
                        .geodesic(false))
                }
            }

            // Snapshot the path to prevent race conditions
            val currentRide = ridePath.toList()
            val totalRideTimeMs = 120_000L
            val rideSteps = if (currentRide.isNotEmpty()) currentRide.size else 80
            val rideDelayPerStep = totalRideTimeMs / rideSteps

            // Safety check: trigger at ~40% progress (simulate a stop)
            val safetyTriggerStep = (rideSteps * 0.4).toInt()

            for (i in 0 until rideSteps) {
                if (!isAdded) return@launch

                val pos = if (currentRide.isNotEmpty()) {
                    currentRide[minOf(i, currentRide.size - 1)]
                } else {
                    interpolateLatLng(pickupLocation!!, destinationLocation!!, i.toFloat() / rideSteps)
                }

                currentDriverPosition = pos
                updateDriverMarker(pos)

                // Update foreground service with latest location (pushed as "user")
                RideForegroundService.updateLocation(requireContext(), rideId, pos.latitude, pos.longitude)
                
                // ALSO push simulating the DRIVER explicitly (so TWO markers appear in tracker)
                FirebaseSafetyHelper.pushLocationUpdate(rideId, "driver", pos.latitude, pos.longitude)

                val progress = i.toFloat() / rideSteps
                val etaMin = maxOf(1, ((totalRideTimeMs / 60000.0) * (1 - progress)).toInt())
                updateState(RideState.RideInProgress(progress, etaMin))

                // ── SAFETY CHECK: Stop for 10 seconds at ~40%, then show dialog ──
                if (i == safetyTriggerStep && !safetyCheckShowing) {
                    safetyCheckShowing = true
                    Log.d("RideSafety", "Ride stopped! Triggering safety check in 10 seconds...")
                    delay(10_000) // Ride stops for 10 seconds

                    if (isAdded && !isDetached) {
                        val safetyDialog = SafetyCheckDialogFragment.newInstance(
                            rideId, pos.latitude, pos.longitude
                        )
                        safetyDialog.onDismissedSafe = {
                            safetyCheckShowing = false
                            Log.d("RideSafety", "User confirmed OK. Ride continues.")
                        }
                        safetyDialog.onSosTriggered = {
                            safetyCheckShowing = false
                            Log.d("RideSafety", "SOS triggered via safety check!")
                        }
                        safetyDialog.show(childFragmentManager, "safety_check")
                    }
                }

                delay(rideDelayPerStep)
            }

            // ─── PHASE 4: Ride Completed ───
            if (!isAdded) return@launch

            // Stop the foreground service normally
            RideForegroundService.stopService(requireContext())

            updateState(RideState.RideCompleted)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateState(state: RideState) {
        if (!isAdded) return
        currentState = state

        val tvStatus = view?.findViewById<TextView>(R.id.tvRideStatus)
        val tvDistance = view?.findViewById<TextView>(R.id.tvDistanceAway)
        val tvEtaStatus = view?.findViewById<TextView>(R.id.tvEtaStatus)
        val btnActionRide = view?.findViewById<MaterialButton>(R.id.btnCancelRide)

        when (state) {
            is RideState.SearchingDrivers -> {
                tvStatus?.text = getString(R.string.searching_drivers)
            }
            is RideState.DriverAssigned -> {
                tvStatus?.text = "Driver ${state.driverName} assigned"
            }
            is RideState.DriverArriving -> {
                tvStatus?.text = getString(R.string.driver_arriving)
                tvDistance?.text = String.format("%.2f km", state.distanceKm)
                tvDistance?.visibility = View.VISIBLE
                tvEtaStatus?.text = "• ETA: ${state.etaMin} min"
                btnActionRide?.text = getString(R.string.cancel_ride)
            }
            is RideState.DriverArrived -> {
                tvStatus?.text = "Driver is here! 🚗"
                tvDistance?.text = "0.0 km"
                tvEtaStatus?.text = "• Arrived"
            }
            is RideState.RideInProgress -> {
                tvStatus?.text = getString(R.string.ride_in_progress)
                tvDistance?.visibility = View.GONE
                tvEtaStatus?.text = "• ETA: ${state.etaMin} min"
                btnActionRide?.text = "Ride Ongoing..."
                btnActionRide?.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface_variant))
                btnActionRide?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_container_highest))
            }
            is RideState.RideCompleted -> {
                tvStatus?.text = "Ride Completed! ✅"
                tvEtaStatus?.text = "• Destination reached"
                btnActionRide?.text = "Rate & Finish"
            }
        }
    }

    // ── Marker management ──
    private fun updateDriverMarker(position: LatLng) {
        val map = googleMap ?: return
        if (driverMarker == null) {
            driverMarker = map.addMarker(MarkerOptions()
                .position(position)
                .title("Driver")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
        } else {
            driverMarker?.position = position
        }
        map.animateCamera(CameraUpdateFactory.newLatLng(position))
    }

    private fun interpolateLatLng(from: LatLng, to: LatLng, fraction: Float): LatLng {
        val lat = (to.latitude - from.latitude) * fraction + from.latitude
        val lng = (to.longitude - from.longitude) * fraction + from.longitude
        return LatLng(lat, lng)
    }

    private fun setupWeatherFeature(view: View) {
        val fabWeather = view.findViewById<FloatingActionButton>(R.id.fabWeatherSmall)
        val cvWeatherPopup = view.findViewById<View>(R.id.cvWeatherPopupRide)
        val layoutWeatherDays = view.findViewById<android.widget.LinearLayout>(R.id.layoutWeatherDaysRide)
        val ivGlow = view.findViewById<View>(R.id.ivWeatherGlowRide)

        // Weather Outline (Static Blue)
        // Animation removed as requested.

        fun refreshWeather() {
            val pickup = pickupLocation ?: LatLng(31.5204, 74.3587)
            lifecycleScope.launch {
                try {
                    val forecast = MockDataRepository.getLiveWeather(pickup.latitude, pickup.longitude)
                    layoutWeatherDays.removeAllViews()
                    forecast.forEachIndexed { index, weather ->
                        val row = LayoutInflater.from(requireContext()).inflate(R.layout.item_weather_mini, layoutWeatherDays, false)
                        val tvDay = row.findViewById<TextView>(R.id.tvDay)
                        val tvTemp = row.findViewById<TextView>(R.id.tvTemp)
                        val ivIcon = row.findViewById<ImageView>(R.id.ivWeatherIcon)
                        
                        tvDay.text = weather.day
                        tvTemp.text = weather.temp
                        ivIcon.setImageResource(weather.iconRes)
                        
                        // Highlight Present Day (First item)
                        if (index == 0) {
                            row.setBackgroundResource(R.drawable.bg_weather_today)
                            tvDay.setTextColor(android.graphics.Color.WHITE)
                            tvTemp.setTextColor(android.graphics.Color.WHITE)
                            ivIcon.setColorFilter(android.graphics.Color.WHITE)
                        }
                        
                        layoutWeatherDays.addView(row)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Initial fetch
        refreshWeather()

        fabWeather.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            if (cvWeatherPopup.visibility == View.VISIBLE) {
                // Animate Out (Slide Down)
                cvWeatherPopup.animate()
                    .alpha(0f)
                    .translationY(20f)
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(200)
                    .withEndAction { cvWeatherPopup.visibility = View.GONE }
                    .start()
            } else {
                // Refresh data
                refreshWeather()

                // Animate In (Slide Up)
                cvWeatherPopup.visibility = View.VISIBLE
                cvWeatherPopup.alpha = 0f
                cvWeatherPopup.translationY = 40f
                cvWeatherPopup.scaleX = 0.8f
                cvWeatherPopup.scaleY = 0.8f
                
                cvWeatherPopup.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(350)
                    .setInterpolator(android.view.animation.OvershootInterpolator(1.2f))
                    .start()
            }
        }
    }
}

