package com.coride.ui.booking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.coride.R
import com.coride.data.model.DriverOffer

class DriverOfferAdapter(
    private var offers: List<DriverOffer>,
    private val onAccept: (DriverOffer) -> Unit,
    private val onDecline: (DriverOffer) -> Unit
) : RecyclerView.Adapter<DriverOfferAdapter.OfferViewHolder>() {

    inner class OfferViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvDriverName)
        val tvRating: TextView = view.findViewById(R.id.tvDriverRating)
        val tvTrips: TextView = view.findViewById(R.id.tvDriverTrips)
        val tvPrice: TextView = view.findViewById(R.id.tvOfferPrice)
        val tvVehicle: TextView = view.findViewById(R.id.tvVehicleInfo)
        val ivCarImage: ImageView = view.findViewById(R.id.ivCarImage)
        val tvEta: TextView = view.findViewById(R.id.tvEta)
        val btnAccept: MaterialButton = view.findViewById(R.id.btnAccept)
        val btnDecline: MaterialButton = view.findViewById(R.id.btnDecline)

        fun bind(offer: DriverOffer) {
            tvName.text = offer.driver.name
            tvRating.text = offer.driver.rating.toString()
            tvTrips.text = "${offer.driver.totalTrips} trips"
            tvPrice.text = "Rs. ${offer.offeredPrice.toInt()}"
            tvVehicle.text = "${offer.driver.vehicle.color} ${offer.driver.vehicle.make} ${offer.driver.vehicle.model} • ${offer.driver.vehicle.plateNumber}"
            
            val iconRes = when (offer.driver.vehicle.type) {
                com.coride.data.model.VehicleType.BIKE -> R.drawable.bike
                com.coride.data.model.VehicleType.RICKSHAW -> R.drawable.rikshaw
                else -> R.drawable.car
            }
            ivCarImage.setImageResource(iconRes)

            tvEta.text = "${offer.estimatedArrival} min away"

            btnAccept.setOnClickListener { onAccept(offer) }
            btnDecline.setOnClickListener { onDecline(offer) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_driver_offer, parent, false)
        return OfferViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(offers[position])
    }

    override fun getItemCount(): Int = offers.size

    fun updateOffers(newOffers: List<DriverOffer>) {
        offers = newOffers
        notifyDataSetChanged()
    }

    fun removeOffer(offer: DriverOffer) {
        val newList = offers.toMutableList()
        newList.remove(offer)
        offers = newList
        notifyDataSetChanged()
    }
}

