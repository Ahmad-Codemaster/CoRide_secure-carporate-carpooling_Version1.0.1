package com.coride.ui.verification

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.coride.R
import com.coride.data.model.VerificationDocType
import com.coride.data.repository.MockDataRepository
import com.coride.ui.common.SpringPhysicsHelper

class VerificationPopupDialogFragment : BottomSheetDialogFragment() {

    private var onVerified: (() -> Unit)? = null
    private var countDownTimer: CountDownTimer? = null

    companion object {
        fun newInstance(onVerified: (() -> Unit)? = null): VerificationPopupDialogFragment {
            return VerificationPopupDialogFragment().apply {
                this.onVerified = onVerified
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_verification_popup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(view)
    }

    private fun setupView(view: View) {
        val heroIcon = view.findViewById<View>(R.id.heroIcon)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvSubtitle = view.findViewById<TextView>(R.id.tvSubtitle)
        val cardOrgId = view.findViewById<MaterialCardView>(R.id.cardOrgId)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)
        val progressTimer = view.findViewById<CircularProgressIndicator>(R.id.progressTimer)
        val tvTimerCount = view.findViewById<TextView>(R.id.tvTimerCount)

        // Check if verification is already in progress
        if (MockDataRepository.isVerificationTimerRunning()) {
            showTimerState(view)
            startCountdownUI(progressTimer, tvTimerCount)
        }

        // ── M3 Expressive Spring Entrance Animations ──
        SpringPhysicsHelper.springScale(heroIcon, 1f, 800f, 0.45f, startDelay = 100L)
        SpringPhysicsHelper.springAlpha(heroIcon, 1f, startDelay = 100L)
        SpringPhysicsHelper.springSlideUpFadeIn(tvTitle, 550f, 0.72f, startDelay = 200L)
        SpringPhysicsHelper.springSlideUpFadeIn(tvSubtitle, 500f, 0.75f, startDelay = 280L)
        SpringPhysicsHelper.springTranslateX(cardOrgId, 0f, 650f, 0.65f, startDelay = 380L)
        SpringPhysicsHelper.springAlpha(cardOrgId, 1f, startDelay = 380L)
        SpringPhysicsHelper.springSlideUpFadeIn(btnCancel, 450f, 0.78f, startDelay = 560L)

        // ── Card Click Handlers ──
        cardOrgId.setOnClickListener {
            SpringPhysicsHelper.springPressFeedback(it)
            it.postDelayed({
                submitDocument(view)
            }, 150)
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun submitDocument(view: View) {
        MockDataRepository.submitDocument(VerificationDocType.ORGANIZATION_CARD)

        Toast.makeText(requireContext(), "📄 Organization Card uploaded successfully", Toast.LENGTH_SHORT).show()

        val progressTimer = view.findViewById<CircularProgressIndicator>(R.id.progressTimer)
        val tvTimerCount = view.findViewById<TextView>(R.id.tvTimerCount)

        showTimerState(view)
        startCountdownUI(progressTimer, tvTimerCount)
    }

    private fun showTimerState(view: View) {
        val timerContainer = view.findViewById<View>(R.id.timerContainer)
        val cardOrgId = view.findViewById<MaterialCardView>(R.id.cardOrgId)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvSubtitle = view.findViewById<TextView>(R.id.tvSubtitle)

        cardOrgId.animate().alpha(0f).scaleX(0.8f).scaleY(0.8f).setDuration(300).start()

        tvTitle.text = getString(R.string.verification_pending)
        tvSubtitle.text = getString(R.string.verification_in_progress)

        timerContainer.postDelayed({
            cardOrgId.visibility = View.GONE
            timerContainer.visibility = View.VISIBLE
            timerContainer.alpha = 0f
            timerContainer.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(400).start()
        }, 300)
    }

    private fun startCountdownUI(
        progressTimer: CircularProgressIndicator,
        tvTimerCount: TextView
    ) {
        val remainingMs = MockDataRepository.getVerificationRemainingMs()
        val totalMs = 30 * 1000L

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(remainingMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isAdded) return
                val seconds = (millisUntilFinished / 1000)
                tvTimerCount.text = String.format("0:%02d", seconds)
                val progress = ((totalMs - millisUntilFinished).toFloat() / totalMs * 100).toInt()
                progressTimer.progress = progress
            }

            override fun onFinish() {
                MockDataRepository.completeVerification()
                if (!isAdded) return
                tvTimerCount.text = "0:00"
                progressTimer.progress = 100
                Toast.makeText(requireContext(), "🎉 You are now verified!", Toast.LENGTH_LONG).show()
                onVerified?.invoke()
                dismiss()
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}

