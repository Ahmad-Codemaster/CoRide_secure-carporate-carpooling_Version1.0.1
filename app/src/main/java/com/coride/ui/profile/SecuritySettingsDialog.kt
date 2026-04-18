package com.coride.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.button.MaterialButton
import com.coride.R
import com.coride.data.local.LocalPreferences
import com.coride.data.repository.MockDataRepository
import com.coride.utils.BiometricHelper

class SecuritySettingsDialog : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_security_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchBiometric = view.findViewById<MaterialSwitch>(R.id.switchBiometric)
        val switchShakeSos = view.findViewById<MaterialSwitch>(R.id.switchShakeSos)
        val btnDeleteAccount = view.findViewById<MaterialButton>(R.id.btnDeleteAccount)
        val btnClose = view.findViewById<MaterialButton>(R.id.btnCloseSecurity)

        // Initialize Biometric state
        val isHardwareAvailable = BiometricHelper.isBiometricAvailable(requireContext())
        switchBiometric.isEnabled = isHardwareAvailable
        switchBiometric.isChecked = LocalPreferences.isBiometricEnabled()

        if (!isHardwareAvailable) {
            Toast.makeText(requireContext(), "Biometrics not available on this device", Toast.LENGTH_SHORT).show()
        }

        switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            LocalPreferences.setBiometricEnabled(isChecked)
            val status = if (isChecked) "Enabled" else "Disabled"
            Toast.makeText(requireContext(), "Biometric Login $status", Toast.LENGTH_SHORT).show()
        }

        // Initialize Shake SOS state
        switchShakeSos.isChecked = LocalPreferences.isShakeSosEnabled()
        switchShakeSos.setOnCheckedChangeListener { _, isChecked ->
            LocalPreferences.setShakeSosEnabled(isChecked)
            val status = if (isChecked) "Enabled" else "Disabled"
            Toast.makeText(requireContext(), "Shake to SOS $status", Toast.LENGTH_SHORT).show()
        }

        btnDeleteAccount.setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("⚠️ Delete Your Account?")
                .setMessage("This action is IRREVERSIBLE. All your institutional verification data, login credentials, and local saved preferences will be permanently wiped.\n\nAre you sure you want to proceed?")
                .setNeutralButton("Cancel", null)
                .setPositiveButton("Verify & Delete") { _, _ ->
                    // Phase 2: Biometric verification
                    BiometricHelper.showBiometricPrompt(
                        activity = requireActivity(),
                        title = "Authorize Deletion",
                        subtitle = "Confirm fingerprint to purge account",
                        onSuccess = {
                            MockDataRepository.deleteAccount()
                            Toast.makeText(requireContext(), "Account & Data Permanently Deleted", Toast.LENGTH_LONG).show()
                            
                            // Redirect to AuthActivity and clear task
                            val intent = android.content.Intent(requireContext(), com.coride.ui.auth.AuthActivity::class.java)
                            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            requireActivity().finish()
                        },
                        onError = { error ->
                            Toast.makeText(requireContext(), "Identity verification failed: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                .show()
        }

        btnClose.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val TAG = "SecuritySettingsDialog"
        fun newInstance() = SecuritySettingsDialog()
    }
}

