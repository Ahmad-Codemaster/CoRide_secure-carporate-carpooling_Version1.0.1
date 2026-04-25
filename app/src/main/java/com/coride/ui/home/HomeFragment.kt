package com.coride.ui.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
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
        setupSpendingIconAnimation(view)

        // ── M3 Expressive entrance animations after layout ──
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                playEntranceAnimations(view)
            }
        })
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
                    findNavController().navigate(R.id.action_home_to_booking)
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
        val homeHeader = view.findViewById<View>(R.id.homeHeader)

        // Threshold for full condensation
        val scrollThreshold = 100f

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val factor = (scrollY.toFloat() / scrollThreshold).coerceIn(0f, 1f)

            // 1. Stable Header Morphing (Glassy Blue as requested)
            // Background: Transparent -> Glassy Blue (#3B82F6 with alpha)
            val alpha = (factor * 230).toInt() // Approx 90% opacity for glassy look
            headerPill?.setCardBackgroundColor(android.graphics.Color.argb(alpha, 59, 130, 246))
            
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
            
            // 4. Triangle Background Fade Out
            homeHeader?.background?.alpha = ((1f - factor) * 255).toInt()

            // 5. Dynamic Padding & Height (Fixes bottom space in pill state)
            // Default: 24dp padding, 84dp minHeight -> Pill: 12dp padding, 72dp minHeight
            val density = resources.displayMetrics.density
            val currentPadding = ((24f - (12f * factor)) * density).toInt()
            val currentMinHeight = ((84f - (12f * factor)) * density).toInt()
            
            homeHeader?.setPadding(
                (20f * density).toInt(), // Left
                (12f * density).toInt(), // Top
                (20f * density).toInt(), // Right
                currentPadding           // Bottom (Dynamic)
            )
            homeHeader?.minimumHeight = currentMinHeight

            // 6. Parallax Depth for Spending Card
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
        tvTotalSpending?.text = String.format("PKR %.2f", totalAmount)
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
                tvPrice.text = String.format("PKR %.0f", ride.finalFare)
                
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

    private fun setupSpendingIconAnimation(view: View) {
        val icon = view.findViewById<ImageView>(R.id.ivPaidIcon) ?: return

        // 1. Horizontal Rotation (Coin Spin)
        val rotationAnim = ObjectAnimator.ofFloat(icon, "rotationY", 0f, 360f).apply {
            duration = 3500
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
        }

        // 2. Automated "Shine" Effect (Periodic Brightness/Alpha pulse)
        val shimmerAnim = ValueAnimator.ofFloat(1.0f, 1.4f, 1.0f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            startDelay = 2000 // Shine every few seconds
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                val scale = animator.animatedValue as Float
                icon.scaleX = scale * 0.8f // Slight scale pulse too
                icon.scaleY = scale * 0.8f
                icon.alpha = if (scale > 1.2f) 1.0f else 0.85f
            }
        }

        rotationAnim.start()
        shimmerAnim.start()
    }
}
