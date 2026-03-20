package com.mvnohopper.presentation.ui.add_edit

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.mvnohopper.R
import com.mvnohopper.data.entity.MobileService
import com.mvnohopper.databinding.ActivityAddEditBinding
import com.mvnohopper.presentation.viewmodel.AddEditViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private val viewModel: AddEditViewModel by viewModels()
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAppBar()
        setupOperatorDropdown()
        setupDefaultValues()
        setupNumericFieldBehavior()
        setupListeners()
        observeViewModel()
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
        val now = LocalDateTime.now().format(dateTimeFormatter)

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
