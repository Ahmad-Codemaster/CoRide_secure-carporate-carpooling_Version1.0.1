package com.coride.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.coride.R
import com.coride.data.local.LocalPreferences
import com.coride.data.repository.MockDataRepository
import com.coride.ui.main.MainActivity
import com.coride.utils.BiometricHelper

class LoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etPhone = view.findViewById<EditText>(R.id.etEmailOrPhone)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val tvSignupLink = view.findViewById<TextView>(R.id.tvSignupLink)
        val ivPasswordToggle = view.findViewById<ImageButton>(R.id.ivPasswordToggle)

        ivPasswordToggle.setOnClickListener {
            if (etPassword.transformationMethod == PasswordTransformationMethod.getInstance()) {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                ivPasswordToggle.setImageResource(R.drawable.ic_visibility_off)
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                ivPasswordToggle.setImageResource(R.drawable.ic_visibility)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        btnLogin.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val success = MockDataRepository.login(phone, pass)
            if (success) {
                // Navigate directly to MainActivity, skipping OTP
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Invalid credentials or user not found", Toast.LENGTH_SHORT).show()
            }
        }

        // Biometric Quick Login
        val btnBiometric = view.findViewById<MaterialButton>(R.id.btnBiometricLogin)
        val isBiometricEnabled = LocalPreferences.isBiometricEnabled()
        val hasHardware = BiometricHelper.isBiometricAvailable(requireContext())
        val savedId = LocalPreferences.getRegisteredEmail() ?: LocalPreferences.getRegisteredPhone()
        val savedPass = LocalPreferences.getRegisteredPassword()

        if (isBiometricEnabled && hasHardware && !savedId.isNullOrEmpty() && !savedPass.isNullOrEmpty()) {
            btnBiometric.visibility = View.VISIBLE
            btnBiometric.setOnClickListener {
                BiometricHelper.showBiometricPrompt(
                    requireActivity(),
                    onSuccess = {
                        if (MockDataRepository.login(savedId, savedPass)) {
                            Toast.makeText(requireContext(), "Biometric Login Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            requireActivity().finish()
                        }
                    },
                    onError = { error ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        } else {
            btnBiometric.visibility = View.GONE
        }

        view.findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgot_password)
        }

        tvSignupLink.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }
}

