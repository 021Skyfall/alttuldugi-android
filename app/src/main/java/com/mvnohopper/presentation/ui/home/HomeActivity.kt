package com.mvnohopper.presentation.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mvnohopper.R
import com.mvnohopper.databinding.ActivityHomeBinding
import com.mvnohopper.presentation.ui.add_edit.AddEditActivity
import com.mvnohopper.presentation.viewmodel.HomeViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeMobileServices()
    }

    private fun setupListeners() {
        binding.addLineButton.setOnClickListener {
            startActivity(Intent(this, AddEditActivity::class.java))
        }
    }

    private fun observeMobileServices() {
        viewModel.mobileServices.observe(this) { items ->
            binding.lineCountTextView.text = getString(
                R.string.home_line_count,
                items.size
            )
            binding.emptyStateTextView.text =
                if (items.isEmpty()) {
                    getString(R.string.home_empty_state)
                } else {
                    getString(R.string.home_ready_state)
                }
        }
    }
}
