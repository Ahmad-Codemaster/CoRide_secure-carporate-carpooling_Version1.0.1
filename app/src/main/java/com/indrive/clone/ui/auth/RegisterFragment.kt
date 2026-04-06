package com.indrive.clone.ui.auth

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.indrive.clone.R
import com.indrive.clone.data.repository.MockDataRepository

class RegisterFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<EditText>(R.id.etName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val etOrg = view.findViewById<EditText>(R.id.etOrganization)
        val etStudentId = view.findViewById<EditText>(R.id.etStudentId)
        val btnRegister = view.findViewById<MaterialButton>(R.id.btnRegister)
        val tvLoginLink = view.findViewById<TextView>(R.id.tvLoginLink)
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

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val org = etOrg.text.toString().trim()
            val studentId = etStudentId.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty() || org.isEmpty() || studentId.isEmpty()) {
                Toast.makeText(requireContext(), "Community Identity details are mandatory", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save pending registration details and navigate to OTP
            MockDataRepository.registerPending(name, email, phone, pass, org, studentId)
            
            val bundle = Bundle().apply {
                putString("phone", "+92 $phone")
            }
            findNavController().navigate(R.id.action_register_to_otp, bundle)
        }

        tvLoginLink.setOnClickListener {
            // Simply pop back to Login screen
            findNavController().popBackStack()
        }
    }
}
