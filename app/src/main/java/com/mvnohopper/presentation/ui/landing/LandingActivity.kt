package com.mvnohopper.presentation.ui.landing

import android.content.Intent
import android.os.Bundle
import androidx.core.view.doOnPreDraw
import androidx.appcompat.app.AppCompatActivity
import com.mvnohopper.R
import com.mvnohopper.databinding.ActivityLandingBinding
import com.mvnohopper.presentation.ui.home.HomeActivity

class LandingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingBinding
    private var isTransitionRunning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startButton.setOnClickListener {
            if (!isTransitionRunning) {
                runLaunchAnimation()
            }
        }
    }

    private fun runLaunchAnimation() {
        isTransitionRunning = true
        binding.startButton.isEnabled = false

        binding.root.doOnPreDraw {
            val targetTop = resources.getDimensionPixelSize(R.dimen.landing_button_target_top)
            val currentTop = binding.startButton.top
            val moveDistance = (targetTop - currentTop).toFloat()

            binding.contentContainer.animate()
                .alpha(0.2f)
                .translationY(-24f)
                .setDuration(260L)
                .start()

            binding.startButton.animate()
                .translationY(moveDistance)
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(420L)
                .withStartAction {
                    binding.startButton.text = getString(R.string.home_add_line)
                }
                .withEndAction {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
                .start()
        }
    }
}
