package com.coride.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvHistory = view.findViewById<RecyclerView>(R.id.rvHistory)
        val tvEmptyState = view.findViewById<android.widget.TextView>(R.id.tvEmptyState)
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
        
        adapter = RideHistoryAdapter(initialData) { ride ->
            MockDataRepository.deleteRide(ride)
            val updatedData = MockDataRepository.getRideHistory()
            adapter.updateData(updatedData)
            checkEmptyState(updatedData.size)
            rvHistory.scheduleLayoutAnimation()
        }
        rvHistory.adapter = adapter

        // Staggered Spring Entrance
        view.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                playEntranceAnimations(rvHistory, tvEmptyState)
            }
        })
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

