package com.coride.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coride.R
import com.coride.data.repository.MockDataRepository

class HistoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    private lateinit var adapter: RideHistoryAdapter
    private lateinit var bottomSheetBehavior: com.google.android.material.bottomsheet.BottomSheetBehavior<android.view.View>
    private lateinit var scrim: android.view.View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvHistory = view.findViewById<RecyclerView>(R.id.rvHistory)
        val tvEmptyState = view.findViewById<android.widget.TextView>(R.id.tvEmptyState)
        
        // Setup Integrated Bottom Sheet
        scrim = view.findViewById(R.id.historyScrim)
        val bottomSheet = view.findViewById<View>(R.id.historyBottomSheet)
        bottomSheetBehavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehavior.addBottomSheetCallback(object : com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN) {
                    scrim.visibility = View.GONE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Fade scrim in from alpha 0 to 0.4 based on slide
                if (slideOffset > 0) {
                    scrim.visibility = View.VISIBLE
                    scrim.alpha = slideOffset * 0.4f
                } else if (slideOffset <= 0) {
                    scrim.alpha = 0f
                }
            }
        })

        // Also close on scrim click
        scrim.setOnClickListener {
            bottomSheetBehavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
        }

        view.findViewById<View>(R.id.btnCloseSheet).setOnClickListener {
            bottomSheetBehavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
        }

        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        
        fun checkEmptyState(dataCount: Int) {
            if (dataCount == 0) {
                rvHistory.visibility = View.GONE
                tvEmptyState.visibility = View.VISIBLE
            } else {
                rvHistory.visibility = View.VISIBLE
                tvEmptyState.visibility = View.GONE
            }
        }
        
        val initialData = MockDataRepository.getRideHistory()
        checkEmptyState(initialData.size)
        
        adapter = RideHistoryAdapter(
            initialData,
            onDeleteClick = { ride ->
                MockDataRepository.deleteRide(ride)
                val updatedData = MockDataRepository.getRideHistory()
                adapter.updateData(updatedData)
                checkEmptyState(updatedData.size)
                rvHistory.scheduleLayoutAnimation()
            },
            onReceiptClick = { ride ->
                showRideDetails(view, ride)
            }
        )
        rvHistory.adapter = adapter

        // Staggered Spring Entrance
        view.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                playEntranceAnimations(rvHistory, tvEmptyState)
            }
        })
    }

    private fun showRideDetails(view: View, ride: com.coride.data.model.Ride) {
        val farePrefix = getString(R.string.currency_symbol)
        
        view.findViewById<TextView>(R.id.tvSheetDate).text = "${ride.date} • ${ride.duration} min"
        view.findViewById<TextView>(R.id.tvSheetPickup).text = ride.pickup.name
        view.findViewById<TextView>(R.id.tvSheetDest).text = ride.destination.name
        
        // Fix: Prevent negative base fare calculation from screenshot
        val finalFareInt = ride.finalFare.toInt()
        val baseFareVal = if (finalFareInt > 50) finalFareInt - 50 else 0
        
        view.findViewById<TextView>(R.id.tvSheetBaseFare).text = "$farePrefix $baseFareVal"
        view.findViewById<TextView>(R.id.tvSheetTotalPaid).text = "$farePrefix $finalFareInt"
        
        bottomSheetBehavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
    }

    private fun playEntranceAnimations(rv: RecyclerView, emptyMsg: View) {
        if (rv.visibility == View.VISIBLE) {
            val items = mutableListOf<View>()
            for (i in 0 until 6) { // Animate first few items
                rv.getChildAt(i)?.let { items.add(it) }
            }
            if (items.isEmpty()) return

            // Initial state reset (Hidden -> Final)
            items.forEach { 
                it.alpha = 0f
                it.translationY = 100f
            }

            com.coride.ui.common.SpringPhysicsHelper.staggerSpringEntrance(
                items,
                staggerDelayMs = 90L,
                stiffness = 500f,
                dampingRatio = 0.75f
            )
        } else if (emptyMsg.visibility == View.VISIBLE) {
            emptyMsg.alpha = 0f
            emptyMsg.translationY = 60f
            emptyMsg.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(450)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
    }
}

