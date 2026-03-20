package com.mvnohopper.presentation.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.LinearLayoutManager
import com.mvnohopper.R
import com.mvnohopper.databinding.ActivityHomeBinding
import com.mvnohopper.presentation.adapter.MobileServiceAdapter
import com.mvnohopper.presentation.ui.add_edit.AddEditActivity
import com.mvnohopper.presentation.viewmodel.HomeViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private val mobileServiceAdapter: MobileServiceAdapter = MobileServiceAdapter()
    private val shouldAnimateEntry: Boolean by lazy {
        intent.getBooleanExtra(EXTRA_ANIMATE_ENTRY, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (shouldAnimateEntry) {
            postponeEnterTransition()
        }
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEntryAnimation()
        setupRecyclerView()
        setupListeners()
        observeMobileServices()
    }

    private fun setupRecyclerView() {
        binding.mobileServiceRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = mobileServiceAdapter
        }
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
            binding.emptyStateTextView.text = if (items.isEmpty()) {
                getString(R.string.home_empty_state)
            } else {
                getString(R.string.home_ready_state)
            }
            binding.emptyStateTextView.visibility = if (items.isEmpty()) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
            mobileServiceAdapter.submitList(items)
        }
    }

    private fun setupEntryAnimation() {
        if (!shouldAnimateEntry) {
            return
        }

        val fadeTargets = listOf(
            binding.titleTextView,
            binding.subtitleTextView,
            binding.lineCountTextView,
            binding.listTitleTextView,
            binding.emptyStateTextView,
            binding.mobileServiceRecyclerView
        )
        fadeTargets.forEach { view ->
            view.alpha = 0f
            view.translationY = 18f
        }

        binding.addLineButton.doOnPreDraw {
            startPostponedEnterTransition()
            binding.addLineButton.postDelayed({
                fadeTargets.forEachIndexed { index, view ->
                    view.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setStartDelay(index * 35L)
                        .setDuration(260L)
                        .start()
                }
            }, 140L)
        }
    }

    companion object {
        const val EXTRA_ANIMATE_ENTRY = "animate_entry"
    }
}
