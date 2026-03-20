package com.mvnohopper.presentation.ui.home

import android.content.Intent
import android.os.Bundle
import android.graphics.Rect
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.mvnohopper.R
import com.mvnohopper.data.entity.MobileService
import com.mvnohopper.databinding.ActivityHomeBinding
import com.mvnohopper.presentation.adapter.MobileServiceAdapter
import com.mvnohopper.presentation.ui.add_edit.AddEditActivity
import com.mvnohopper.presentation.viewmodel.HomeViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private val mobileServiceAdapter: MobileServiceAdapter = MobileServiceAdapter(
        onSelectionChanged = ::updateDeleteState,
        onUpdateRequested = { viewModel.updateMobileService(it) }
    )
    private val shouldAnimateEntry: Boolean by lazy {
        intent.getBooleanExtra(EXTRA_ANIMATE_ENTRY, false)
    }
    private var currentMobileServices: List<MobileService> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEntryAnimation()
        setupRecyclerView()
        setupListeners()
        observeMobileServices()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val focusedView = currentFocus
            if (focusedView is EditText) {
                val outRect = Rect()
                focusedView.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    focusedView.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
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
        binding.deleteButton.setOnClickListener {
            val selectedIds = mobileServiceAdapter.getSelectedIds()
            val selectedItems = currentMobileServices.filter { it.id in selectedIds }
            if (selectedItems.isNotEmpty()) {
                viewModel.deleteMobileServices(selectedItems)
                mobileServiceAdapter.clearSelection()
            }
        }
    }

    private fun observeMobileServices() {
        viewModel.mobileServices.observe(this) { items ->
            currentMobileServices = items.map { it.mobileService }
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
            binding.topSpacerView,
            binding.lineCountTextView,
            binding.listTitleTextView,
            binding.emptyStateTextView,
            binding.mobileServiceRecyclerView
        )
        fadeTargets.forEach { view ->
            view.alpha = 0f
            view.translationY = 18f
        }

        binding.addLineButton.postDelayed({
            fadeTargets.forEachIndexed { index, view ->
                view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(index * 35L)
                    .setDuration(260L)
                    .start()
            }
        }, 120L)
    }

    companion object {
        const val EXTRA_ANIMATE_ENTRY = "animate_entry"
    }

    private fun updateDeleteState(selectedCount: Int) {
        binding.deleteButton.visibility = if (selectedCount > 0) View.VISIBLE else View.INVISIBLE
        binding.deleteButton.isEnabled = selectedCount > 0
        binding.deleteButton.alpha = if (selectedCount > 0) 1f else 0.35f
    }
}
