package com.coride.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.coride.R
import com.coride.data.model.VerificationStatus
import com.coride.data.repository.MockDataRepository
import com.coride.ui.auth.AuthActivity
import com.coride.ui.common.SpringPhysicsHelper
import com.coride.ui.verification.VerificationPopupDialogFragment

class ProfileFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = MockDataRepository.getCurrentUser()
        populateUserData(view, user)
        updateVerificationUI(view)

        // Menu items — Safe Binding
        view.findViewById<View>(R.id.btnEditProfile)?.setOnClickListener {
            showEditProfileDialog()
        }

        view.findViewById<View>(R.id.btnVerification)?.setOnClickListener {
            if (MockDataRepository.isUserVerified()) {
                Toast.makeText(requireContext(), "✅ You are already verified!", Toast.LENGTH_SHORT).show()
            } else {
                val popup = VerificationPopupDialogFragment.newInstance {
                    // On verified callback — refresh the entire profile UI
                    this.view?.let { root ->
                        populateUserData(root, MockDataRepository.getCurrentUser())
                        updateVerificationUI(root)
                    }
                    Toast.makeText(requireContext(), "🎉 You are now verified! Full access unlocked.", Toast.LENGTH_LONG).show()
                }
                popup.show(childFragmentManager, "verification_popup")
            }
        }

        view.findViewById<View>(R.id.btnSettings)?.setOnClickListener {
            SettingsDialog().show(childFragmentManager, "settings_dialog")
        }

        view.findViewById<View>(R.id.btnSecurityCenter)?.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_trusted_contacts)
        }

        view.findViewById<View>(R.id.btnDriverMode)?.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            val currentUser = MockDataRepository.getCurrentUser()
            if (currentUser.isRegisteredDriver) {
                MockDataRepository.setDriverMode(true)
                findNavController().navigate(R.id.action_global_to_driver_dashboard)
            } else {
                findNavController().navigate(R.id.driverRegistrationFragment)
            }
        }

        view.findViewById<View>(R.id.btnSecuritySettings)?.setOnClickListener {
            SecuritySettingsDialog.newInstance().show(childFragmentManager, SecuritySettingsDialog.TAG)
        }

        view.findViewById<View>(R.id.btnHelp)?.setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("📞 Help & Support")
                .setMessage(
                    "Need assistance? We're here for you!\n\n" +
                    "📧 Email: support@coride.pk\n" +
                    "📱 Helpline: 0800-CORIDE (267433)\n" +
                    "🕒 Hours: Mon–Sat, 9 AM – 9 PM\n\n" +
                    "FAQs:\n" +
                    "• How do I verify my identity?\n" +
                    "   Go to Profile → Verification to upload your student/employee ID.\n\n" +
                    "• How do I report an issue with a ride?\n" +
                    "   Open Ride History, select the ride, and tap 'Report Issue'.\n\n" +
                    "• How do I change my phone number?\n" +
                    "   Go to Profile → Edit Profile to update your contact details.\n\n" +
                    "• Is my data safe?\n" +
                    "   Yes! All data is encrypted and stored securely on our servers."
                )
                .setPositiveButton("Got it", null)
                .setNeutralButton("Call Support") { _, _ ->
                    Toast.makeText(requireContext(), "Calling 0800-CORIDE...", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        view.findViewById<View>(R.id.btnAbout)?.setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("About CoRide")
                .setMessage("CoRide v1.0\n\nA secure, identity-verified ride-sharing platform exclusively designed for students and employees.\n\nAll drivers and riders go through mandatory background checks and institutional ID verification to ensure maximum safety on campus and commutes.")
                .setPositiveButton("OK", null)
                .show()
        }

        // Sign Out
        view.findViewById<MaterialButton>(R.id.btnSignOut)?.setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("⚠️ Logout")
                .setMessage("Are you sure you want to logout?\n\nDon't worry — your profile data, ride history, and verification status will be safely saved for when you return.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Logout") { _, _ ->
                    MockDataRepository.logout()
                    val intent = Intent(requireContext(), AuthActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    requireActivity().finish()
                }
                .show()
        }

    }

    private fun updateVerificationUI(view: View) {
        val isVerified = MockDataRepository.isUserVerified()
        val banner = view.findViewById<MaterialCardView>(R.id.bannerUnverified)
        val tvStatus = view.findViewById<TextView>(R.id.tvVerificationStatus)

        if (isVerified) {
            banner?.visibility = View.GONE
            tvStatus?.text = "✅ Verified"
            tvStatus?.setTextColor(resources.getColor(R.color.primary, null))
        } else {
            banner?.visibility = View.VISIBLE
            tvStatus?.text = "Pending"
            tvStatus?.setTextColor(resources.getColor(R.color.error, null))
        }
    }

    private fun populateUserData(view: View, user: com.coride.data.model.User) {
        val totalRides = MockDataRepository.getRideHistory().size.toString()
        val isVerified = MockDataRepository.isUserVerified()
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()

        view.findViewById<TextView>(R.id.tvUserName)?.text = user.name
        view.findViewById<TextView>(R.id.tvUserPhone)?.text = user.phone
        view.findViewById<TextView>(R.id.tvTotalRides)?.text = totalRides
        view.findViewById<TextView>(R.id.tvRating)?.let {
            it.text = if (isVerified) "Verified" else "Pending"
            it.textSize = 14f
        }
        view.findViewById<TextView>(R.id.tvMemberSince)?.text = currentYear

        // Role
        val tvRole = view.findViewById<TextView>(R.id.tvRole)
        tvRole?.text = user.role.name.lowercase().replaceFirstChar { it.uppercase() }
        tvRole?.visibility = View.VISIBLE
    }

    private fun showEditProfileDialog() {
        val dialog = EditProfileDialog()
        dialog.onProfileUpdated = {
            this.view?.let { root -> populateUserData(root, MockDataRepository.getCurrentUser()) }
        }
        dialog.show(childFragmentManager, "edit_profile_dialog")
    }
}

