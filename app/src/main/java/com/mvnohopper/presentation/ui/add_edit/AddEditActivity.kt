package com.mvnohopper.presentation.ui.add_edit

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import com.google.android.material.textfield.TextInputLayout
import androidx.appcompat.app.AppCompatActivity
import com.mvnohopper.R
import com.mvnohopper.databinding.ActivityAddEditBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAppBar()
        setupOperatorDropdown()
        setupDefaultValues()
        setupListeners()
    }

    private fun setupAppBar() {
        binding.toolbar.title = getString(R.string.add_edit_title)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupDefaultValues() {
        binding.operatorNameEditText.setText(
            getString(R.string.operator_option_kt),
            false
        )
        binding.activationDateEditText.setText(LocalDate.now().format(dateFormatter))
        binding.promotionMonthsEditText.setText("0")
        binding.minContractMonthsEditText.setText("0")
        binding.earlyTerminationFeeEditText.setText("0")
        binding.monthlyFeeEditText.setText("0")
        binding.reminderDaysEditText.setText("15")
    }

    private fun setupListeners() {
        binding.activationDateLayout.setEndIconOnClickListener {
            showDatePicker()
        }
        binding.activationDateEditText.setOnClickListener {
            showDatePicker()
        }
        binding.activationDateEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showDatePicker()
            }
        }
        binding.cancelButton.setOnClickListener {
            finish()
        }
        binding.saveButton.setOnClickListener {
            handleSave()
        }
    }

    private fun setupOperatorDropdown() {
        val operatorOptions = listOf(
            getString(R.string.operator_option_kt),
            getString(R.string.operator_option_sk),
            getString(R.string.operator_option_lg)
        )
        val adapter = ArrayAdapter(
            this,
            R.layout.item_dropdown_operator,
            operatorOptions
        )
        binding.operatorNameEditText.setAdapter(adapter)
    }

    private fun handleSave() {
        clearValidationErrors()

        val invalidLayout = validateRequiredFields()
        if (invalidLayout != null) {
            invalidLayout.requestFocus()
            binding.formGuideTextView.text = getString(R.string.add_edit_validation_guide)
            return
        }

        binding.formGuideTextView.text = getString(R.string.add_edit_save_placeholder)
    }

    private fun clearValidationErrors() {
        binding.operatorNameLayout.error = null
        binding.providerNameLayout.error = null
        binding.planNameLayout.error = null
        binding.activationDateLayout.error = null
        binding.promotionMonthsLayout.error = null
        binding.minContractMonthsLayout.error = null
        binding.earlyTerminationFeeLayout.error = null
        binding.monthlyFeeLayout.error = null
        binding.reminderDaysLayout.error = null
    }

    private fun validateRequiredFields(): TextInputLayout? {
        val validationTargets = listOf(
            binding.operatorNameLayout to binding.operatorNameEditText.text?.toString(),
            binding.providerNameLayout to binding.providerNameEditText.text?.toString(),
            binding.planNameLayout to binding.planNameEditText.text?.toString(),
            binding.activationDateLayout to binding.activationDateEditText.text?.toString(),
            binding.promotionMonthsLayout to binding.promotionMonthsEditText.text?.toString(),
            binding.minContractMonthsLayout to binding.minContractMonthsEditText.text?.toString(),
            binding.earlyTerminationFeeLayout to binding.earlyTerminationFeeEditText.text?.toString(),
            binding.monthlyFeeLayout to binding.monthlyFeeEditText.text?.toString(),
            binding.reminderDaysLayout to binding.reminderDaysEditText.text?.toString()
        )

        var firstInvalidLayout: TextInputLayout? = null

        for ((layout, value) in validationTargets) {
            if (value.isNullOrBlank()) {
                layout.error = getString(R.string.add_edit_required_error)
                if (firstInvalidLayout == null) {
                    firstInvalidLayout = layout
                }
            }
        }

        return firstInvalidLayout
    }

    private fun showDatePicker() {
        val currentDate = runCatching {
            LocalDate.parse(binding.activationDateEditText.text.toString(), dateFormatter)
        }.getOrElse {
            LocalDate.now()
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                binding.activationDateEditText.setText(selectedDate.format(dateFormatter))
            },
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        ).show()
    }
}
