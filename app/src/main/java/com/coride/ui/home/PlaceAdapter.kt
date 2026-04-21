package com.coride.ui.home

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coride.R
import com.coride.data.model.Place
import com.coride.data.model.PlaceType
import com.google.android.material.card.MaterialCardView

class PlaceAdapter(
    private var places: List<Place>,
    private val onPlaceClick: (Place) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    inner class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cvIconBackground: MaterialCardView = view.findViewById(R.id.cvIconBackground)
        val ivIcon: ImageView = view.findViewById(R.id.ivPlaceIcon)
        val tvName: TextView = view.findViewById(R.id.tvPlaceName)
        val tvAddress: TextView = view.findViewById(R.id.tvPlaceAddress)

        fun bind(place: Place) {
            tvName.text = place.name
            tvAddress.text = place.address

            // Professional Colorful Icon Styling
            val (bgColor, iconColor, iconRes) = when (place.type) {
                PlaceType.HOME -> Triple("#EFF6FF", "#3B82F6", R.drawable.location)
                PlaceType.WORK -> Triple("#FFEDD5", "#F97316", R.drawable.ic_work)
                PlaceType.SAVED -> Triple("#FEF9C3", "#EAB308", R.drawable.ic_star)
                PlaceType.SEARCH_RESULT -> Triple("#DCFCE7", "#22C55E", R.drawable.ic_search)
                else -> Triple("#F1F5F9", "#475569", R.drawable.ic_history)
            }

            cvIconBackground.setCardBackgroundColor(Color.parseColor(bgColor))
            ivIcon.setImageResource(iconRes)
            ivIcon.setColorFilter(Color.parseColor(iconColor), PorterDuff.Mode.SRC_IN)

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
