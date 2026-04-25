package com.coride.ui.common

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.coride.data.model.Place as CoRidePlace
import com.coride.data.model.PlaceType
import com.coride.data.repository.MockDataRepository
import com.coride.data.network.SafetyNetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Helper to perform location searches using OpenRouteService (ORS).
 * Replaced Google Places to avoid billing and legacy API issues.
 */
class PlacesAutocompleteHelper(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val orsKey = SafetyNetworkModule.getOrsKey()

    // Temporary cache for resolved coordinates from autocomplete
    private val coordCache = mutableMapOf<String, LatLng>()

    /**
     * Search both local mock data and OpenRouteService.
     */
    fun search(query: String, biasLocation: LatLng? = null, onResult: (List<CoRidePlace>) -> Unit) {
        if (query.isEmpty()) {
            onResult(MockDataRepository.getSavedPlaces())
            return
        }

        // 1. Search Local First (Saved/Mocked)
        val localResults = MockDataRepository.searchPlaces(query)
        
        // 2. Search OpenRouteService
        scope.launch {
            try {
                // 2.1 Decide on a focus point (biasing). Use current location if available, otherwise default to Lahore, Pakistan
                val biasLat = biasLocation?.latitude ?: 31.5204
                val biasLon = biasLocation?.longitude ?: 74.3587

                val response = withContext(Dispatchers.IO) {
                    SafetyNetworkModule.orsApi.getAutocomplete(
                        apiKey = orsKey,
                        text = query,
                        lat = biasLat,
                        lon = biasLon,
                        layers = "address,venue,neighbourhood,locality",
                        size = 12
                    )
                }

                if (response.isSuccessful && response.body() != null) {
                    val orsPlaces = response.body()!!.features?.mapNotNull { feature ->
                        val props = feature.properties
                        val geom = feature.geometry
                        val coords = geom?.coordinates // List<Double>: [lon, lat]
                        
                        if (props?.id != null) {
                            val latLng = if (coords != null && coords.size >= 2) LatLng(coords[1], coords[0]) else null
                            if (latLng != null) {
                                coordCache[props.id] = latLng
                            }

                            CoRidePlace(
                                id = props.id,
                                name = props.name ?: "Unknown Place",
                                address = props.label ?: "",
                                latitude = latLng?.latitude ?: 0.0,
                                longitude = latLng?.longitude ?: 0.0,
                                type = PlaceType.SEARCH_RESULT
                            )
                        } else null
                    } ?: emptyList()

                    onResult(localResults + orsPlaces)
                } else {
                    Log.e("PlacesHelper", "ORS Search failed with code: ${response.code()}")
                    onResult(localResults)
                }
            } catch (e: Exception) {
                Log.e("PlacesHelper", "Error in ORS search: ${e.message}")
                onResult(localResults)
            }
        }
    }

    /**
     * Fetch exact coordinates for a selected Place ID.
     */
    fun resolvePlace(placeId: String, onResolved: (LatLng?) -> Unit) {
        // 1. Check Mock data
        val mock = MockDataRepository.getPlaces().find { it.id == placeId }
        if (mock != null) {
            onResolved(LatLng(mock.latitude, mock.longitude))
            return
        }

        // 2. Check internal cache from previous search
        if (coordCache.containsKey(placeId)) {
            onResolved(coordCache[placeId])
            return
        }

        // 3. Fallback (should not happen often with ORS autocomplete)
        onResolved(null)
    }
}
