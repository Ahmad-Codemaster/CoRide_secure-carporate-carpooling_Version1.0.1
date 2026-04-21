package com.coride.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coride.R
import com.coride.data.repository.MockDataRepository
import com.coride.ui.common.PlacesAutocompleteHelper
import com.coride.ui.common.LocationHelper
import com.coride.ui.common.SpringPhysicsHelper

class SearchPlaceFragment : Fragment() {

    private lateinit var adapter: PlaceAdapter
    private var autocompleteHelper: PlacesAutocompleteHelper? = null
    private var currentLocation: com.google.android.gms.maps.model.LatLng? = null
    
    private var rvResults: RecyclerView? = null
    private var tvNoResults: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_place, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autocompleteHelper = PlacesAutocompleteHelper(requireContext())
        
        LocationHelper.getCurrentLocation(requireContext()) { loc ->
            loc?.let { currentLocation = com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude) }
        }

        // --- View Bindings ---
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        rvResults = view.findViewById(R.id.rvSearchResults)
        tvNoResults = view.findViewById(R.id.tvNoResults)

        // Navigation
        btnBack.setOnClickListener { findNavController().navigateUp() }

        // Setup Colorful Category Pills
        setupCategoryPills(view, etSearch)

        // --- Adapter Setup ---
        adapter = PlaceAdapter(MockDataRepository.getSavedPlaces()) { place ->
            if (place.latitude == 0.0 && place.longitude == 0.0) {
                // Fetch coords for ORS/Google result if needed
                autocompleteHelper?.resolvePlace(place.id) { latLng ->
                    navigateToBooking(place.name, place.address, latLng?.latitude ?: 0.0, latLng?.longitude ?: 0.0)
                }
            } else {
                navigateToBooking(place.name, place.address, place.latitude, place.longitude)
            }
        }

        rvResults?.layoutManager = LinearLayoutManager(requireContext())
        rvResults?.adapter = adapter

        // --- Search Logic ---
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                performSearch(query)
            }
        })

        etSearch.requestFocus()
    }

    private fun setupCategoryPills(view: View, etSearch: EditText) {
        val pills = listOf(
            view.findViewById<View>(R.id.pillRestaurant) to "Restaurants",
            view.findViewById<View>(R.id.pillATM) to "ATM",
            view.findViewById<View>(R.id.pillPetrol) to "Petrol",
            view.findViewById<View>(R.id.pillHospital) to "Hospital"
        )
        
        pills.forEach { (pill, query) ->
            pill?.setOnClickListener {
                etSearch.setText(query)
                etSearch.setSelection(query.length)
            }
        }
    }

    private fun navigateToBooking(name: String, address: String, lat: Double, lng: Double) {
        val bundle = bundleOf(
            "destination_name" to name,
            "destination_address" to address,
            "destination_lat" to lat,
            "destination_lng" to lng
        )
        findNavController().navigate(R.id.action_search_to_booking, bundle)
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            val saved = MockDataRepository.getSavedPlaces()
            adapter.updatePlaces(saved)
            tvNoResults?.visibility = if (saved.isEmpty()) View.VISIBLE else View.GONE
            return
        }

        autocompleteHelper?.search(query, currentLocation) { results ->
            if (!isAdded) return@search
            
            adapter.updatePlaces(results)
            tvNoResults?.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
            
            if (results.isNotEmpty()) {
                rvResults?.post {
                    val rv = rvResults ?: return@post
                    val views = mutableListOf<View>()
                    for (i in 0 until minOf(results.size, 6)) {
                        rv.getChildAt(i)?.let { views.add(it) }
                    }
                    if (views.isNotEmpty()) {
                        SpringPhysicsHelper.staggerSpringEntrance(views, staggerDelayMs = 40L)
                    }
                }
            }
        }
    }
}
