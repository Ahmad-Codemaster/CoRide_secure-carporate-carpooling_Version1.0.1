package com.indrive.clone.ui.profile

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.indrive.clone.R
import com.indrive.clone.ui.common.ThemeHelper

class SettingsDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_settings, null)
        
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_card) ?: view.background

        val switchDarkMode = view.findViewById<MaterialSwitch>(R.id.switchDarkMode)
        
        // 1. Initial State from Preference
        val isDark = ThemeHelper.isDarkMode(requireContext())
        switchDarkMode?.isChecked = isDark
        
        // 2. Toggle Handler
        switchDarkMode?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != ThemeHelper.isDarkMode(requireContext())) {
                ThemeHelper.setDarkMode(requireContext(), isChecked)
            }
        }

        view.findViewById<View>(R.id.btnSaveSettings)?.setOnClickListener {
            dismiss()
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .create()
            
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        return dialog
    }
}
