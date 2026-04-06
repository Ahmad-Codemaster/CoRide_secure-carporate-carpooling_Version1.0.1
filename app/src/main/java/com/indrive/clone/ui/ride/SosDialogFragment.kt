package com.indrive.clone.ui.ride

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.indrive.clone.R
import com.indrive.clone.data.repository.MockDataRepository
import com.indrive.clone.ui.common.SpringPhysicsHelper

class SosDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_sos, null)

        setupView(view)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .create()

        dialog.window?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            setGravity(android.view.Gravity.TOP)
            setWindowAnimations(R.style.DialogAnimation_SlideFromTop)
            
            // Force full width and zero top margin for "Top Sheet" look
            setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
            val params = attributes
            params.horizontalMargin = 0f
            params.verticalMargin = 0f
            params.y = 0 
            attributes = params
        }
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        return dialog
    }

    private fun setupView(view: View) {
        val heroIcon = view.findViewById<View>(R.id.sosHeroIcon)
        val tvTitle = view.findViewById<TextView>(R.id.tvSosTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvSosMessage)
        val cardEmergency = view.findViewById<View>(R.id.cardEmergencyContact)
        val tvEmergencyInfo = view.findViewById<TextView>(R.id.tvEmergencyContactInfo)
        val btnSendAlert = view.findViewById<MaterialButton>(R.id.btnSendAlert)
        val btnCallPolice = view.findViewById<MaterialButton>(R.id.btnCallPolice)
        val btnGoBack = view.findViewById<MaterialButton>(R.id.btnGoBack)

        // Populate emergency contacts from the Security Center
        val containerContacts = view.findViewById<LinearLayout>(R.id.containerTrustedContacts)
        val trustedContacts = MockDataRepository.getTrustedContacts()
        
        if (trustedContacts.isEmpty()) {
            tvEmergencyInfo.visibility = View.VISIBLE
            tvEmergencyInfo.text = getString(R.string.sos_no_contacts)
            tvEmergencyInfo.setTextColor(resources.getColor(R.color.error, null))
            btnSendAlert.isEnabled = false
            btnSendAlert.alpha = 0.5f 
        } else {
            tvEmergencyInfo.visibility = View.GONE
            containerContacts.removeAllViews()
            
            trustedContacts.forEach { contact ->
                val contactView = layoutInflater.inflate(R.layout.item_sos_contact, containerContacts, false)
                contactView.findViewById<TextView>(R.id.tvSosContactName).text = contact.name
                contactView.findViewById<TextView>(R.id.tvSosContactPhone).text = contact.phone
                
                contactView.findViewById<MaterialButton>(R.id.btnSosNotifyWhatsApp).setOnClickListener {
                    SpringPhysicsHelper.springPressFeedback(it)
                    val message = MockDataRepository.getSosMessageTemplate()
                    sendDirectWhatsAppAlert(contact.phone, message)
                }
                
                containerContacts.addView(contactView)
            }
            
            btnSendAlert.text = "Share Alert to Other Apps"
            btnSendAlert.isEnabled = true
            btnSendAlert.alpha = 1.0f
        }

        // ── Spring Entrance Animations ──
        SpringPhysicsHelper.springScale(heroIcon, 1f, 900f, 0.40f, startDelay = 80L)
        SpringPhysicsHelper.springAlpha(heroIcon, 1f, startDelay = 80L)
        SpringPhysicsHelper.springSlideUpFadeIn(tvTitle, 600f, 0.70f, startDelay = 180L)
        SpringPhysicsHelper.springSlideUpFadeIn(tvMessage, 550f, 0.72f, startDelay = 260L)
        SpringPhysicsHelper.springSlideUpFadeIn(cardEmergency, 500f, 0.75f, startDelay = 350L)
        SpringPhysicsHelper.springSlideUpFadeIn(btnSendAlert, 480f, 0.78f, startDelay = 440L)
        SpringPhysicsHelper.springSlideUpFadeIn(btnCallPolice, 460f, 0.78f, startDelay = 520L)
        SpringPhysicsHelper.springSlideUpFadeIn(btnGoBack, 440f, 0.80f, startDelay = 600L)

        // ── Button Actions ──
        btnSendAlert.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            MockDataRepository.triggerSOS()
            
            it.postDelayed({
                val message = MockDataRepository.getSosMessageTemplate()
                sendWhatsAppAlert(message)
                
                // Show success state locally
                btnSendAlert.text = getString(R.string.sos_sent_title)
                btnSendAlert.isEnabled = false
            }, 150)
        }

        btnCallPolice.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:15"))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Cannot make call", Toast.LENGTH_SHORT).show()
            }
        }

        btnGoBack.setOnClickListener {
            dismiss()
        }
    }

    private fun sendDirectWhatsAppAlert(phone: String, message: String) {
        try {
            val cleanPhone = phone.replace(Regex("[^0-9]"), "")
            // For Pakistan, if it starts with 0, replace with 92. If it starts with +, clean it.
            val finalPhone = if (cleanPhone.startsWith("0")) "92" + cleanPhone.substring(1) else cleanPhone
            
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/$finalPhone?text=" + Uri.encode(message))
            startActivity(intent)
        } catch (e: Exception) {
            sendWhatsAppAlert(message) // Fallback to general share
        }
    }

    private fun sendWhatsAppAlert(message: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/?text=" + Uri.encode(message))
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to generic share
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(Intent.createChooser(shareIntent, "Share Emergency Alert"))
        }
    }
}
