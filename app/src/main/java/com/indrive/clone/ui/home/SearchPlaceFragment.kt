package com.indrive.clone.ui.home

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
import com.indrive.clone.R
import com.indrive.clone.data.model.Place
import com.indrive.clone.data.model.PlaceType
import com.indrive.clone.data.repository.MockDataRepository
import java.util.Locale

class SearchPlaceFragment : Fragment() {

    private lateinit var adapter: PlaceAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_place, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { findNavController().navigateUp() }

        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val rvResults = view.findViewById<RecyclerView>(R.id.rvSearchResults)
        val btnPickOnMap = view.findViewById<View>(R.id.btnPickOnMap)
        val tvNoResults = view.findViewById<TextView>(R.id.tvNoResults)

        btnPickOnMap.setOnClickListener {
            // Navigate to booking without a destination → user picks on map
            findNavController().navigate(R.id.action_search_to_booking)
        }

        adapter = PlaceAdapter(MockDataRepository.getSavedPlaces()) { place ->
            val bundle = bundleOf(
                "destination_name" to place.name,
                "destination_address" to place.address,
                "destination_lat" to place.latitude,
                "destination_lng" to place.longitude
            )
            findNavController().navigate(R.id.action_search_to_booking, bundle)
        }

        rvResults.layoutManager = LinearLayoutManager(requireContext())
        rvResults.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                if (query.isEmpty()) {
                    val saved = MockDataRepository.getSavedPlaces()
                    adapter.updatePlaces(saved)
                    tvNoResults.visibility = if (saved.isEmpty()) View.VISIBLE else View.GONE
                    return
                }

                // First check mock places
                val mockResults = MockDataRepository.searchPlaces(query)

                if (mockResults.isNotEmpty()) {
                    adapter.updatePlaces(mockResults)
                    tvNoResults.visibility = View.GONE
                } else if (query.length >= 3) {
                    // Use Geocoder for real-world search
                    searchWithGeocoder(query, tvNoResults)
                } else {
                    adapter.updatePlaces(emptyList())
                    tvNoResults.visibility = View.VISIBLE
                }
            }
        })

        etSearch.requestFocus()
    }

    private fun searchWithGeocoder(query: String, tvNoResults: TextView) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(query, 8)

            if (addresses.isNullOrEmpty()) {
                adapter.updatePlaces(emptyList())
                tvNoResults.visibility = View.VISIBLE
                return
            }

            val places = addresses.mapIndexed { index, addr ->
                val name = addr.featureName ?: addr.locality ?: query
                val address = addr.getAddressLine(0) ?: "${addr.locality}, ${addr.countryName}"
                Place(
                    id = "geo_${index}_${query.hashCode()}",
                    name = name,
                    address = address,
                    latitude = addr.latitude,
                    longitude = addr.longitude,
                    type = PlaceType.SEARCH_RESULT
                )
            }

            adapter.updatePlaces(places)
            tvNoResults.visibility = View.GONE
        } catch (e: Exception) {
            // Geocoder failed (no internet, etc.) — show empty
            adapter.updatePlaces(emptyList())
            tvNoResults.visibility = View.VISIBLE
        }
    }
}
