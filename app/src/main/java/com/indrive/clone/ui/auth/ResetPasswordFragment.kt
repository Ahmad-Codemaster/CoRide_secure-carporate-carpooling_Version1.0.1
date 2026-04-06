package com.indrive.clone.ui.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionManager
import android.transition.AutoTransition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.indrive.clone.R
import com.indrive.clone.data.repository.MockDataRepository

class ResetPasswordFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reset_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contentRoot = view.findViewById<ViewGroup>(R.id.contentRoot)
        val cardOtp = view.findViewById<MaterialCardView>(R.id.cardOtp)
        val layoutResetPassword = view.findViewById<View>(R.id.layoutResetPassword)
        val doneOverlay = view.findViewById<View>(R.id.doneOverlay)
        val ivSuccessCheck = view.findViewById<View>(R.id.ivSuccessCheck)

        val etOtp = view.findViewById<TextInputEditText>(R.id.etOtp)
        val btnVerify = view.findViewById<MaterialButton>(R.id.btnVerify)
        
        val etNewPassword = view.findViewById<TextInputEditText>(R.id.etNewPassword)
        val btnReset = view.findViewById<MaterialButton>(R.id.btnReset)

        btnVerify.setOnClickListener {
            val otpCode = etOtp.text.toString().trim()
            if (MockDataRepository.verifyResetOtp(otpCode)) {
                
                // --- PHASE TRANSITION ANIMATION ---
                TransitionManager.beginDelayedTransition(contentRoot)
                
                // Fade out OTP section (Disable interaction)
                cardOtp.alpha = 0.5f
                etOtp.isEnabled = false
                btnVerify.isEnabled = false
                btnVerify.text = "✅ Identity Verified"

                // Animate showing Password Reset section
                layoutResetPassword.visibility = View.VISIBLE
                etNewPassword.requestFocus()
                
            } else {
                Toast.makeText(requireContext(), "Invalid verification code", Toast.LENGTH_SHORT).show()
                etOtp.error = "Wrong Code"
            }
        }

        btnReset.setOnClickListener {
            val newPass = etNewPassword.text.toString().trim()
            if (newPass.length < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Finalize registration logic
            MockDataRepository.finalizePasswordReset(newPass)

            // --- COMPLETION SUCCESS UX ---
            doneOverlay.visibility = View.VISIBLE
            ivSuccessCheck.visibility = View.VISIBLE

            // Wait 2.5 seconds then go to login
            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded) {
                    try {
                        findNavController().popBackStack(R.id.loginFragment, false)
                    } catch (e: Exception) {
                        // Final safety catch for navigation state
                    }
                }
            }, 2500)
        }
    }
}
