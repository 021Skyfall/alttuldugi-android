package com.mvnohopper.presentation.ui.add_edit

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
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
        binding.promotionMonthsEditText.setText("4")
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
            binding.formGuideTextView.text = getString(R.string.add_edit_save_placeholder)
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
            android.R.layout.simple_list_item_1,
            operatorOptions
        )
        binding.operatorNameEditText.setAdapter(adapter)
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
