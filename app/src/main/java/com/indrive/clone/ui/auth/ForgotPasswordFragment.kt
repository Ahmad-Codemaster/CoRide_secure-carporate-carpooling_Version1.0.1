package com.indrive.clone.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.indrive.clone.R
import com.indrive.clone.data.repository.MockDataRepository

class ForgotPasswordFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etIdentity = view.findViewById<TextInputEditText>(R.id.etIdentity)
        val btnSendCode = view.findViewById<MaterialButton>(R.id.btnSendCode)
        val btnBackToLogin = view.findViewById<TextView>(R.id.btnBackToLogin)

        btnSendCode.setOnClickListener {
            val identity = etIdentity.text.toString().trim()
            if (identity.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your email or phone", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSendCode.isEnabled = false
            btnSendCode.text = "Checking identity..."
            
            // Background check
            val isSuccess = MockDataRepository.sendResetOtp(identity)
            
            // Navigation Guard: Ensure fragment is still attached
            if (!isAdded) return@setOnClickListener

            if (isSuccess) {
                Toast.makeText(requireContext(), "OTP sent to your registered Gmail!", Toast.LENGTH_SHORT).show()
                
                // Safe Navigation
                try {
                    findNavController().navigate(R.id.action_forgot_to_reset)
                } catch (e: Exception) {
                    // Fallback if navigation state is invalid
                    btnSendCode.isEnabled = true
                    btnSendCode.text = "Send Recovery OTP"
                }
            } else {
                Toast.makeText(requireContext(), "Identity not found. Check your Student ID/Gmail.", Toast.LENGTH_SHORT).show()
                btnSendCode.isEnabled = true
                btnSendCode.text = "Send Recovery OTP"
            }
        }

        btnBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
