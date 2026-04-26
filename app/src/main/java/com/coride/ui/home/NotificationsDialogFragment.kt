package com.coride.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.coride.R
import com.coride.data.model.NotificationType
import com.coride.data.repository.MockDataRepository
import com.coride.ui.common.SpringPhysicsHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class NotificationsDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val layoutList = view.findViewById<LinearLayout>(R.id.layoutNotificationList)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyNotifications)
        
        val notifications = MockDataRepository.getNotifications()
        
        if (notifications.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            layoutList.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            layoutList.visibility = View.VISIBLE
            
            val notificationViews = mutableListOf<View>()
            
            notifications.forEach { notification ->
                val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_notification, layoutList, false)
                notificationViews.add(itemView)
                
                val tvTitle = itemView.findViewById<TextView>(R.id.tvNotificationTitle)
                val tvMessage = itemView.findViewById<TextView>(R.id.tvNotificationMessage)
                val tvTime = itemView.findViewById<TextView>(R.id.tvNotificationTime)
                val ivIcon = itemView.findViewById<ImageView>(R.id.ivNotificationIcon)
                val cvIconBg = itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cvIconBackground)
                
                tvTitle.text = notification.title
                tvMessage.text = notification.message
                
                val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                tvTime.text = sdf.format(Date(notification.timestamp))
                
                val btnDelete = itemView.findViewById<View>(R.id.btnDeleteNotification)
                btnDelete.setOnClickListener {
                    SpringPhysicsHelper.springPressFeedback(it)
                    MockDataRepository.deleteNotification(notification.id)
                    itemView.animate().alpha(0f).translationX(100f).setDuration(300).withEndAction {
                        layoutList.removeView(itemView)
                        if (layoutList.childCount == 0) {
                            tvEmpty.visibility = View.VISIBLE
                            layoutList.visibility = View.GONE
                        }
                    }.start()
                }
                
                // Styling based on type - FIXED: Use setCardBackgroundColor to maintain rounding
                when (notification.type) {
                    NotificationType.WELCOME -> {
                        ivIcon.setImageResource(R.drawable.ic_home)
                        cvIconBg.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_container))
                    }
                    NotificationType.VERIFICATION -> {
                        ivIcon.setImageResource(R.drawable.ic_shield_check)
                        cvIconBg.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary_container))
                    }
                    NotificationType.RIDE -> {
                        ivIcon.setImageResource(R.drawable.ic_car)
                        cvIconBg.setCardBackgroundColor(android.graphics.Color.parseColor("#E3F2FD"))
                    }
                    NotificationType.SOS -> {
                        ivIcon.setImageResource(R.drawable.ic_warning)
                        cvIconBg.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF3F3"))
                        ivIcon.setColorFilter(android.graphics.Color.RED)
                    }
                    else -> {
                        ivIcon.setImageResource(R.drawable.ic_notifications)
                        cvIconBg.setCardBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"))
                    }
                }
                
                layoutList.addView(itemView)
            }
            
            // Apply staggered spring entrance
            SpringPhysicsHelper.staggerSpringEntrance(notificationViews)
            
            MockDataRepository.markNotificationsAsRead()
        }
    }
}
