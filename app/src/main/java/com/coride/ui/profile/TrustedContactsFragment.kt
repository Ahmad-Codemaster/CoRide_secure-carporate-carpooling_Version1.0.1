package com.coride.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.coride.R
import com.coride.data.model.TrustedContact
import com.coride.data.repository.MockDataRepository
import com.coride.ui.common.SpringPhysicsHelper

class TrustedContactsFragment : Fragment() {

    private lateinit var adapter: TrustedContactsAdapter
    private lateinit var rvContacts: RecyclerView
    private lateinit var layoutEmpty: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_trusted_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.toolbar).setOnClickListener {
            findNavController().navigateUp()
        }

        rvContacts = view.findViewById(R.id.rvTrustedContacts)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
        
        setupRecyclerView()
        refreshList()

        view.findViewById<View>(R.id.fabAddContact).setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            showAddContactBottomSheet()
        }

        // Entrance animation
        SpringPhysicsHelper.springSlideUpFadeIn(view.findViewById(R.id.ivSecurityHero), startDelay = 100L)
        SpringPhysicsHelper.springSlideUpFadeIn(rvContacts, startDelay = 300L)
    }

    private fun setupRecyclerView() {
        adapter = TrustedContactsAdapter(
            onDelete = { contact ->
                MockDataRepository.removeTrustedContact(contact.id)
                refreshList()
                Toast.makeText(requireContext(), "Contact Removed", Toast.LENGTH_SHORT).show()
            },
            onCall = { contact ->
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Cannot make call", Toast.LENGTH_SHORT).show()
                }
            }
        )
        rvContacts.layoutManager = LinearLayoutManager(requireContext())
        rvContacts.adapter = adapter
    }

    private fun refreshList() {
        val contacts = MockDataRepository.getTrustedContacts()
        adapter.submitList(contacts)
        layoutEmpty.visibility = if (contacts.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showAddContactBottomSheet() {
        val dialog = BottomSheetDialog(requireContext(), R.style.Widget_CoRide_BottomSheet)
        val view = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        dialog.setContentView(view)

        val etName = view.findViewById<EditText>(R.id.etContactName)
        val etPhone = view.findViewById<EditText>(R.id.etContactPhone)
        val btnAdd = view.findViewById<MaterialButton>(R.id.btnAddConfirm)

        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (name.isNotEmpty() && phone.isNotEmpty()) {
                val contact = TrustedContact(name = name, phone = phone)
                MockDataRepository.addTrustedContact(contact)
                refreshList()
                dialog.dismiss()
                Toast.makeText(requireContext(), "Contact added to your network!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}

class TrustedContactsAdapter(
    private val onDelete: (TrustedContact) -> Unit,
    private val onCall: (TrustedContact) -> Unit
) : RecyclerView.Adapter<TrustedContactsViewHolder>() {

    private var items = emptyList<TrustedContact>()

    fun submitList(newItems: List<TrustedContact>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrustedContactsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trusted_contact, parent, false)
        return TrustedContactsViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrustedContactsViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.phone.text = item.phone
        holder.initial.text = item.name.take(1).uppercase()
        
        holder.btnCall.setOnClickListener { onCall(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = items.size
}

class TrustedContactsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val name: TextView = view.findViewById(R.id.tvContactName)
    val phone: TextView = view.findViewById(R.id.tvContactPhone)
    val initial: TextView = view.findViewById(R.id.tvInitial)
    val btnCall: View = view.findViewById(R.id.btnCallContact)
    val btnDelete: View = view.findViewById(R.id.btnDeleteContact)
}


