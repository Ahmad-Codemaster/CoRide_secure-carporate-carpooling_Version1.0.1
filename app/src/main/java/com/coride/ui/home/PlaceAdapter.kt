package com.coride.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coride.R
import com.coride.data.model.Place
import com.coride.data.model.PlaceType

class PlaceAdapter(
    private var places: List<Place>,
    private val onPlaceClick: (Place) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    inner class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivPlaceIcon)
        val tvName: TextView = view.findViewById(R.id.tvPlaceName)
        val tvAddress: TextView = view.findViewById(R.id.tvPlaceAddress)

        fun bind(place: Place) {
            tvName.text = place.name
            tvAddress.text = place.address

            val iconRes = when (place.type) {
                PlaceType.HOME -> android.R.drawable.ic_menu_myplaces
                PlaceType.WORK -> android.R.drawable.ic_menu_agenda
                PlaceType.SAVED -> android.R.drawable.btn_star_big_on
                else -> android.R.drawable.ic_menu_recent_history
            }
            ivIcon.setImageResource(iconRes)

            itemView.setOnClickListener { onPlaceClick(place) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(places[position])
    }

    override fun getItemCount(): Int = places.size

    fun updatePlaces(newPlaces: List<Place>) {
        places = newPlaces
        notifyDataSetChanged()
    }
}

