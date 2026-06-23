package com.mvnohopper.presentation.ui.add_edit

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.mvnohopper.R
import com.mvnohopper.data.entity.MobileService
import com.mvnohopper.databinding.ActivityAddEditBinding
import com.mvnohopper.presentation.viewmodel.AddEditViewModel
import com.mvnohopper.util.Constants
import com.mvnohopper.util.DateFormats
import com.mvnohopper.util.OperatorOptions
import com.mvnohopper.util.parseIsoDateOrToday
import com.mvnohopper.util.showDatePicker
import java.time.LocalDate

class AddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private val viewModel: AddEditViewModel by viewModels()
    private var baseFormContentPaddingBottom = 0
    private val scrollToFocusedFieldRunnable = Runnable { scrollFocusedFieldIntoView() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAppBar()
        setupOperatorDropdown()
        setupDefaultValues()
        setupNumericFieldBehavior()
        setupListeners()
        setupKeyboardScroll()
        observeViewModel()
    }

    private fun setupKeyboardScroll() {
        baseFormContentPaddingBottom = binding.formContentLayout.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.formScrollView) { _, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            binding.formContentLayout.updatePadding(bottom = baseFormContentPaddingBottom + imeBottom)
            if (imeBottom > 0) {
                scheduleScrollToFocusedField()
            }
            insets
        }

        registerAutoScrollOnFocus(binding.formContentLayout)
    }

    private fun registerAutoScrollOnFocus(parent: ViewGroup) {
        for (index in 0 until parent.childCount) {
            when (val child = parent.getChildAt(index)) {
                is EditText -> wrapFocusListenerForAutoScroll(child)
                is ViewGroup -> registerAutoScrollOnFocus(child)
            }
        }
    }

    private fun wrapFocusListenerForAutoScroll(editText: EditText) {
        val existingListener = editText.onFocusChangeListener
        editText.setOnFocusChangeListener { view, hasFocus ->
            existingListener?.onFocusChange(view, hasFocus)
            if (hasFocus) {
                scheduleScrollToFocusedField()
            }
        }
    }

    private fun scheduleScrollToFocusedField() {
        binding.formScrollView.removeCallbacks(scrollToFocusedFieldRunnable)
        binding.formScrollView.postDelayed(scrollToFocusedFieldRunnable, 200)
    }

    private fun scrollFocusedFieldIntoView() {
        val focusedView = binding.formScrollView.findFocus() ?: currentFocus ?: return
        if (!isDescendantOf(focusedView, binding.formContentLayout)) return

        val targetView = findScrollTarget(focusedView)
        val scrollViewHeight = binding.formScrollView.height
        if (scrollViewHeight <= 0) return

        val extraMargin = (24 * resources.displayMetrics.density).toInt()
        val targetScrollY = targetView.top - (scrollViewHeight - targetView.height) / 2 - extraMargin
        val maxScrollY = (binding.formContentLayout.height - scrollViewHeight).coerceAtLeast(0)
        binding.formScrollView.smoothScrollTo(0, targetScrollY.coerceIn(0, maxScrollY))
    }

    private fun findScrollTarget(view: View): View {
        var current: View = view
        var parent = current.parent
        while (parent is View && parent != binding.formContentLayout) {
            if (parent is TextInputLayout) {
                return parent
            }
            current = parent
            parent = parent.parent
        }
        return view
    }

    private fun isDescendantOf(view: View, ancestor: View): Boolean {
        var parent = view.parent
        while (parent is View) {
            if (parent == ancestor) return true
            parent = parent.parent
        }
        return false
    }

    private fun setupAppBar() {
        binding.toolbar.title = getString(R.string.add_edit_title)
        binding.toolbar.navigationContentDescription = getString(R.string.common_back)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupDefaultValues() {
        binding.operatorNameEditText.setText(getString(R.string.operator_option_kt), false)
        binding.activationDateEditText.setText(DateFormats.formatDate(LocalDate.now()))
        binding.promotionMonthsEditText.setText("0")
        binding.minContractMonthsEditText.setText("0")
        binding.earlyTerminationFeeEditText.setText("0")
        binding.monthlyFeeEditText.setText("0")
        binding.reminderDaysEditText.setText(Constants.DEFAULT_REMINDER_DAYS.toString())
    }

    private fun setupListeners() {
        binding.activationDateLayout.setEndIconOnClickListener { showDatePicker() }
        binding.activationDateEditText.setOnClickListener { showDatePicker() }
        binding.activationDateEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showDatePicker()
        }
        binding.cancelButton.setOnClickListener { finish() }
        binding.saveButton.setOnClickListener { handleSave() }
    }

    private fun setupOperatorDropdown() {
        val adapter = ArrayAdapter(
            this,
            R.layout.item_dropdown_operator,
            OperatorOptions.labels(this)
        )
        binding.operatorNameEditText.setAdapter(adapter)
    }

    private fun setupNumericFieldBehavior() {
        applyZeroClearBehavior(binding.promotionMonthsEditText)
        applyZeroClearBehavior(binding.minContractMonthsEditText)
        applyZeroClearBehavior(binding.earlyTerminationFeeEditText)
        applyZeroClearBehavior(binding.monthlyFeeEditText)
    }

    private fun applyZeroClearBehavior(editText: EditText) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            val currentValue = editText.text?.toString().orEmpty()
            if (hasFocus && currentValue == "0") {
                editText.setText("")
            } else if (!hasFocus && currentValue.isBlank()) {
                editText.setText("0")
            }
        }

        editText.setOnClickListener {
            if (editText.text?.toString() == "0") {
                editText.setText("")
            }
        }
    }

    private fun handleSave() {
        clearValidationErrors()

        val invalidLayout = validateRequiredFields()
        if (invalidLayout != null) {
            invalidLayout.requestFocus()
            binding.formGuideTextView.text = getString(R.string.add_edit_validation_guide)
            Snackbar.make(
                binding.root,
                getString(R.string.add_edit_validation_snackbar),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        binding.formGuideTextView.text = getString(R.string.add_edit_saving_placeholder)
        binding.saveButton.isEnabled = false
        viewModel.saveMobileService(buildMobileService())
    }

    private fun requiredFields(): List<Pair<TextInputLayout, () -> String?>> = listOf(
        binding.operatorNameLayout to { binding.operatorNameEditText.text?.toString() },
        binding.providerNameLayout to { binding.providerNameEditText.text?.toString() },
        binding.planNameLayout to { binding.planNameEditText.text?.toString() },
        binding.activationDateLayout to { binding.activationDateEditText.text?.toString() },
        binding.promotionMonthsLayout to { binding.promotionMonthsEditText.text?.toString() },
        binding.minContractMonthsLayout to { binding.minContractMonthsEditText.text?.toString() },
        binding.earlyTerminationFeeLayout to { binding.earlyTerminationFeeEditText.text?.toString() },
        binding.monthlyFeeLayout to { binding.monthlyFeeEditText.text?.toString() },
        binding.reminderDaysLayout to { binding.reminderDaysEditText.text?.toString() }
    )

    private fun clearValidationErrors() {
        requiredFields().forEach { (layout, _) -> layout.error = null }
    }

    private fun validateRequiredFields(): TextInputLayout? {
        var firstInvalidLayout: TextInputLayout? = null

        for ((layout, valueProvider) in requiredFields()) {
            if (valueProvider().isNullOrBlank()) {
                layout.error = getString(R.string.add_edit_required_error)
                if (firstInvalidLayout == null) {
                    firstInvalidLayout = layout
                }
            }
        }

        return firstInvalidLayout
    }

    private fun observeViewModel() {
        viewModel.saveResult.observe(this) { saveSucceeded ->
            when (saveSucceeded) {
                true -> {
                    binding.saveButton.isEnabled = true
                    Snackbar.make(
                        binding.root,
                        getString(R.string.add_edit_save_success),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    finish()
                }

                false -> {
                    binding.saveButton.isEnabled = true
                    binding.formGuideTextView.text = getString(R.string.add_edit_save_failed)
                    Snackbar.make(
                        binding.root,
                        getString(R.string.add_edit_save_failed),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                null -> Unit
            }
        }
    }

    private fun buildMobileService(): MobileService {
        val now = DateFormats.nowIsoDateTime()

        return MobileService(
            operatorName = binding.operatorNameEditText.text.toString().trim(),
            providerName = binding.providerNameEditText.text.toString().trim(),
            planName = binding.planNameEditText.text.toString().trim(),
            activationDate = binding.activationDateEditText.text.toString().trim(),
            promotionMonths = binding.promotionMonthsEditText.text.toString().trim().toInt(),
            minContractMonths = binding.minContractMonthsEditText.text.toString().trim().toInt(),
            earlyTerminationFee = binding.earlyTerminationFeeEditText.text.toString().trim().toInt(),
            monthlyFee = binding.monthlyFeeEditText.text.toString().trim().toInt(),
            reminderDaysBeforeEnd = binding.reminderDaysEditText.text.toString().trim().toInt(),
            notes = binding.notesEditText.text?.toString()?.trim().orEmpty(),
            createdAt = now,
            updatedAt = now
        )
    }

    private fun showDatePicker() {
        showDatePicker(
            parseIsoDateOrToday(binding.activationDateEditText.text.toString())
        ) { selectedDate ->
            binding.activationDateEditText.setText(DateFormats.formatDate(selectedDate))
        }
    }
}
