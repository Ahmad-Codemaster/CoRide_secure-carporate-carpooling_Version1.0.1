package com.indrive.clone.ui.booking

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.indrive.clone.R
import com.indrive.clone.data.model.DriverOffer
import com.indrive.clone.data.repository.MockDataRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.indrive.clone.ui.common.SpringPhysicsHelper
import kotlinx.coroutines.launch

class DriverOffersFragment : Fragment() {

    private lateinit var adapter: DriverOfferAdapter
    private var googleMap: GoogleMap? = null
    private var pickupLatLng: LatLng? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_driver_offers, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fare = arguments?.getDouble("fare") ?: 300.0
        val destName = arguments?.getString("destination_name") ?: ""
        val destLat = arguments?.getDouble("destination_lat") ?: 0.0
        val destLng = arguments?.getDouble("destination_lng") ?: 0.0
        val pickupLat = arguments?.getDouble("pickup_lat") ?: 0.0
        val pickupLng = arguments?.getDouble("pickup_lng") ?: 0.0
        val rideType = arguments?.getString("ride_type") ?: "ECONOMY"

        pickupLatLng = if (pickupLat != 0.0) LatLng(pickupLat, pickupLng) else null

        view.findViewById<TextView>(R.id.tvYourFare).text = "Rs. ${fare.toInt()}"
        view.findViewById<TextView>(R.id.tvOfferDestination).text = destName

        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            findNavController().navigateUp()
        }

        val searchingContainer = view.findViewById<LinearLayout>(R.id.searchingContainer)
        val rvOffers = view.findViewById<RecyclerView>(R.id.rvOffers)

        adapter = DriverOfferAdapter(
            emptyList<DriverOffer>(),
            onAccept = { offer -> acceptOffer(offer, destName, destLat, destLng, pickupLat, pickupLng, rideType) },
            onDecline = { offer -> adapter.removeOffer(offer) }
        )

        rvOffers.layoutManager = LinearLayoutManager(requireContext())
        rvOffers.adapter = adapter

        setupMap()

        // Simulate searching for drivers — use smart matching
        viewLifecycleOwner.lifecycleScope.launch {
            val offers = MockDataRepository.generateDriverOffers(fare, pickupLat, pickupLng)

            searchingContainer.visibility = View.GONE
            rvOffers.visibility = View.VISIBLE
            adapter.updateOffers(offers)

            // Show drivers on map
            showDriversOnMap(offers)

            // Staggered spring entrance for offer cards
            rvOffers.post {
                val cardViews = (0 until rvOffers.childCount).mapNotNull { rvOffers.getChildAt(it) }
                SpringPhysicsHelper.staggerSpringEntrance(cardViews, staggerDelayMs = 70L)
            }
        }
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.offersMapView) as? SupportMapFragment
        mapFragment?.getMapAsync { map ->
            googleMap = map
            map.uiSettings.isZoomControlsEnabled = false
            pickupLatLng?.let {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 14f))
            }
        }
    }

    private fun showDriversOnMap(offers: List<DriverOffer>) {
        val map = googleMap ?: return
        val pickup = pickupLatLng ?: return

        map.clear()

        // User marker
        map.addMarker(MarkerOptions()
            .position(pickup)
            .title("Your Pickup")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))

        val boundsBuilder = LatLngBounds.Builder().include(pickup)

        // Driver markers
        offers.forEach { offer ->
            if (offer.driverLat != 0.0 && offer.driverLng != 0.0) {
                val driverPos = LatLng(offer.driverLat, offer.driverLng)
                map.addMarker(MarkerOptions()
                    .position(driverPos)
                    .title("${offer.driver.name} — ${offer.driver.vehicle.model}")
                    .snippet("${String.format("%.1f", offer.distance)} km away")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
                boundsBuilder.include(driverPos)
            }
        }

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
    }

    private fun acceptOffer(
        offer: DriverOffer,
        destName: String,
        destLat: Double,
        destLng: Double,
        pickupLat: Double,
        pickupLng: Double,
        rideType: String
    ) {
        val bundle = bundleOf(
            "driver_name" to offer.driver.name,
            "driver_rating" to offer.driver.rating,
            "driver_trips" to offer.driver.totalTrips,
            "driver_phone" to offer.driver.phone,
            "vehicle_info" to "${offer.driver.vehicle.color} ${offer.driver.vehicle.make} ${offer.driver.vehicle.model}",
            "plate_number" to offer.driver.vehicle.plateNumber,
            "fare" to offer.offeredPrice,
            "eta" to offer.estimatedArrival,
            "destination_name" to destName,
            "destination_lat" to destLat,
            "destination_lng" to destLng,
            "pickup_lat" to pickupLat,
            "pickup_lng" to pickupLng,
            "driver_lat" to offer.driverLat,
            "driver_lng" to offer.driverLng
        )
        findNavController().navigate(R.id.action_offers_to_ride, bundle)
    }
}
