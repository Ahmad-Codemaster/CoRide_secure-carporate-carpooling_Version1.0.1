package com.indrive.clone.ui.ride

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.indrive.clone.R
import com.indrive.clone.data.repository.MockDataRepository
import com.indrive.clone.ui.common.SpringPhysicsHelper
import com.indrive.clone.utils.EmailNotificationHelper
import com.indrive.clone.utils.SmsSafetyHelper

/**
 * "Are You OK?" Safety Check dialog.
 * Appears when the ride stops moving for too long.
 *
 * - "I'm OK" → Dismisses
 * - "Help" → SOS triggered, email to admin, SMS to emergency contacts
 * - Auto-alert after 30 seconds if no response
 */
class SafetyCheckDialogFragment : DialogFragment() {

    private var countDownTimer: CountDownTimer? = null
    private var rideId: String = ""
    private var lastKnownLat: Double = 0.0
    private var lastKnownLng: Double = 0.0

    var onDismissedSafe: (() -> Unit)? = null
    var onSosTriggered: (() -> Unit)? = null

    companion object {
        fun newInstance(rideId: String, lat: Double, lng: Double): SafetyCheckDialogFragment {
            return SafetyCheckDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("ride_id", rideId)
                    putDouble("lat", lat)
                    putDouble("lng", lng)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        rideId = arguments?.getString("ride_id") ?: "demo_ride"
        lastKnownLat = arguments?.getDouble("lat") ?: 0.0
        lastKnownLng = arguments?.getDouble("lng") ?: 0.0

        val view = layoutInflater.inflate(R.layout.dialog_safety_check, null)
        setupView(view)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .create()

        dialog.window?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        return dialog
    }

    private fun setupView(view: View) {
        val tvTimer = view.findViewById<TextView>(R.id.tvSafetyTimer)
        val btnOk = view.findViewById<MaterialButton>(R.id.btnSafetyOk)
        val btnHelp = view.findViewById<MaterialButton>(R.id.btnSafetyHelp)

        // Spring entrance animations
        val icon = view.findViewById<View>(R.id.ivSafetyIcon)
        SpringPhysicsHelper.springScale(icon, 1f, 800f, 0.45f, startDelay = 100L)

        // Start 30-second countdown. If no response, auto-trigger SOS.
        countDownTimer = object : CountDownTimer(30_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                tvTimer.text = "Auto-alerting in ${seconds}s"
            }

            override fun onFinish() {
                // User didn't respond — trigger SOS automatically
                triggerSos()
            }
        }.start()

        btnOk.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            countDownTimer?.cancel()
            onDismissedSafe?.invoke()
            dismiss()
        }

        btnHelp.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            countDownTimer?.cancel()
            triggerSos()
        }
    }

    private fun triggerSos() {
        val context = context ?: return

        // 1. Trigger SOS in the data layer
        MockDataRepository.triggerSOS()

        // 2. Send email to admin
        val user = MockDataRepository.getCurrentUser()
        EmailNotificationHelper.sendSosAlert(user, lastKnownLat, lastKnownLng)

        // 3. Send SMS to all emergency contacts
        val sosMessage = SmsSafetyHelper.buildSosMessage(rideId, lastKnownLat, lastKnownLng)
        val sentCount = SmsSafetyHelper.sendToAllEmergencyContacts(context, sosMessage)

        Toast.makeText(context, "🆘 SOS triggered! Alert sent to $sentCount contacts + admin.", Toast.LENGTH_LONG).show()

        onSosTriggered?.invoke()
        dismiss()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}
