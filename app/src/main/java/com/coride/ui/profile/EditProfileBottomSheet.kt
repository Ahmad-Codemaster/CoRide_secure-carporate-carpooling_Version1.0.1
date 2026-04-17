package com.coride.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.coride.R
import com.coride.data.repository.MockDataRepository

class EditProfileBottomSheet : BottomSheetDialogFragment() {

    var onProfileUpdated: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val user = MockDataRepository.getCurrentUser()
        val etName = view.findViewById<TextInputEditText>(R.id.etEditName)
        val etPhone = view.findViewById<TextInputEditText>(R.id.etEditPhone)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEditEmail)

        etName?.setText(user.name)
        etPhone?.setText(user.phone)
        etEmail?.setText(user.email)

        view.findViewById<MaterialButton>(R.id.btnCancelEdit)?.setOnClickListener {
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.btnSaveEdit)?.setOnClickListener {
            val updatedUser = user.copy(
                name = etName?.text.toString().trim(),
                phone = etPhone?.text.toString().trim(),
                email = etEmail?.text.toString().trim()
            )
            MockDataRepository.updateUser(updatedUser)
            
            Toast.makeText(requireContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
            onProfileUpdated?.invoke()
            dismiss()
        }
    }
}

