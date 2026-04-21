package com.coride.ui.ride

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.coride.R
import com.coride.data.model.RideState
import com.coride.data.repository.MockDataRepository
import com.coride.service.RideForegroundService
import com.coride.ui.common.DirectionsHelper
import com.coride.ui.common.SpringPhysicsHelper
import com.coride.utils.FirebaseSafetyHelper
import com.coride.utils.SmsSafetyHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class RideFragment : Fragment() {

    private var pickupLocation: LatLng? = null
    private var destinationLocation: LatLng? = null
    private var driverStartLocation: LatLng? = null
    private var googleMap: GoogleMap? = null
    private var driverMarker: Marker? = null

    private var approachPath: List<LatLng> = emptyList()
    private var safetyCheckShowing = false
    private var currentDriverPosition: LatLng? = null
    private var ridePath: List<LatLng> = emptyList()

    private var currentState: RideState = RideState.SearchingDrivers
    private var rideId: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ride, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS, android.Manifest.permission.SEND_SMS), 101)
        }

        // --- Data Extraction ---
        val driverName = arguments?.getString("driver_name") ?: "Driver"
        val driverRating = arguments?.getFloat("driver_rating") ?: 4.8f
        val driverPhone = arguments?.getString("driver_phone") ?: ""
        val vehicleInfo = arguments?.getString("vehicle_info") ?: ""
        val plateNumber = arguments?.getString("plate_number") ?: ""
        val fare = arguments?.getDouble("fare") ?: 300.0
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
        rideId = "ride_${System.currentTimeMillis()}"

        // Populate Redesigned UI
        view.findViewById<TextView>(R.id.tvDriverName).text = driverName
        view.findViewById<TextView>(R.id.tvDriverRating).text = "($driverRating)"
        view.findViewById<TextView>(R.id.tvVehicleInfo).text = vehicleInfo
        view.findViewById<TextView>(R.id.tvPlateNumber).text = plateNumber
        view.findViewById<TextView>(R.id.tvDestination).text = destName
        view.findViewById<TextView>(R.id.tvFare).text = "₨ ${fare.toInt()}"

        setupPaths()
        setupButtons(view, driverName, driverPhone, vehicleInfo, plateNumber, destName, fare, driverRating)
        setupMap()
        setupWeatherFeature(view)

        // Staggered Entrance Animations
        animateEntrance(view)

        startRideLifecycle()
    }

    private fun animateEntrance(view: View) {
        val statusHUD = view.findViewById<View>(R.id.statusChipContainer)
        val safetyPill = view.findViewById<View>(R.id.cvSafetyPill)
        val bottomPanel = view.findViewById<View>(R.id.rideBottomSheet)

        statusHUD.alpha = 0f
        statusHUD.translationY = -60f
        statusHUD.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(200).start()

        safetyPill.alpha = 0f
        safetyPill.translationX = 60f
        safetyPill.animate().alpha(1f).translationX(0f).setDuration(500).setStartDelay(400).start()

        bottomPanel.translationY = 400f
        bottomPanel.animate().translationY(0f).setDuration(600).setStartDelay(100).start()
    }

    private fun setupPaths() {
        viewLifecycleOwner.lifecycleScope.launch {
            driverStartLocation?.let { dStart ->
                pickupLocation?.let { pLoc ->
                    approachPath = DirectionsHelper.generateApproachPath(dStart, pLoc, 40)
                    googleMap?.let { drawInitialMap(it) }
                }
            }
            pickupLocation?.let { pLoc ->
                destinationLocation?.let { dLoc ->
                    val route = DirectionsHelper.generateRoute(pLoc, dLoc, 60)
                    ridePath = route.polylinePoints
                    googleMap?.let { drawInitialMap(it) }
                }
            }
        }
    }

    private fun setupButtons(view: View, driverName: String, driverPhone: String, vehicleInfo: String, plateNumber: String, destName: String, fare: Double, driverRating: Float) {
        val fabShare = view.findViewById<FloatingActionButton>(R.id.fabShare)
        val fabSos = view.findViewById<FloatingActionButton>(R.id.fabSos)
        val fabSosInfo = view.findViewById<FloatingActionButton>(R.id.fabSosInfo)
        val cvSosInfoPopup = view.findViewById<View>(R.id.cvSosInfoPopup)
        val btnCancelRide = view.findViewById<MaterialButton>(R.id.btnCancelRide)

        view.findViewById<View>(R.id.btnCall).setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            try { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$driverPhone"))) } catch (e: Exception) {}
        }

        view.findViewById<View>(R.id.btnMessage).setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            Toast.makeText(requireContext(), "Opening chat with $driverName", Toast.LENGTH_SHORT).show()
        }

        fabShare.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            val shareText = "🚗 CoRide Safety Share\n\nI'm riding with $driverName (✅ Verified)\nVehicle: $vehicleInfo ($plateNumber)\nHeading to: $destName\n\nTrack me on CoRide!"
            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText) }
            startActivity(Intent.createChooser(intent, "Share ride with..."))
        }

        fabSos.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            val sosDialog = SosDialogFragment.newInstance(rideId, currentDriverPosition?.latitude ?: 0.0, currentDriverPosition?.longitude ?: 0.0)
            sosDialog.show(childFragmentManager, "sos_dialog")
        }

        fabSosInfo.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            if (cvSosInfoPopup.visibility == View.VISIBLE) {
                cvSosInfoPopup.animate().alpha(0f).translationX(-20f).setDuration(200).withEndAction { cvSosInfoPopup.visibility = View.GONE }.start()
            } else {
                cvSosInfoPopup.visibility = View.VISIBLE
                cvSosInfoPopup.alpha = 0f
                cvSosInfoPopup.translationX = -20f
                cvSosInfoPopup.animate().alpha(1f).translationX(0f).setDuration(300).setInterpolator(android.view.animation.OvershootInterpolator()).start()
            }
        }

        btnCancelRide.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            handleRideAction(driverName, driverRating, fare, destName, vehicleInfo)
        }
    }

    private fun handleRideAction(driverName: String, driverRating: Float, fare: Double, destName: String, vehicleInfo: String) {
        when (currentState) {
            is RideState.RideCompleted -> {
                val bundle = Bundle().apply {
                    putString("driver_name", driverName); putFloat("driver_rating", driverRating); putDouble("fare", fare)
                    putString("destination_name", destName); putString("vehicle_info", vehicleInfo)
                    putDouble("distance_km", 2.4); putInt("duration_seconds", 600)
                }
                findNavController().navigate(R.id.action_ride_to_complete, bundle)
            }
            is RideState.RideInProgress -> { Toast.makeText(requireContext(), "Ride is ongoing.", Toast.LENGTH_SHORT).show() }
            else -> { findNavController().popBackStack(R.id.homeFragment, false) }
        }
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.rideMapView) as? SupportMapFragment
        mapFragment?.getMapAsync { map ->
            googleMap = map
            map.uiSettings.isMapToolbarEnabled = false
            map.uiSettings.isZoomControlsEnabled = false
            drawInitialMap(map)
        }
    }

    private fun drawInitialMap(map: GoogleMap) {
        map.clear()
        driverMarker = null
        val mapStyle = """[ { "elementType": "geometry", "stylers": [ { "color": "#f5f5f5" } ] } ]"""
        map.setMapStyle(MapStyleOptions(mapStyle))
        pickupLocation?.let { map.addMarker(MarkerOptions().position(it).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))) }
        destinationLocation?.let { dLoc ->
            map.addMarker(MarkerOptions().position(dLoc).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)))
            if (ridePath.isNotEmpty()) map.addPolyline(PolylineOptions().addAll(ridePath).width(12f).color(android.graphics.Color.GRAY))
            if (approachPath.isNotEmpty()) map.addPolyline(PolylineOptions().addAll(approachPath).width(12f).color(android.graphics.Color.BLACK))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startRideLifecycle() {
        viewLifecycleOwner.lifecycleScope.launch {
            // --- PHASE 1: Driver Arriving ---
            updateState(RideState.DriverArriving(0f, 5))
            val currentApproach = approachPath.toList()
            val steps = if (currentApproach.isNotEmpty()) currentApproach.size else 50
            for (i in 0 until steps) {
                if (!isAdded) return@launch
                val pos = if (currentApproach.isNotEmpty()) currentApproach[i] else interpolateLatLng(driverStartLocation ?: pickupLocation!!, pickupLocation!!, i.toFloat() / steps)
                updateDriverMarker(pos)
                updateState(RideState.DriverArriving(2.0f * (1 - i.toFloat() / steps), maxOf(1, (5 * (1 - i.toFloat() / steps)).toInt())))
                delay(100)
            }

            // --- PHASE 2: Driver Arrived ---
            updateState(RideState.DriverArrived)
            delay(2000)

            // --- PHASE 3: Ride In Progress (Restored Safety Simulations) ---
            updateState(RideState.RideInProgress(0f, 10))
            
            // Re-enable Auto-SMS Alerts
            val driverNameVal = arguments?.getString("driver_name") ?: "Driver"
            val vehicleInfoVal = arguments?.getString("vehicle_info") ?: ""
            val plateNumberVal = arguments?.getString("plate_number") ?: ""
            val destNameVal = arguments?.getString("destination_name") ?: ""
            val pLat = pickupLocation?.latitude ?: 0.0
            val pLng = pickupLocation?.longitude ?: 0.0
            val dLat = destinationLocation?.latitude ?: 0.0
            val dLng = destinationLocation?.longitude ?: 0.0

            if (SmsSafetyHelper.hasSmsPermission(requireContext())) {
                val rideMsg = SmsSafetyHelper.buildRideStartMessage(rideId, driverNameVal, vehicleInfoVal, plateNumberVal, destNameVal, dLat, dLng)
                val count = SmsSafetyHelper.sendToAllEmergencyContacts(requireContext(), rideMsg)
                if (count > 0) Toast.makeText(requireContext(), "✅ Safety alert sent to $count contacts", Toast.LENGTH_SHORT).show()
                Log.d("RideSafety", "Auto-sent SMS to $count contacts")
            }

            RideForegroundService.startService(requireContext(), rideId, driverNameVal, destNameVal, pLat, pLng)

            val currentRide = ridePath.toList()
            val rideSteps = if (currentRide.isNotEmpty()) currentRide.size else 80
            val safetyTriggerStep = (rideSteps * 0.4).toInt()
            val deviationTriggerStep = (rideSteps * 0.65).toInt()
            
            for (i in 0 until rideSteps) {
                if (!isAdded) return@launch
                
                var pos = if (currentRide.isNotEmpty()) currentRide[i] else interpolateLatLng(pickupLocation!!, destinationLocation!!, i.toFloat() / rideSteps)

                // RESTORE DEVIATION SIMULATION
                if (i == deviationTriggerStep) {
                    showDeviationWarning()
                    pos = LatLng(pos.latitude + 0.001, pos.longitude + 0.001) // Offset position
                }

                currentDriverPosition = pos
                updateDriverMarker(pos)
                FirebaseSafetyHelper.pushLocationUpdate(rideId, "driver", pos.latitude, pos.longitude)
                updateState(RideState.RideInProgress(i.toFloat() / rideSteps, maxOf(1, (10 * (1 - i.toFloat() / rideSteps)).toInt())))

                // RESTORE SAFETY STOP SIMULATION
                if (i == safetyTriggerStep && !safetyCheckShowing) {
                    safetyCheckShowing = true
                    Log.d("RideSafety", "Simulating ride stop for safety check...")
                    delay(10000) // 10 second stop
                    if (isAdded) {
                        val safetyDialog = SafetyCheckDialogFragment.newInstance(rideId, pos.latitude, pos.longitude)
                        safetyDialog.onDismissedSafe = { safetyCheckShowing = false }
                        safetyDialog.show(childFragmentManager, "safety_check")
                    }
                }

                delay(120)
            }
            updateState(RideState.RideCompleted)
            RideForegroundService.stopService(requireContext())
        }
    }

    private fun updateState(state: RideState) {
        if (!isAdded) return
        currentState = state
        val tvStatus = view?.findViewById<TextView>(R.id.tvRideStatus)
        val tvEta = view?.findViewById<TextView>(R.id.tvEtaStatus)
        val btnCancel = view?.findViewById<MaterialButton>(R.id.btnCancelRide)

        when (state) {
            is RideState.DriverArriving -> { tvStatus?.text = "Driver Arriving"; tvEta?.text = "• ETA: ${state.etaMin} min"; btnCancel?.text = "Cancel Ride" }
            is RideState.DriverArrived -> { tvStatus?.text = "Driver is here! 🚗"; tvEta?.text = "• Arrived" }
            is RideState.RideInProgress -> { 
                tvStatus?.text = "Ride in Progress"; tvEta?.text = "• ETA: ${state.etaMin} min"
                btnCancel?.text = "Ride Ongoing..."
                btnCancel?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_container_highest))
            }
            is RideState.RideCompleted -> { tvStatus?.text = "Destination Reached! ✅"; tvEta?.text = "• Finished"; btnCancel?.text = "Finish Ride" }
            else -> {}
        }
    }

    private fun updateDriverMarker(position: LatLng) {
        val map = googleMap ?: return
        if (driverMarker == null) {
            driverMarker = map.addMarker(MarkerOptions().position(position).title("Driver").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
        } else {
            driverMarker?.position = position
        }

        // Smart Tracking: Auto-adjust camera to keep both driver and target (pickup or destination) in view
        updateCameraTracking(position)
    }

    private fun updateCameraTracking(currentPos: LatLng) {
        val map = googleMap ?: return
        val builder = LatLngBounds.Builder()
        builder.include(currentPos)
        
        // Include the target point (Pickup if arriving, Destination if in progress)
        val target = if (currentState is RideState.DriverArriving) {
            pickupLocation
        } else {
            destinationLocation
        }
        
        target?.let { 
            builder.include(it)
            val bounds = builder.build()
            // Padding of 200px to ensure markers aren't on the extreme edge
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
        } ?: run {
            // Fallback if no target: just center on driver
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 16f))
        }
    }

    private fun interpolateLatLng(from: LatLng, to: LatLng, fraction: Float): LatLng {
        return LatLng((to.latitude - from.latitude) * fraction + from.latitude, (to.longitude - from.longitude) * fraction + from.longitude)
    }

    private fun setupWeatherFeature(view: View) {
        val fabWeather = view.findViewById<FloatingActionButton>(R.id.fabWeatherSmall)
        val cvWeatherPopup = view.findViewById<View>(R.id.cvWeatherPopupRide)
        val layoutWeatherDays = view.findViewById<android.widget.LinearLayout>(R.id.layoutWeatherDaysRide)

        fabWeather.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            if (cvWeatherPopup.visibility == View.VISIBLE) {
                cvWeatherPopup.animate().alpha(0f).translationX(20f).setDuration(200).withEndAction { cvWeatherPopup.visibility = View.GONE }.start()
            } else {
                cvWeatherPopup.visibility = View.VISIBLE
                cvWeatherPopup.alpha = 0f
                cvWeatherPopup.translationX = 20f
                cvWeatherPopup.animate().alpha(1f).translationX(0f).setDuration(350).setInterpolator(android.view.animation.OvershootInterpolator(1.2f)).start()
                
                lifecycleScope.launch {
                    val pickup = pickupLocation ?: LatLng(31.5204, 74.3587)
                    val forecast = MockDataRepository.getLiveWeather(pickup.latitude, pickup.longitude)
                    layoutWeatherDays.removeAllViews()
                    forecast.forEachIndexed { index, weather ->
                        val row = LayoutInflater.from(requireContext()).inflate(R.layout.item_weather_mini, layoutWeatherDays, false)
                        val tvDay = row.findViewById<TextView>(R.id.tvDay)
                        val tvTemp = row.findViewById<TextView>(R.id.tvTemp)
                        val ivIcon = row.findViewById<ImageView>(R.id.ivWeatherIcon)
                        tvDay.text = weather.day; tvTemp.text = weather.temp; ivIcon.setImageResource(weather.iconRes)
                        
                        // COLORFUL HIGHLIGHT for Current Day
                        if (index == 0) {
                            row.setBackgroundResource(R.drawable.bg_weather_today)
                            tvDay.setTextColor(android.graphics.Color.WHITE)
                            tvTemp.setTextColor(android.graphics.Color.WHITE)
                            ivIcon.setColorFilter(android.graphics.Color.WHITE)
                        } else {
                            ivIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary))
                        }
                        layoutWeatherDays.addView(row)
                    }
                }
            }
        }
    }

    private fun showDeviationWarning() {
        val warningCard = view?.findViewById<View>(R.id.cvDeviationWarning) ?: return
        warningCard.visibility = View.VISIBLE
        warningCard.alpha = 0f; warningCard.translationY = -40f
        warningCard.animate().alpha(1f).translationY(0f).setDuration(400).setInterpolator(android.view.animation.OvershootInterpolator()).withEndAction {
            warningCard.postDelayed({ if (isAdded) warningCard.animate().alpha(0f).translationY(-40f).setDuration(400).withEndAction { warningCard.visibility = View.GONE }.start() }, 5000)
        }.start()
        Toast.makeText(requireContext(), "Safety Alert: Route deviation detected", Toast.LENGTH_SHORT).show()
    }
}
