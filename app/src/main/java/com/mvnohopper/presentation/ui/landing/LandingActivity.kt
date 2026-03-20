package com.mvnohopper.presentation.ui.landing

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
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

        val moveDistance = -resources.getDimension(R.dimen.landing_button_travel_distance)

        binding.titleTextView.animate()
            .alpha(0.15f)
            .translationY(-18f)
            .setDuration(240L)
            .start()

        binding.descriptionTextView.animate()
            .alpha(0.15f)
            .translationY(-14f)
            .setDuration(240L)
            .start()

        binding.startButton.animate()
            .translationY(moveDistance)
            .setInterpolator(FastOutSlowInInterpolator())
            .setDuration(620L)
            .withEndAction {
                binding.startButton.text = getString(R.string.home_add_line)
                val intent = Intent(this, HomeActivity::class.java).apply {
                    putExtra(HomeActivity.EXTRA_ANIMATE_ENTRY, true)
                }
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            .start()

        binding.startButton.postDelayed({
            binding.startButton.text = getString(R.string.home_add_line)
        }, 260L)
    }
}
