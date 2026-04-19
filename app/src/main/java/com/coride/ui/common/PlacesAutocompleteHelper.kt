package com.coride.ui.common

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.coride.data.model.Place as CoRidePlace
import com.coride.data.model.PlaceType
import com.coride.data.repository.MockDataRepository

class PlacesAutocompleteHelper(context: Context) {

    private val placesClient: PlacesClient = Places.createClient(context)
    private var sessionToken: AutocompleteSessionToken? = null

    /**
     * Search both local mock data and Google Places.
     * @param location Current location to bias results (Google Maps feature)
     */
    fun search(query: String, biasLocation: LatLng? = null, onResult: (List<CoRidePlace>) -> Unit) {
        if (query.isEmpty()) {
            onResult(MockDataRepository.getSavedPlaces())
            return
        }

        // 1. Search Local First (Saved/Mocked)
        val localResults = MockDataRepository.searchPlaces(query)
        
        // 2. Search Google Places
        if (sessionToken == null) sessionToken = AutocompleteSessionToken.newInstance()
        
        val requestBuilder = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(sessionToken)
            .setQuery(query)

        // Apply Location Biasing (The "Professional" fix)
        // This ensures "Restaurant" shows local ones first
        biasLocation?.let { latLng ->
            val bias = com.google.android.libraries.places.api.model.RectangularBounds.newInstance(
                LatLng(latLng.latitude - 0.1, latLng.longitude - 0.1),
                LatLng(latLng.latitude + 0.1, latLng.longitude + 0.1)
            )
            requestBuilder.setLocationBias(bias)
        }
        
        placesClient.findAutocompletePredictions(requestBuilder.build())
            .addOnSuccessListener { response ->
                val googlePlaces = response.autocompletePredictions.map { prediction ->
                    CoRidePlace(
                        id = prediction.placeId,
                        name = prediction.getPrimaryText(null).toString(),
                        address = prediction.getSecondaryText(null).toString(),
                        latitude = 0.0, // LatLng to be fetched on selection
                        longitude = 0.0,
                        type = PlaceType.SEARCH_RESULT
                    )
                }
                
                // Combine and prioritize local results
                onResult(localResults + googlePlaces)
            }
            .addOnFailureListener {
                onResult(localResults) // Fallback to local only on network error
            }
    }

    /**
     * Fetch exact coordinates for a selected Place ID.
     */
    fun resolvePlace(placeId: String, onResolved: (LatLng?) -> Unit) {
        // If it's a mock place, we already have coords
        val mock = MockDataRepository.getPlaces().find { it.id == placeId }
        if (mock != null) {
            onResolved(LatLng(mock.latitude, mock.longitude))
            return
        }

        // Otherwise fetch from Google
        val fields = listOf(Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(placeId, fields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                onResolved(response.place.latLng)
                sessionToken = null // Reset token for next session
            }
            .addOnFailureListener {
                onResolved(null)
            }
    }
}
