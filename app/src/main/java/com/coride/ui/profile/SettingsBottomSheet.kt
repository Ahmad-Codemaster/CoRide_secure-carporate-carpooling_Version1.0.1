package com.coride.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.coride.ui.common.ThemeHelper
import com.google.android.material.materialswitch.MaterialSwitch
import com.coride.R

//class SettingsBottomSheet : BottomSheetDialogFragment() {
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return inflater.inflate(R.layout.dialog_settings, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val switchDarkMode = view.findViewById<MaterialSwitch>(R.id.switchDarkMode)
//
//        // 1. Initial State from Preference (SET FIRST to avoid trigger)
//        val isDark = ThemeHelper.isDarkMode(requireContext())
//        switchDarkMode?.isChecked = isDark
//
//        // 2. Toggle Handler (SET AFTER initial state)
//        switchDarkMode?.setOnCheckedChangeListener { _, isChecked ->
//            // Prevent redundant calls if state hasn't actually changed
//            if (isChecked != ThemeHelper.isDarkMode(requireContext())) {
//                ThemeHelper.setDarkMode(requireContext(), isChecked)
//                // AppCompatDelegate.setDefaultNightMode handles activity recreate automatically
//            }
//        }
//
//        view.findViewById<View>(R.id.btnSaveSettings)?.setOnClickListener {
//            dismiss()
//        }
//    }
//}

