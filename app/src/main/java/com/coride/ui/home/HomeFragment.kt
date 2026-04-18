package com.coride.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.coride.R
import com.coride.data.model.RideStatus
import com.coride.data.repository.MockDataRepository
import com.coride.ui.common.SpringPhysicsHelper
import com.coride.ui.verification.VerificationPopupDialogFragment
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader(view)
        setupSearchBar(view)
        setupQuickActions(view)
        setupRecentRides(view)
        setupSavedPlaceButtons(view)
        updateDynamicContext(view)
        setupScrollEffects(view)
        setupWeatherFeature(view)

        // ── M3 Expressive entrance animations after layout ──
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                playEntranceAnimations(view)
            }
        })
    }

    private fun setupWeatherFeature(view: View) {
        val fabWeather = view.findViewById<FloatingActionButton>(R.id.fabWeatherHome)
        val cvWeatherPopup = view.findViewById<View>(R.id.cvWeatherPopupHome)
        val layoutWeatherDays = view.findViewById<LinearLayout>(R.id.layoutWeatherDaysHome)

        lifecycleScope.launch {
            try {
                // Lahore Default Coords
                val forecast = MockDataRepository.getLiveWeather(31.5204, 74.3587)
                layoutWeatherDays.removeAllViews()
                forecast.forEachIndexed { index, weather ->
                    val row = LayoutInflater.from(requireContext()).inflate(R.layout.item_weather_mini, layoutWeatherDays, false)
                    val tvDay = row.findViewById<TextView>(R.id.tvDay)
                    val tvTemp = row.findViewById<TextView>(R.id.tvTemp)
                    val ivIcon = row.findViewById<ImageView>(R.id.ivWeatherIcon)
                    
                    tvDay.text = weather.day
                    tvTemp.text = weather.temp
                    ivIcon.setImageResource(weather.iconRes)
                    
                    if (index == 0) {
                        row.setBackgroundResource(R.drawable.bg_weather_today)
                        tvDay.setTextColor(android.graphics.Color.WHITE)
                        tvTemp.setTextColor(android.graphics.Color.WHITE)
                        ivIcon.setColorFilter(android.graphics.Color.WHITE)
                    }
                    
                    layoutWeatherDays.addView(row)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fabWeather.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            if (cvWeatherPopup.visibility == View.VISIBLE) {
                cvWeatherPopup.animate()
                    .alpha(0f)
                    .translationY(20f)
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(200)
                    .withEndAction { cvWeatherPopup.visibility = View.GONE }
                    .start()
            } else {
                cvWeatherPopup.visibility = View.VISIBLE
                cvWeatherPopup.alpha = 0f
                cvWeatherPopup.translationY = 40f
                cvWeatherPopup.scaleX = 0.8f
                cvWeatherPopup.scaleY = 0.8f
                
                cvWeatherPopup.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(350)
                    .setInterpolator(android.view.animation.OvershootInterpolator(1.2f))
                    .start()
            }
        }
    }

    private fun setupHeader(view: View) {
        val user = MockDataRepository.getCurrentUser()
        view.findViewById<TextView>(R.id.tvUserName).text = user.name
        view.findViewById<View>(R.id.btnToggleMap).setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            findNavController().navigate(R.id.action_home_to_home_map)
        }
        
        view.findViewById<View>(R.id.ivUserAvatarCard).setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            findNavController().navigate(R.id.action_home_to_profile)
        }
    }

    private fun requireVerification(onVerified: (() -> Unit)? = null): Boolean {
        if (MockDataRepository.isUserVerified()) {
            return true
        }
        val popup = VerificationPopupDialogFragment.newInstance {
            onVerified?.invoke()
        }
        popup.show(childFragmentManager, "verification_popup")
        return false
    }

    private fun playEntranceAnimations(view: View) {
        val spendingCard = view.findViewById<View>(R.id.spendingCard)
        val searchCard = view.findViewById<View>(R.id.searchCard)
        val quickActions = view.findViewById<View>(R.id.quickActionsGrid)
        val layoutRecentRides = view.findViewById<View>(R.id.layoutRecentRides)
        val savedAddresses = view.findViewById<View>(R.id.savedAddressesSection)

        // Reset states for spring entrance
        val views = listOf(spendingCard, searchCard, quickActions, layoutRecentRides, savedAddresses)
        views.forEach { 
            it?.alpha = 0f
            it?.translationY = 100f
        }

        SpringPhysicsHelper.staggerSpringEntrance(
            views.filterNotNull(),
            staggerDelayMs = 120L,
            stiffness = 600f,
            dampingRatio = 0.72f
        )
    }

    private fun setupQuickActions(view: View) {
        view.findViewById<View>(R.id.actionRideNow)?.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            it.postDelayed({
                if (requireVerification()) {
                    findNavController().navigate(R.id.action_home_to_search)
                }
            }, 100)
        }

        view.findViewById<View>(R.id.actionBookNow)?.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            it.postDelayed({
                if (requireVerification()) {
                    findNavController().navigate(R.id.action_home_to_search)
                }
            }, 100)
        }

        view.findViewById<View>(R.id.actionSchedule)?.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            Toast.makeText(requireContext(), "Scheduling is coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupScrollEffects(view: View) {
        val scrollView = view.findViewById<androidx.core.widget.NestedScrollView>(R.id.homeContentScroll)
        val headerPill = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.headerPill)
        val avatarCard = view.findViewById<View>(R.id.ivUserAvatarCard)
        val tvGreeting = view.findViewById<View>(R.id.tvGreeting)
        val tvUserName = view.findViewById<View>(R.id.tvUserName)
        val btnMap = view.findViewById<View>(R.id.btnToggleMap)
        val spendingCard = view.findViewById<View>(R.id.spendingCard)

        // Threshold for full condensation
        val scrollThreshold = 100f

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val factor = (scrollY.toFloat() / scrollThreshold).coerceIn(0f, 1f)

            // 1. Stable Header Morphing (95% Opacity White as requested)
            // Background: Transparent -> 95% White (#F2FFFFFF)
            val alpha = (factor * 242).toInt() // 95% of 255 is approx 242
            headerPill?.setCardBackgroundColor(android.graphics.Color.argb(alpha, 255, 255, 255))
            
            // Corner Radius morphing: 0dp -> 100dp
            val radius = factor * 100f * resources.displayMetrics.density
            headerPill?.radius = radius
            
            // Elevation for shadow depth
            headerPill?.cardElevation = factor * 4f * resources.displayMetrics.density
            
            // Horizontal Margins: 0dp -> 16dp
            val margin = (factor * 16f * resources.displayMetrics.density).toInt()
            val topMargin = (factor * 8f * resources.displayMetrics.density).toInt()
            val lp = headerPill?.layoutParams as? ViewGroup.MarginLayoutParams
            lp?.setMargins(margin, topMargin, margin, 0)
            headerPill?.layoutParams = lp

            // 2. Element Shrinking (Stays Sharp)
            val avatarScale = 1f - (factor * 0.25f)
            avatarCard?.scaleX = avatarScale
            avatarCard?.scaleY = avatarScale
            
            val btnScale = 1f - (factor * 0.15f)
            btnMap?.scaleX = btnScale
            btnMap?.scaleY = btnScale
            
            // 3. Subtle Translation
            headerPill?.translationY = factor * 5f * resources.displayMetrics.density

            // 4. Parallax Depth for Spending Card
            spendingCard?.translationY = scrollY * 0.35f
        }
    }

    private fun updateDynamicContext(view: View) {
        val user = MockDataRepository.getCurrentUser()
        val microWidget = view.findViewById<TextView>(R.id.tvHomeMicroWidget)
        val tvTotalSpending = view.findViewById<TextView>(R.id.tvTotalSpending)

        microWidget?.text = "Hi ${user.name.split(" ").first()}, Start your new journey here"

        val completedRides = MockDataRepository.getRideHistory().filter { it.status == RideStatus.COMPLETED }
        val totalAmount = completedRides.sumOf { it.finalFare }
        
        // Format to decimal
        tvTotalSpending?.text = String.format("$%.2f", totalAmount)
    }

    private fun setupSearchBar(view: View) {
        val searchCard = view.findViewById<View>(R.id.searchCard)
        searchCard?.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            it.postDelayed({
                if (requireVerification {
                    findNavController().navigate(R.id.action_home_to_search)
                }) {
                    findNavController().navigate(R.id.action_home_to_search)
                }
            }, 120)
        }
    }

    private fun setupRecentRides(view: View) {
        val layoutRecentRides = view.findViewById<LinearLayout>(R.id.layoutRecentRides)
        val tvNoRides = view.findViewById<TextView>(R.id.tvNoRides)
        
        view.findViewById<View>(R.id.tvViewAllHistory)?.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            it.postDelayed({
                findNavController().navigate(R.id.action_home_to_history)
            }, 100)
        }

        val recentRides = MockDataRepository.getRideHistory().filter { it.status == RideStatus.COMPLETED }.take(2)

        if (recentRides.isEmpty()) {
            tvNoRides?.visibility = View.VISIBLE
        } else {
            tvNoRides?.visibility = View.GONE
            for (ride in recentRides) {
                val item = LayoutInflater.from(requireContext()).inflate(R.layout.item_recent_ride_home, layoutRecentRides, false)
                
                val tvRoute = item.findViewById<TextView>(R.id.tvRideRoute)
                val tvDate = item.findViewById<TextView>(R.id.tvRideDate)
                val tvPrice = item.findViewById<TextView>(R.id.tvRidePrice)
                
                // Assuming format: Downtown → Airport
                val origin = ride.pickup.name.takeIf { it.isNotBlank() } ?: "Origin"
                val dest = ride.destination.name.takeIf { it.isNotBlank() } ?: "Destination"
                tvRoute.text = "$origin → $dest"
                tvDate.text = ride.date
                tvPrice.text = String.format("$%.0f", ride.finalFare)
                
                item.setOnClickListener {
                    SpringPhysicsHelper.springPressFeedback(it)
                    it.postDelayed({
                        findNavController().navigate(R.id.action_home_to_history)
                    }, 100)
                }
                
                layoutRecentRides?.addView(item)
            }
        }
    }

    private fun setupSavedPlaceButtons(view: View) {
        val savedPlaces = MockDataRepository.getSavedPlaces()
        val home = savedPlaces.firstOrNull { it.type == com.coride.data.model.PlaceType.HOME }
        val work = savedPlaces.firstOrNull { it.type == com.coride.data.model.PlaceType.WORK }

        view.findViewById<View>(R.id.btnAddAddress)?.setOnClickListener { v ->
            SpringPhysicsHelper.springPressFeedback(v)
            v.postDelayed({
                findNavController().navigate(R.id.action_home_to_home_map)
            }, 100)
        }

        view.findViewById<View>(R.id.btnHome)?.setOnClickListener { v ->
            SpringPhysicsHelper.springPressFeedback(v)
            home?.let { place ->
                v.postDelayed({
                    if (requireVerification {
                        val bundle = bundleOf(
                            "destination_name" to place.name,
                            "destination_address" to place.address,
                            "destination_lat" to place.latitude,
                            "destination_lng" to place.longitude
                        )
                        findNavController().navigate(R.id.action_home_to_booking, bundle)
                    }) {
                        val bundle = bundleOf(
                            "destination_name" to place.name,
                            "destination_address" to place.address,
                            "destination_lat" to place.latitude,
                            "destination_lng" to place.longitude
                        )
                        findNavController().navigate(R.id.action_home_to_booking, bundle)
                    }
                }, 100)
            }
        }

        view.findViewById<View>(R.id.btnWork)?.setOnClickListener { v ->
            SpringPhysicsHelper.springPressFeedback(v)
            work?.let { place ->
                v.postDelayed({
                    if (requireVerification {
                        val bundle = bundleOf(
                            "destination_name" to place.name,
                            "destination_address" to place.address,
                            "destination_lat" to place.latitude,
                            "destination_lng" to place.longitude
                        )
                        findNavController().navigate(R.id.action_home_to_booking, bundle)
                    }) {
                        val bundle = bundleOf(
                            "destination_name" to place.name,
                            "destination_address" to place.address,
                            "destination_lat" to place.latitude,
                            "destination_lng" to place.longitude
                        )
                        findNavController().navigate(R.id.action_home_to_booking, bundle)
                    }
                }, 100)
            }
        }
    }
}
