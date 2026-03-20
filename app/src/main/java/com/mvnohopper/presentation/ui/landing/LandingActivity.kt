package com.mvnohopper.presentation.ui.landing

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
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
        binding.contentContainer.animate()
            .alpha(0.18f)
            .translationY(-12f)
            .setDuration(220L)
            .start()

        binding.startButton.animate()
            .translationYBy(-10f)
            .setDuration(180L)
            .withEndAction {
                binding.startButton.text = getString(R.string.home_add_line)
                val intent = Intent(this, HomeActivity::class.java).apply {
                    putExtra(HomeActivity.EXTRA_ANIMATE_ENTRY, true)
                }
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    binding.startButton,
                    getString(R.string.transition_home_cta)
                )
                startActivity(intent, options.toBundle())
                finish()
            }
            .start()
    }
}
