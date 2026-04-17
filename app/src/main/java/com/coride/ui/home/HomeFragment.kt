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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.coride.R
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
        setupRecentPlaces(view)
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
        val ivGlow = view.findViewById<View>(R.id.ivWeatherGlowHome)

        // Weather Outline (Static Blue)
        // Animation removed as requested.
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
                    
                    // Highlight Present Day (First item)
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
                // Animate Out (Slide Down)
                cvWeatherPopup.animate()
                    .alpha(0f)
                    .translationY(20f)
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(200)
                    .withEndAction { cvWeatherPopup.visibility = View.GONE }
                    .start()
            } else {
                // Animate In (Slide Up)
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
            findNavController().navigate(R.id.action_home_to_home_map)
        }
        
        view.findViewById<View>(R.id.ivUserAvatar).setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }
    }

    /**
     * Gate critical riding actions behind verification.
     * Returns true if user is verified and can proceed.
     */
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
        val searchCard = view.findViewById<View>(R.id.searchCard)
        val quickActions = view.findViewById<View>(R.id.quickActionsGrid)
        val safetyBanner = view.findViewById<View>(R.id.safetyBanner)
        val destinationsCard = view.findViewById<View>(R.id.destinationsCard)
        val savedAddresses = view.findViewById<View>(R.id.savedAddressesSection)
        val background = view.findViewById<View>(R.id.ivHomeBackground)

        // Background fade
        background.alpha = 0f
        background.animate().alpha(1f).setDuration(1000).start()

        // Sequencing animations for a premium "cascade" effect
        SpringPhysicsHelper.springSlideUpFadeIn(searchCard, 550f, 0.75f, startDelay = 200L)
        SpringPhysicsHelper.springSlideUpFadeIn(quickActions, 520f, 0.78f, startDelay = 350L)
        SpringPhysicsHelper.springSlideUpFadeIn(safetyBanner, 500f, 0.80f, startDelay = 500L)
        SpringPhysicsHelper.springSlideUpFadeIn(destinationsCard, 480f, 0.82f, startDelay = 650L)
        SpringPhysicsHelper.springSlideUpFadeIn(savedAddresses, 450f, 0.85f, startDelay = 800L)
    }

    private fun setupQuickActions(view: View) {
        view.findViewById<View>(R.id.actionRideNow).setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            it.postDelayed({
                if (requireVerification()) {
                    findNavController().navigate(R.id.action_home_to_search)
                }
            }, 100)
        }

        view.findViewById<View>(R.id.actionSchedule).setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            Toast.makeText(requireContext(), "Scheduling is coming soon!", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.actionSecurity).setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            findNavController().navigate(R.id.action_home_to_profile) // Example routing
        }

        view.findViewById<View>(R.id.btnLearnMoreSafety).setOnClickListener {
            Toast.makeText(requireContext(), "Safety is our #1 priority.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupScrollEffects(view: View) {
        val scrollView = view.findViewById<androidx.core.widget.NestedScrollView>(R.id.homeContentScroll)
        val header = view.findViewById<View>(R.id.homeHeader)
        val background = view.findViewById<View>(R.id.ivHomeBackground)
        
        val searchCard = view.findViewById<View>(R.id.searchCard)
        val quickActions = view.findViewById<View>(R.id.quickActionsGrid)
        val safetyBanner = view.findViewById<View>(R.id.safetyBanner)

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            // Progressive Content Fading as they hit the top bound
            // Each item has a slightly different threshold for a staggered "feel good" transition
            
            // Search Card Fades first
            searchCard.alpha = (1f - (scrollY / 80f)).coerceIn(0f, 1f)
            
            // Grid Fades a bit later
            quickActions.alpha = (1f - ((scrollY - 60f) / 100f)).coerceIn(0f, 1f)
            
            // Safety Banner Fades last in the top block
            safetyBanner.alpha = (1f - ((scrollY - 140f) / 100f)).coerceIn(0f, 1f)
        }
    }

    private fun updateDynamicContext(view: View) {
        val user = MockDataRepository.getCurrentUser()
        val microWidget = view.findViewById<TextView>(R.id.tvHomeMicroWidget)
        val safetyStats = view.findViewById<TextView>(R.id.tvSafetyStats)

        microWidget.text = "Hi ${user.name.split(" ").first()}, Start your new journey here"
        safetyStats.text = "98% Drivers Verified Today"
    }

    private fun setupSearchBar(view: View) {
        val searchCard = view.findViewById<View>(R.id.searchCard)
        searchCard.setOnClickListener {
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

    private fun setupRecentPlaces(view: View) {
        val rvRecent = view.findViewById<RecyclerView>(R.id.rvRecentPlaces)
        val recentPlaces = MockDataRepository.getRecentPlaces().take(3)

        rvRecent.layoutManager = LinearLayoutManager(requireContext())
        rvRecent.adapter = PlaceAdapter(recentPlaces) { place ->
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
        }
    }

    private fun setupSavedPlaceButtons(view: View) {
        val savedPlaces = MockDataRepository.getSavedPlaces()
        val home = savedPlaces.firstOrNull { it.type == com.coride.data.model.PlaceType.HOME }
        val work = savedPlaces.firstOrNull { it.type == com.coride.data.model.PlaceType.WORK }

        view.findViewById<View>(R.id.btnAddAddress)?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_home_map)
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



