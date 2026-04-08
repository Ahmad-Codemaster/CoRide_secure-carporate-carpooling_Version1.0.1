package com.indrive.clone.ui.driver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.indrive.clone.R
import com.indrive.clone.data.repository.MockDataRepository
import com.indrive.clone.ui.common.SpringPhysicsHelper

class DriverRegistrationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_driver_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etMake = view.findViewById<TextInputEditText>(R.id.etVehicleMake)
        val etModel = view.findViewById<TextInputEditText>(R.id.etVehicleModel)
        val etPlate = view.findViewById<TextInputEditText>(R.id.etPlateNumber)
        val etLicense = view.findViewById<TextInputEditText>(R.id.etLicenseNumber)
        val btnSubmit = view.findViewById<MaterialButton>(R.id.btnSubmitDriver)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelDriver)

        btnSubmit.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            
            val make = etMake.text.toString().trim()
            val model = etModel.text.toString().trim()
            val plate = etPlate.text.toString().trim()
            val license = etLicense.text.toString().trim()

            if (make.isEmpty() || model.isEmpty() || plate.isEmpty() || license.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields to continue", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // High-quality registration simulation
            MockDataRepository.registerDriverDetails(make, model, plate, license)
            MockDataRepository.setDriverMode(true)
            
            Toast.makeText(requireContext(), "Driver Registry Successful! Opening Dashboard...", Toast.LENGTH_LONG).show()
            
            it.postDelayed({
                findNavController().navigate(R.id.action_global_to_driver_dashboard)
            }, 800)
        }

        btnCancel.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            findNavController().navigateUp()
        }
    }
}
