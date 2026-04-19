package com.coride.ui.home

import android.location.Geocoder
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_place, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autocompleteHelper = PlacesAutocompleteHelper(requireContext())
        
        // Try to get current location for biasing results (Professional feature)
        LocationHelper.getCurrentLocation(requireContext()) { loc ->
            loc?.let { currentLocation = com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude) }
        }

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { findNavController().navigateUp() }

        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val rvResults = view.findViewById<RecyclerView>(R.id.rvSearchResults)
        val btnPickOnMap = view.findViewById<View>(R.id.btnPickOnMap)
        val tvNoResults = view.findViewById<TextView>(R.id.tvNoResults)

        // Setup Chips (Professional Action Buttons)
        setupCategoryChips(view, etSearch)

        btnPickOnMap.setOnClickListener {
            findNavController().navigate(R.id.action_search_to_booking)
        }

        adapter = PlaceAdapter(MockDataRepository.getSavedPlaces()) { place ->
            if (place.latitude == 0.0 && place.longitude == 0.0) {
                // Fetch coords for Google result
                autocompleteHelper?.resolvePlace(place.id) { latLng ->
                    val bundle = bundleOf(
                        "destination_name" to place.name,
                        "destination_address" to place.address,
                        "destination_lat" to (latLng?.latitude ?: 0.0),
                        "destination_lng" to (latLng?.longitude ?: 0.0)
                    )
                    findNavController().navigate(R.id.action_search_to_booking, bundle)
                }
            } else {
                val bundle = bundleOf(
                    "destination_name" to place.name,
                    "destination_address" to place.address,
                    "destination_lat" to place.latitude,
                    "destination_lng" to place.longitude
                )
                findNavController().navigate(R.id.action_search_to_booking, bundle)
            }
        }

        rvResults.layoutManager = LinearLayoutManager(requireContext())
        rvResults.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                performSearch(query, tvNoResults)
            }
        })

        etSearch.requestFocus()
    }

    private fun setupCategoryChips(view: View, etSearch: EditText) {
        val chips = listOf(
            view.findViewById<com.google.android.material.chip.Chip>(R.id.chipRestaurant) to "Restaurants",
            view.findViewById<com.google.android.material.chip.Chip>(R.id.chipATM) to "ATM",
            view.findViewById<com.google.android.material.chip.Chip>(R.id.chipPetrol) to "Petrol",
            view.findViewById<com.google.android.material.chip.Chip>(R.id.chipHospital) to "Hospital"
        )
        
        chips.forEach { (chip, query) ->
            chip?.setOnClickListener {
                etSearch.setText(query)
                etSearch.setSelection(query.length)
            }
        }
    }

    private fun performSearch(query: String, tvNoResults: TextView) {
        if (query.isEmpty()) {
            val saved = MockDataRepository.getSavedPlaces()
            adapter.updatePlaces(saved)
            tvNoResults.visibility = if (saved.isEmpty()) View.VISIBLE else View.GONE
            return
        }

        // Use the new Location-Biased search (Professional approach)
        autocompleteHelper?.search(query, currentLocation) { results ->
            if (!isAdded) return@search
            
            adapter.updatePlaces(results)
            tvNoResults.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
            
            if (results.isNotEmpty()) {
                val rv = view?.findViewById<RecyclerView>(R.id.rvSearchResults) ?: return@search
                rv.post {
                    val views = mutableListOf<View>()
                    for (i in 0 until minOf(results.size, 6)) {
                        rv.getChildAt(i)?.let { views.add(it) }
                    }
                    if (views.isNotEmpty()) {
                        views.forEach { 
                            it.alpha = 0f
                            it.translationY = 50f
                        }
                        SpringPhysicsHelper.staggerSpringEntrance(views, staggerDelayMs = 50L)
                    }
                }
            }
        }
    }
}

