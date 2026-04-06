package com.indrive.clone.ui.auth

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import android.content.Intent
import com.indrive.clone.R
import com.indrive.clone.data.repository.MockDataRepository
import com.indrive.clone.ui.main.MainActivity

class OtpVerificationFragment : Fragment() {

    private lateinit var otpFields: List<EditText>
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_otp_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val phone = arguments?.getString("phone") ?: ""
        val tvSubtitle = view.findViewById<TextView>(R.id.tvOtpSubtitle)
        tvSubtitle.text = "${getString(R.string.otp_sent_to)} $phone"

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { findNavController().navigateUp() }

        otpFields = listOf(
            view.findViewById(R.id.etOtp1),
            view.findViewById(R.id.etOtp2),
            view.findViewById(R.id.etOtp3),
            view.findViewById(R.id.etOtp4)
        )

        setupOtpAutoFocus()

        val btnVerify = view.findViewById<MaterialButton>(R.id.btnVerify)
        btnVerify.setOnClickListener {
            val otp = otpFields.joinToString("") { it.text.toString() }
            if (MockDataRepository.completeRegistration(otp)) {
                Toast.makeText(requireContext(), "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack(R.id.loginFragment, false)
            } else {
                Toast.makeText(requireContext(), "Invalid OTP. Use 1234", Toast.LENGTH_SHORT).show()
                otpFields.forEach { it.text.clear() }
                otpFields[0].requestFocus()
            }
        }

        // Start countdown
        val tvResend = view.findViewById<TextView>(R.id.tvResend)
        startCountdown(tvResend)
    }

    private fun setupOtpAutoFocus() {
        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        otpFields[i + 1].requestFocus()
                    }
                }
            })

            otpFields[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                    event.action == android.view.KeyEvent.ACTION_DOWN &&
                    otpFields[i].text.isEmpty() && i > 0
                ) {
                    otpFields[i - 1].requestFocus()
                    otpFields[i - 1].text.clear()
                    true
                } else false
            }
        }
    }

    private fun startCountdown(tvResend: TextView) {
        tvResend.isEnabled = false
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvResend.text = getString(R.string.resend_in, (millisUntilFinished / 1000).toInt())
                tvResend.setTextColor(resources.getColor(R.color.text_hint, null))
            }

            override fun onFinish() {
                tvResend.text = getString(R.string.resend_code)
                tvResend.setTextColor(resources.getColor(R.color.primary, null))
                tvResend.isEnabled = true
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}
