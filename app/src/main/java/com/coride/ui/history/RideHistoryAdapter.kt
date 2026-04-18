package com.coride.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.coride.ui.common.SpringPhysicsHelper
import com.coride.R
import com.coride.data.model.Ride
import com.coride.data.model.RideStatus

class RideHistoryAdapter(
    private var rides: List<Ride>,
    private val onDeleteClick: (Ride) -> Unit
) : RecyclerView.Adapter<RideHistoryAdapter.RideViewHolder>() {

    fun updateData(newRides: List<Ride>) {
        rides = newRides
        notifyDataSetChanged()
    }

    inner class RideViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardRide: View = view.findViewById(R.id.cardRideHistory)
        val tvDate: TextView = view.findViewById(R.id.tvRideDate)
        val tvPickup: TextView = view.findViewById(R.id.tvPickup)
        val tvDest: TextView = view.findViewById(R.id.tvDest)
        val tvFare: TextView = view.findViewById(R.id.tvRideFare)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        val btnBookAgain: MaterialButton = view.findViewById(R.id.btnBookAgain)
        val btnViewReceipt: MaterialButton = view.findViewById(R.id.btnViewReceipt)
        val btnDelete: android.widget.ImageView = view.findViewById(R.id.btnDeleteRide)

        fun bind(ride: Ride) {
            tvDate.text = ride.date
            tvPickup.text = ride.pickup.name
            tvDest.text = ride.destination.name
            tvDuration.text = "${ride.duration} min"
            
            val farePrefix = itemView.resources.getString(R.string.currency_symbol)
            tvFare.text = "$farePrefix ${ride.finalFare.toInt()}"

            cardRide.setOnClickListener {
                SpringPhysicsHelper.springPressFeedback(it)
            }

            btnBookAgain.setOnClickListener {
                SpringPhysicsHelper.springPressFeedback(it)
                it.postDelayed({
                    Toast.makeText(itemView.context, "🚖 Book Again feature is coming soon!", Toast.LENGTH_SHORT).show()
                }, 100)
            }

            btnViewReceipt.setOnClickListener {
                SpringPhysicsHelper.springPressFeedback(it)
                it.postDelayed({
                    showReceiptDialog(ride)
                }, 100)
            }

            btnDelete.setOnClickListener {
                SpringPhysicsHelper.springPressFeedback(it)
                it.postDelayed({
                    showDeleteConfirmation(ride)
                }, 100)
            }
        }

        private fun showDeleteConfirmation(ride: Ride) {
            MaterialAlertDialogBuilder(itemView.context)
                .setTitle("Delete History?")
                .setMessage("Are you sure you want to remove this ride from your activity history? This cannot be undone.")
                .setNegativeButton("Keep It", null)
                .setPositiveButton("Delete Forever") { _, _ ->
                    onDeleteClick(ride)
                    Toast.makeText(itemView.context, "Ride deleted", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        private fun showReceiptDialog(ride: Ride) {
            val dialog = BottomSheetDialog(itemView.context, R.style.Widget_CoRide_BottomSheet)
            val view = LayoutInflater.from(itemView.context).inflate(R.layout.dialog_receipt, null)
            dialog.setContentView(view)

            view.findViewById<TextView>(R.id.tvReceiptDate).text = "${ride.date} • ${ride.duration} min"
            val farePrefix = itemView.resources.getString(R.string.currency_symbol)
            
            val baseFareVal = ride.finalFare.toInt() - 50
            view.findViewById<TextView>(R.id.tvBaseFare).text = "$farePrefix $baseFareVal"
            view.findViewById<TextView>(R.id.tvTotalPaid).text = "$farePrefix ${ride.finalFare.toInt()}"
            
            view.findViewById<MaterialButton>(R.id.btnCloseReceipt).setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideHistoryAdapter.RideViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ride_history, parent, false)
        return RideViewHolder(view)
    }

    override fun onBindViewHolder(holder: RideHistoryAdapter.RideViewHolder, position: Int) {
        holder.bind(rides[position])
    }

    override fun getItemCount(): Int = rides.size
}


