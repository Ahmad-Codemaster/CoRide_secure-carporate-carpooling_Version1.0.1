package com.coride.ui.home

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.coride.R
import com.coride.data.model.Place
import com.coride.data.model.PlaceType
import com.coride.data.repository.MockDataRepository
import com.coride.ui.common.LocationHelper
import com.coride.ui.common.PlacesAutocompleteHelper
import com.coride.ui.common.SpringPhysicsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class HomeMapFragment : Fragment() {

    private var googleMap: GoogleMap? = null
    private var geocodeJob: Job? = null
    private var autocompleteHelper: PlacesAutocompleteHelper? = null
    private var currentAddress: String = "Selected Location"
    private var isManualPickMode = false

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (fineGranted) {
            fetchLiveLocation()
        } else {
            val shouldShowRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            if (!shouldShowRationale) {
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
        autocompleteHelper = PlacesAutocompleteHelper(requireContext())
        setupMap()
        setupSearch(view)

        view.findViewById<FloatingActionButton>(R.id.fabMyLocation).setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            fetchLiveLocation()
        }

        view.findViewById<MaterialButton>(R.id.btnSaveLocation).setOnClickListener {
            val target = googleMap?.cameraPosition?.target
            if (target != null) {
                val newPlace = Place(
                    id = "place_saved_${System.currentTimeMillis()}",
                    name = currentAddress.take(20),
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

        if (LocationHelper.hasLocationPermission(requireContext())) {
            fetchLiveLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
        setupWeatherFeature(view)
    }

    private fun setupSearch(view: View) {
        val searchBar = view.findViewById<SearchBar>(R.id.mapSearchBar)
        val searchView = view.findViewById<SearchView>(R.id.mapSearchView)
        val rvSuggestions = view.findViewById<RecyclerView>(R.id.rvSearchSuggestions)
        val btnPickOnMap = view.findViewById<View>(R.id.btnPickOnMap)
        val ivCenterPin = view.findViewById<View>(R.id.ivCenterPin)

        searchBar.setNavigationOnClickListener { findNavController().navigateUp() }

        val searchAdapter = PlaceAdapter(emptyList()) { place ->
            // Resolve exact coordinates for the selected place
            autocompleteHelper?.resolvePlace(place.id) { latLng ->
                if (latLng != null) {
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                    currentAddress = place.address
                    searchBar.setText(place.name)
                    searchView.hide()
                    disableManualPickMode(ivCenterPin)
                }
            }
        }

        rvSuggestions.layoutManager = LinearLayoutManager(requireContext())
        rvSuggestions.adapter = searchAdapter

        searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                autocompleteHelper?.search(query) { results ->
                    searchAdapter.updatePlaces(results)
                }
            }
        })

        btnPickOnMap.setOnClickListener {
            enableManualPickMode(ivCenterPin)
            searchView.hide()
            searchBar.setText("Manual Pin Mode")
        }
    }

    private fun enableManualPickMode(ivCenterPin: View) {
        isManualPickMode = true
        ivCenterPin.visibility = View.VISIBLE
        SpringPhysicsHelper.springSlideUpFadeIn(ivCenterPin, 40f, 1f)
        Toast.makeText(requireContext(), "Drag map to pick exactly", Toast.LENGTH_SHORT).show()
    }

    private fun disableManualPickMode(ivCenterPin: View) {
        isManualPickMode = false
        ivCenterPin.visibility = View.GONE
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.homeMapView) as? SupportMapFragment
        mapFragment?.getMapAsync { map ->
            googleMap = map
            map.uiSettings.isZoomControlsEnabled = false
            map.uiSettings.isMyLocationButtonEnabled = false

            map.setOnCameraMoveStartedListener {
                if (isManualPickMode) {
                    view?.findViewById<SearchBar>(R.id.mapSearchBar)?.setText("Locating...")
                    view?.findViewById<View>(R.id.ivCenterPin)?.animate()?.translationY(-40f)?.setDuration(150)?.start()
                }
            }

            map.setOnCameraIdleListener {
                if (isManualPickMode) {
                    view?.findViewById<View>(R.id.ivCenterPin)?.animate()?.translationY(0f)?.setDuration(150)?.start()
                    geocodeLiveAddress(map.cameraPosition.target)
                }
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
                } catch (_: Exception) {
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
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
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
                        view?.findViewById<SearchBar>(R.id.mapSearchBar)?.setText(currentAddress)
                    }
                }
            } catch (_: Exception) {
                launch(Dispatchers.Main) {
                    if (isAdded) {
                        currentAddress = "Selected Location"
                        view?.findViewById<SearchBar>(R.id.mapSearchBar)?.setText(currentAddress)
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
                            tvDay.setTextColor(Color.WHITE)
                            tvTemp.setTextColor(Color.WHITE)
                            ivIcon.setColorFilter(Color.WHITE)
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
            if (cvWeatherPopup.isVisible) {
                // Animate Out
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
                
                // Animate In
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
                    .setInterpolator(OvershootInterpolator(1.2f))
                    .start()
            }
        }
    }

    override fun onDestroyView() {
        geocodeJob?.cancel()
        autocompleteHelper?.cancel()
        googleMap = null
        super.onDestroyView()
    }
}
