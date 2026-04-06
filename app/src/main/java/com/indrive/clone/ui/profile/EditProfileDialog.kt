package com.indrive.clone.ui.profile

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.indrive.clone.R
import com.indrive.clone.data.repository.MockDataRepository

class EditProfileDialog : DialogFragment() {

    var onProfileUpdated: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        
        // Dynamically assign a standard rounded card background since it sits in the center now
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_card) ?: view.background

        val user = MockDataRepository.getCurrentUser()
        val etName = view.findViewById<TextInputEditText>(R.id.etEditName)
        val etPhone = view.findViewById<TextInputEditText>(R.id.etEditPhone)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEditEmail)
        val etOrg = view.findViewById<TextInputEditText>(R.id.etEditOrg)
        val etStudentId = view.findViewById<TextInputEditText>(R.id.etEditStudentId)
        val etAddress = view.findViewById<TextInputEditText>(R.id.etEditAddress)
        val etEmerName = view.findViewById<TextInputEditText>(R.id.etEditEmergencyName)
        val etEmerPhone = view.findViewById<TextInputEditText>(R.id.etEditEmergencyPhone)

        etName?.setText(user.name)
        etPhone?.setText(user.phone)
        etEmail?.setText(user.email)
        etOrg?.setText(user.organizationName)
        etStudentId?.setText(user.cnicNumber)
        etAddress?.setText(user.homeAddress)
        etEmerName?.setText(user.emergencyContactName)
        etEmerPhone?.setText(user.emergencyContactPhone)

        view.findViewById<MaterialButton>(R.id.btnCancelEdit)?.setOnClickListener {
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.btnSaveEdit)?.setOnClickListener {
            val name = etName?.text.toString().trim()
            val phone = etPhone?.text.toString().trim()
            val email = etEmail?.text.toString().trim()
            val org = etOrg?.text.toString().trim()
            val studentId = etStudentId?.text.toString().trim()
            val address = etAddress?.text.toString().trim()
            val emerName = etEmerName?.text.toString().trim()
            val emerPhone = etEmerPhone?.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || org.isEmpty() || studentId.isEmpty() || address.isEmpty()) {
                Toast.makeText(requireContext(), "Mandatory fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedUser = user.copy(
                name = name,
                phone = phone,
                email = email,
                organizationName = org,
                cnicNumber = studentId,
                homeAddress = address,
                emergencyContactName = emerName,
                emergencyContactPhone = emerPhone
            )
            MockDataRepository.updateUser(updatedUser)
            
            Toast.makeText(requireContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
            onProfileUpdated?.invoke()
            dismiss()
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .create()
            
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        return dialog
    }
}
