package com.coride.ui.ride

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.coride.R
import com.coride.data.repository.MockDataRepository
import com.coride.ui.common.SpringPhysicsHelper
import com.coride.utils.SmsSafetyHelper

class SosDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_sos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(view)
    }

    private fun setupView(view: View) {
        val heroIcon = view.findViewById<View>(R.id.sosHeroIcon)
        val tvTitle = view.findViewById<TextView>(R.id.tvSosTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvSosMessage)
        val cardEmergency = view.findViewById<View>(R.id.cardEmergencyContact)
        val containerContacts = view.findViewById<LinearLayout>(R.id.containerTrustedContacts)
        val tvNoContacts = view.findViewById<TextView>(R.id.tvEmergencyContactInfo)
        
        val btnNotifyAll = view.findViewById<MaterialButton>(R.id.btnSendAlert)
        val btnCall15 = view.findViewById<MaterialButton>(R.id.btnCallPolice)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnGoBack)

        // Get actual contacts from repository (limiting to 3 as requested)
        val user = MockDataRepository.getCurrentUser()
        val trustedContacts = MockDataRepository.getTrustedContacts().toMutableList()
        
        // Also include the primary emergency contact if set
        if (user.emergencyContactPhone.isNotEmpty()) {
            trustedContacts.add(0, com.coride.data.model.TrustedContact(
                name = user.emergencyContactName.ifEmpty { "Emergency Contact" },
                phone = user.emergencyContactPhone,
                relation = "Primary"
            ))
        }
        
        val displayContacts = trustedContacts.take(3)
        
        if (displayContacts.isEmpty()) {
            tvNoContacts.visibility = View.VISIBLE
            btnNotifyAll.isEnabled = false
            btnNotifyAll.alpha = 0.5f
        } else {
            tvNoContacts.visibility = View.GONE
            containerContacts.removeAllViews()
            
            displayContacts.forEach { contact ->
                val row = layoutInflater.inflate(R.layout.item_sos_contact, containerContacts, false)
                row.findViewById<TextView>(R.id.tvSosContactName).text = contact.name
                row.findViewById<TextView>(R.id.tvSosContactPhone).text = contact.phone
                containerContacts.addView(row)
            }
        }

        // ── Professional Animations ──
        SpringPhysicsHelper.springScale(heroIcon, 1f, 1000f, 0.40f, startDelay = 50L)
        SpringPhysicsHelper.springAlpha(heroIcon, 1f, startDelay = 50L)
        
        SpringPhysicsHelper.springSlideUpFadeIn(tvTitle, 700f, 0.70f, startDelay = 150L)
        SpringPhysicsHelper.springSlideUpFadeIn(tvMessage, 650f, 0.72f, startDelay = 220L)
        SpringPhysicsHelper.springSlideUpFadeIn(cardEmergency, 600f, 0.75f, startDelay = 300L)
        
        SpringPhysicsHelper.springSlideUpFadeIn(btnNotifyAll, 550f, 0.78f, startDelay = 400L)
        SpringPhysicsHelper.springAlpha(btnCall15, 1f, startDelay = 480L)
        SpringPhysicsHelper.springAlpha(btnCancel, 1f, startDelay = 550L)

        // ── Actions ──
        btnNotifyAll.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            
            // 🚀 REAL SOS TRIGGER: Send SMS to all using SmsSafetyHelper
            val rideId = arguments?.getString("ride_id") ?: "active_ride"
            val lat = arguments?.getDouble("lat") ?: 0.0
            val lng = arguments?.getDouble("lng") ?: 0.0
            
            val sosMessage = SmsSafetyHelper.buildSosMessage(rideId, lat, lng)
            val sentCount = SmsSafetyHelper.sendToAllEmergencyContacts(requireContext(), sosMessage)
            
            if (sentCount > 0) {
                Toast.makeText(requireContext(), "🆘 SOS Alerts dispatched to $sentCount contacts!", Toast.LENGTH_LONG).show()
                btnNotifyAll.text = "NOTIFIED ✅"
                btnNotifyAll.isEnabled = false
                
                // Also trigger internal SOS state
                MockDataRepository.triggerSOS()
            } else {
                if (!com.coride.utils.SmsSafetyHelper.hasSmsPermission(requireContext())) {
                    Toast.makeText(requireContext(), "⚠️ SMS Permission Denied. Cannot send SOS.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "No contacts chosen or delivery failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnCall15.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            try {
                // Open Dialer as requested (safer for accidental taps)
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:15"))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Could not open dialer", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            dismiss()
        }
    }

    companion object {
        fun newInstance(rideId: String, lat: Double, lng: Double): SosDialogFragment {
            return SosDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("ride_id", rideId)
                    putDouble("lat", lat)
                    putDouble("lng", lng)
                }
            }
        }
    }
}
