package com.coride.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.coride.ui.common.ThemeHelper
import com.google.android.material.materialswitch.MaterialSwitch
import com.coride.R

class SettingsBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views (Add mapping for dark mode if layout supports it, currently layout has generic switches)
        // For now, let's keep it simple as a settings container as seen in layout
        
        view.findViewById<View>(R.id.btnSaveSettings)?.setOnClickListener {
            dismiss()
        }
    }
}
