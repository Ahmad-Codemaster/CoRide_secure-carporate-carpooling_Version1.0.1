package com.coride.ui.booking

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
import com.coride.R
import com.coride.data.model.DriverOffer
import com.coride.data.repository.MockDataRepository
import com.coride.ui.common.SpringPhysicsHelper
import kotlinx.coroutines.launch

/**
 * Driver Offers Screen - Monochrome bidding focus.
 * Displays the current fare request and real-time bids from institutional drivers.
 */
class DriverOffersFragment : Fragment() {

    private lateinit var adapter: DriverOfferAdapter

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

        view.findViewById<TextView>(R.id.tvYourFare).text = "PKR ${fare.toInt()}"
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

        // Simulate searching for drivers
        viewLifecycleOwner.lifecycleScope.launch {
            val offers = MockDataRepository.generateDriverOffers(fare, pickupLat, pickupLng)

            searchingContainer.visibility = View.GONE
            rvOffers.visibility = View.VISIBLE
            adapter.updateOffers(offers)

            // Staggered spring entrance for offer cards
            rvOffers.post {
                val cardViews = (0 until rvOffers.childCount).mapNotNull { rvOffers.getChildAt(it) }
                SpringPhysicsHelper.staggerSpringEntrance(cardViews, staggerDelayMs = 70L)
            }
        }
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

