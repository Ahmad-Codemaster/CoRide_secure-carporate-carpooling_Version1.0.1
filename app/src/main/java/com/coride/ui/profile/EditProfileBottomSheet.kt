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
        val etOrg = view.findViewById<TextInputEditText>(R.id.etEditOrg)
        val etStudentId = view.findViewById<TextInputEditText>(R.id.etEditStudentId)
        val etAddress = view.findViewById<TextInputEditText>(R.id.etEditAddress)


        etName?.setText(user.name)
        etPhone?.setText(user.phone)
        etEmail?.setText(user.email)
        etOrg?.setText(user.organizationName)
        etStudentId?.setText(user.cnicNumber)
        etAddress?.setText(user.homeAddress)


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
            val emerName = user.emergencyContactName
            val emerPhone = user.emergencyContactPhone

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
    }
}

