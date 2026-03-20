package com.mvnohopper.presentation.ui.home

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mvnohopper.databinding.ActivityHomeBinding
import com.mvnohopper.presentation.viewmodel.HomeViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeMobileServices()
    }

    private fun observeMobileServices() {
        viewModel.mobileServices.observe(this) { items ->
            binding.lineCountTextView.text = getString(
                com.mvnohopper.R.string.home_line_count,
                items.size
            )
            binding.emptyStateTextView.text =
                if (items.isEmpty()) {
                    getString(com.mvnohopper.R.string.home_empty_state)
                } else {
                    getString(com.mvnohopper.R.string.home_ready_state)
                }
        }
    }
}
