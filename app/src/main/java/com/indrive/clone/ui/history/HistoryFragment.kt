package com.indrive.clone.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.indrive.clone.R
import com.indrive.clone.data.repository.MockDataRepository

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
    }
}
