package com.coride.ui.ride

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.coride.R
import com.coride.data.repository.MockDataRepository
import com.coride.ui.common.SpringPhysicsHelper
import com.coride.utils.EmailNotificationHelper
import com.coride.utils.SmsSafetyHelper
import com.google.android.material.button.MaterialButton

/**
 * "Are You OK?" Safety Check Bottom Sheet.
 * Appears when the ride stops moving for too long.
 *
 * - "I'm OK" → Dismisses
 * - "Help" → SOS triggered instantly
 * - Auto-alert after 15 seconds if no response
 */
class SafetyCheckDialogFragment : BottomSheetDialogFragment() {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_safety_check, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        rideId = arguments?.getString("ride_id") ?: "demo_ride"
        lastKnownLat = arguments?.getDouble("lat") ?: 0.0
        lastKnownLng = arguments?.getDouble("lng") ?: 0.0

        setupView(view)
    }

    private fun setupView(view: View) {
        val tvTimer = view.findViewById<TextView>(R.id.tvSafetyTimer)
        val btnOk = view.findViewById<MaterialButton>(R.id.btnSafetyOk)
        val btnHelp = view.findViewById<MaterialButton>(R.id.btnSafetyHelp)
        val icon = view.findViewById<View>(R.id.ivSafetyIcon)

        // ── Professional Flow ──
        SpringPhysicsHelper.springScale(icon, 1f, 800f, 0.45f, startDelay = 100L)

        // Start 15-second countdown as requested
        countDownTimer = object : CountDownTimer(15_000, 1_000) {
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
        EmailNotificationHelper.sendSosAlert(user, rideId, lastKnownLat, lastKnownLng)

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

