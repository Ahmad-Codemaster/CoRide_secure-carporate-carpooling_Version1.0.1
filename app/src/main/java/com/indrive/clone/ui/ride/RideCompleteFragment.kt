package com.indrive.clone.ui.ride

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import android.transition.TransitionManager
import android.transition.AutoTransition
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.indrive.clone.R

class RideCompleteFragment : Fragment() {

    private var selectedRating = 5
    private var isPaid = false
    private var selectedPaymentMethod = "Cash"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ride_complete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val driverName = arguments?.getString("driver_name") ?: "Driver"
        val fare = arguments?.getDouble("fare") ?: 300.0
        val destName = arguments?.getString("destination_name") ?: ""
        val distKm = arguments?.getDouble("distance_km") ?: 0.0
        val durationSec = arguments?.getInt("duration_seconds") ?: 0

        val tvDestination = view.findViewById<TextView>(R.id.tvDestination)
        val tvTotalFare = view.findViewById<TextView>(R.id.tvTotalFare)
        val tvDistance = view.findViewById<TextView>(R.id.tvDistance)
        val tvDuration = view.findViewById<TextView>(R.id.tvDuration)
        val tvRateDriverName = view.findViewById<TextView>(R.id.tvRateDriverName)
        
        tvDestination?.text = destName
        tvTotalFare?.text = fare.toInt().toString()
        tvDistance?.text = String.format("%.1f", distKm)
        tvDuration?.text = (durationSec / 60).toString()
        tvRateDriverName?.text = "Rate $driverName"

        // --- PAYMENT UI COMPONENTS ---
        val layoutPayment = view.findViewById<View>(R.id.layoutPayment)
        val layoutRating = view.findViewById<View>(R.id.layoutRating)
        val btnDone = view.findViewById<MaterialButton>(R.id.btnDone)
        
        val cardCash = view.findViewById<MaterialCardView>(R.id.cardCash)
        val cardWallet = view.findViewById<MaterialCardView>(R.id.cardWallet)
        val cardVisa = view.findViewById<MaterialCardView>(R.id.cardVisa)
        
        val rbCash = view.findViewById<RadioButton>(R.id.rbCash)
        val rbWallet = view.findViewById<RadioButton>(R.id.rbWallet)
        val rbVisa = view.findViewById<RadioButton>(R.id.rbVisa)

        fun updatePaymentSelection(method: String) {
            selectedPaymentMethod = method
            
            // Update Visuals
            cardCash.apply {
                strokeColor = if (method == "Cash") ContextCompat.getColor(requireContext(), R.color.primary) else ContextCompat.getColor(requireContext(), R.color.outline_variant)
                strokeWidth = if (method == "Cash") 4 else 2
                setCardBackgroundColor(if (method == "Cash") ContextCompat.getColor(requireContext(), R.color.surface_container_low) else ContextCompat.getColor(requireContext(), R.color.surface_container_lowest))
            }
            cardWallet.apply {
                strokeColor = if (method == "Wallet") ContextCompat.getColor(requireContext(), R.color.primary) else ContextCompat.getColor(requireContext(), R.color.outline_variant)
                strokeWidth = if (method == "Wallet") 4 else 2
                setCardBackgroundColor(if (method == "Wallet") ContextCompat.getColor(requireContext(), R.color.surface_container_low) else ContextCompat.getColor(requireContext(), R.color.surface_container_lowest))
            }
            cardVisa.apply {
                strokeColor = if (method == "Card") ContextCompat.getColor(requireContext(), R.color.primary) else ContextCompat.getColor(requireContext(), R.color.outline_variant)
                strokeWidth = if (method == "Card") 4 else 2
                setCardBackgroundColor(if (method == "Card") ContextCompat.getColor(requireContext(), R.color.surface_container_low) else ContextCompat.getColor(requireContext(), R.color.surface_container_lowest))
            }
            
            rbCash.isChecked = (method == "Cash")
            rbWallet.isChecked = (method == "Wallet")
            rbVisa.isChecked = (method == "Card")
        }

        cardCash.setOnClickListener { updatePaymentSelection("Cash") }
        cardWallet.setOnClickListener { updatePaymentSelection("Wallet") }
        cardVisa.setOnClickListener { updatePaymentSelection("Card") }

        // --- PHASE TRANSITION LOGIC ---
        btnDone.setOnClickListener {
            if (!isPaid) {
                // PHASE 1 -> PHASE 2
                btnDone.isEnabled = false
                btnDone.text = "Processing Payment..."
                
                Handler(Looper.getMainLooper()).postDelayed({
                    TransitionManager.beginDelayedTransition(view as ViewGroup, AutoTransition())
                    layoutPayment.visibility = View.GONE
                    layoutRating.visibility = View.VISIBLE
                    
                    isPaid = true
                    btnDone.isEnabled = true
                    btnDone.text = "Submit Feedback"
                    btnDone.setIconResource(R.drawable.ic_star)
                }, 1500)
            } else {
                // PHASE 2 COMPLETE
                completeAndGoHome(driverName, fare, destName, selectedRating)
            }
        }

        // Star rating
        val stars = listOf<TextView>(
            view.findViewById(R.id.star1),
            view.findViewById(R.id.star2),
            view.findViewById(R.id.star3),
            view.findViewById(R.id.star4),
            view.findViewById(R.id.star5)
        )

        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                selectedRating = index + 1
                updateStars(stars, selectedRating)
            }
        }
    }

    private fun completeAndGoHome(driverName: String, fare: Double, destName: String, rating: Int) {
        val completedRide = com.indrive.clone.data.model.Ride(
            id = "ride_${System.currentTimeMillis()}",
            pickup = com.indrive.clone.data.model.Place("p1", "Pickup", "Current Location", 31.5204, 74.3587),
            destination = com.indrive.clone.data.model.Place("p2", destName, destName, 0.0, 0.0),
            driver = com.indrive.clone.data.repository.MockDataRepository.getDrivers().firstOrNull { it.name == driverName },
            status = com.indrive.clone.data.model.RideStatus.COMPLETED,
            requestedFare = fare,
            finalFare = fare,
            date = java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()),
            driverRating = rating.toFloat(),
            comment = "Paid via $selectedPaymentMethod"
        )
        com.indrive.clone.data.repository.MockDataRepository.addRide(completedRide)
        
        Toast.makeText(requireContext(), "Payment Successful via $selectedPaymentMethod", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_complete_to_home)
    }

    private fun updateStars(stars: List<TextView>, rating: Int) {
        stars.forEachIndexed { index, star ->
            if (index < rating) {
                star.setTextColor(ContextCompat.getColor(requireContext(), R.color.star_yellow))
            } else {
                star.setTextColor(ContextCompat.getColor(requireContext(), R.color.outline))
            }
        }
    }
}
