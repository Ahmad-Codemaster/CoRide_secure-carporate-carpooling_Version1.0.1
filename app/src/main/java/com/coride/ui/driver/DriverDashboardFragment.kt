package com.coride.ui.driver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.coride.R
import com.coride.data.repository.MockDataRepository
import com.coride.ui.common.SpringPhysicsHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class DriverDashboardFragment : Fragment() {

    private var isOnline = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_driver_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = MockDataRepository.getCurrentUser()
        val btnGoOnline = view.findViewById<MaterialButton>(R.id.btnGoOnline)
        val chipStatus = view.findViewById<Chip>(R.id.chipStatus)
        val tvSwitch = view.findViewById<TextView>(R.id.tvSwitchToPassenger)
        val tvName = view.findViewById<TextView>(R.id.tvDriverName)
        val tvVeh = view.findViewById<TextView>(R.id.tvVehicleInfo)

        tvName.text = user.name
        tvVeh.text = "${user.driverDetails?.vehicle?.make} ${user.driverDetails?.vehicle?.model} • ${user.driverDetails?.vehicle?.plateNumber}"

        updateStatusUI(btnGoOnline, chipStatus)

        btnGoOnline.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            isOnline = !isOnline
            MockDataRepository.setDriverOnline(isOnline)
            updateStatusUI(btnGoOnline, chipStatus)
        }

        tvSwitch.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            MockDataRepository.setDriverMode(false)
            findNavController().navigate(R.id.action_global_to_home)
        }
    }

    private fun updateStatusUI(btn: MaterialButton, chip: Chip) {
        if (isOnline) {
            btn.text = "STOP DRIVING"
            btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error))
            chip.text = "ONLINE"
            chip.setChipBackgroundColorResource(R.color.success)
            chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        } else {
            btn.text = "GO ONLINE"
            btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
            chip.text = "OFFLINE"
            chip.setChipBackgroundColorResource(R.color.surface_container_high)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface_variant))
        }
    }
}

