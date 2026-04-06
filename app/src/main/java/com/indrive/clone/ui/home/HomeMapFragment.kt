package com.indrive.clone.ui.home

import android.Manifest
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.indrive.clone.R
import com.indrive.clone.data.model.Place
import com.indrive.clone.data.model.PlaceType
import com.indrive.clone.data.repository.MockDataRepository
import com.indrive.clone.ui.common.LocationHelper
import com.indrive.clone.ui.common.SpringPhysicsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class HomeMapFragment : Fragment() {

    private var googleMap: GoogleMap? = null
    private var geocodeJob: Job? = null
    private var currentAddress: String = "Selected Location"

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
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
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
        return inflater.inflate(R.layout.fragment_home_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnBackContainer).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<FloatingActionButton>(R.id.fabMyLocation).setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            fetchLiveLocation()
        }

        view.findViewById<MaterialButton>(R.id.btnSaveLocation).setOnClickListener {
            val target = googleMap?.cameraPosition?.target
            if (target != null) {
                val newPlace = Place(
                    id = "place_saved_${System.currentTimeMillis()}",
                    name = currentAddress.take(20), // short name
                    address = currentAddress,
                    latitude = target.latitude,
                    longitude = target.longitude,
                    type = PlaceType.SAVED
                )
                MockDataRepository.addSavedPlace(newPlace)
                Toast.makeText(requireContext(), "Location Saved", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        setupMap()

        if (LocationHelper.hasLocationPermission(requireContext())) {
            fetchLiveLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }

        setupWeatherFeature(view)
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.homeMapView) as? SupportMapFragment
        mapFragment?.getMapAsync { map ->
            googleMap = map
            map.uiSettings.isZoomControlsEnabled = false
            map.uiSettings.isMyLocationButtonEnabled = false

            map.setOnCameraMoveStartedListener {
                view?.findViewById<TextView>(R.id.tvMapAddress)?.text = "Locating..."
                view?.findViewById<View>(R.id.ivCenterPin)?.animate()?.translationY(-40f)?.setDuration(150)?.start()
            }

            map.setOnCameraIdleListener {
                view?.findViewById<View>(R.id.ivCenterPin)?.animate()?.translationY(0f)?.setDuration(150)?.start()
                geocodeLiveAddress(map.cameraPosition.target)
            }
            
            map.setOnMapClickListener { latLng ->
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            }
            
            // Default center if no GPS
            val origin = LatLng(31.5204, 74.3587)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 14f))
        }
    }

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
                // GPS is ON, proceed to fetch
                LocationHelper.getCurrentLocation(requireContext()) { location ->
                    if (!isAdded) return@getCurrentLocation
                    hideLoading()
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                        geocodeLiveAddress(latLng)
                    } ?: run {
                        Toast.makeText(requireContext(), "Weak GPS signal. Tap again.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onResolutionRequired = { exception ->
                // Show system dialog to turn on GPS
                try {
                    val intentSenderRequest = androidx.activity.result.IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    gpsResolutionLauncher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    hideLoading()
                }
            },
            onFailure = {
                hideLoading()
                Toast.makeText(requireContext(), "GPS Settings error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showLoading() {
        view?.findViewById<View>(R.id.locationProgressBar)?.visibility = View.VISIBLE
        view?.findViewById<View>(R.id.fabMyLocation)?.isEnabled = false
        view?.findViewById<TextView>(R.id.tvMapAddress)?.text = "Locating you..."
    }

    private fun hideLoading() {
        view?.findViewById<View>(R.id.locationProgressBar)?.visibility = View.GONE
        view?.findViewById<View>(R.id.fabMyLocation)?.isEnabled = true
    }

    private fun showPermissionSettingsDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Location Permission Needed")
            .setMessage("Location access is permanently denied. Please enable it in app settings to use map features.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun geocodeLiveAddress(latLng: LatLng) {
        geocodeJob?.cancel()
        geocodeJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            delay(350)
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses.first()
                    currentAddress = addr.getAddressLine(0) ?: "Selected Location"
                } else {
                    currentAddress = "Selected Location"
                }

                launch(Dispatchers.Main) {
                    if (isAdded) {
                        view?.findViewById<TextView>(R.id.tvMapAddress)?.text = currentAddress
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    if (isAdded) {
                        currentAddress = "Selected Location"
                        view?.findViewById<TextView>(R.id.tvMapAddress)?.text = currentAddress
                    }
                }
            }
        }
    }

    private fun setupWeatherFeature(view: View) {
        val fabWeather = view.findViewById<FloatingActionButton>(R.id.fabWeatherMap)
        val cvWeatherPopup = view.findViewById<View>(R.id.cvWeatherPopupMap)
        val layoutWeatherDays = view.findViewById<LinearLayout>(R.id.layoutWeatherDaysMap)

        fun refreshWeather() {
            val target = googleMap?.cameraPosition?.target ?: LatLng(31.5204, 74.3587)
            lifecycleScope.launch {
                try {
                    val forecast = MockDataRepository.getLiveWeather(target.latitude, target.longitude)
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
                // Refresh data based on current map center
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
