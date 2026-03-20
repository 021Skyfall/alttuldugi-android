package com.mvnohopper.presentation.adapter

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mvnohopper.R
import com.mvnohopper.data.entity.MobileService
import com.mvnohopper.databinding.ItemMobileServiceBinding
import com.mvnohopper.domain.model.MobileServiceWithCalculations
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MobileServiceAdapter(
    private val onSelectionChanged: (Int) -> Unit = {},
    private val onUpdateRequested: (MobileService) -> Unit = {}
) : ListAdapter<MobileServiceWithCalculations, MobileServiceAdapter.MobileServiceViewHolder>(DiffCallback) {

    private val selectedIds = linkedSetOf<Long>()
    private val operatorOptions = listOf(
        R.string.operator_option_kt,
        R.string.operator_option_sk,
        R.string.operator_option_lg
    )
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MobileServiceViewHolder {
        val binding = ItemMobileServiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MobileServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MobileServiceViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, selectedIds.contains(item.mobileService.id))
    }

    override fun submitList(list: List<MobileServiceWithCalculations>?) {
        super.submitList(list) {
            val validIds = list.orEmpty().map { it.mobileService.id }.toSet()
            if (selectedIds.retainAll(validIds)) {
                onSelectionChanged(selectedIds.size)
            }
        }
    }

    fun getSelectedIds(): Set<Long> = selectedIds.toSet()

    fun clearSelection() {
        if (selectedIds.isEmpty()) return
        selectedIds.clear()
        notifyDataSetChanged()
        onSelectionChanged(0)
    }

    inner class MobileServiceViewHolder(
        private val binding: ItemMobileServiceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MobileServiceWithCalculations, isSelected: Boolean) {
            val mobileService = item.mobileService

            resetInlineEditors()
            bindDisplayValues(item)
            bindSelection(mobileService.id, isSelected)
            bindInlineEditTriggers(item)
        }

        private fun bindDisplayValues(item: MobileServiceWithCalculations) {
            val mobileService = item.mobileService
            binding.operatorNameTextView.text = mobileService.operatorName
            binding.providerNameTextView.text = mobileService.providerName
            binding.planNameTextView.text = mobileService.planName
            binding.activationDateTextView.text = binding.root.context.getString(
                R.string.home_activation_date,
                mobileService.activationDate
            )
            binding.promotionDateTextView.text = item.promotionEndDate.toString()
            binding.recommendedDateTextView.text = item.recommendedReminderDate.toString()
            binding.progressSummaryTextView.text = buildRemainingDaysText(
                binding.root.context.getString(
                    R.string.home_progress_summary,
                    item.elapsedMonths,
                    item.remainingPromotionDays
                )
            )
            binding.alertDateTextView.text = buildBoldDateText(
                binding.root.context.getString(
                    R.string.home_alert_date,
                    item.recommendedReminderDate.toString()
                )
            )
        }

        private fun bindSelection(id: Long, isSelected: Boolean) {
            binding.selectCheckBox.setOnCheckedChangeListener(null)
            binding.selectCheckBox.isChecked = isSelected
            binding.selectCheckBox.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    selectedIds.add(id)
                } else {
                    selectedIds.remove(id)
                }
                onSelectionChanged(selectedIds.size)
            }
        }

        private fun bindInlineEditTriggers(item: MobileServiceWithCalculations) {
            val mobileService = item.mobileService

            binding.operatorNameTextView.setOnClickListener {
                showOperatorEditor(mobileService)
            }
            binding.providerNameTextView.setOnClickListener {
                showTextEditor(
                    textView = binding.providerNameTextView,
                    editText = binding.providerNameEditor,
                    initialValue = mobileService.providerName
                ) { newValue ->
                    onUpdateRequested(
                        mobileService.copy(
                            providerName = newValue,
                            updatedAt = now()
                        )
                    )
                }
            }
            binding.planNameTextView.setOnClickListener {
                showTextEditor(
                    textView = binding.planNameTextView,
                    editText = binding.planNameEditor,
                    initialValue = mobileService.planName
                ) { newValue ->
                    onUpdateRequested(
                        mobileService.copy(
                            planName = newValue,
                            updatedAt = now()
                        )
                    )
                }
            }
            binding.promotionDateTextView.setOnClickListener {
                showPromotionDatePicker(item)
            }
        }

        private fun showOperatorEditor(mobileService: MobileService) {
            binding.operatorNameTextView.visibility = View.GONE
            binding.operatorNameEditor.visibility = View.VISIBLE

            val adapter = ArrayAdapter(
                binding.root.context,
                R.layout.item_dropdown_operator,
                operatorOptions.map { binding.root.context.getString(it) }
            )
            binding.operatorNameEditor.setAdapter(adapter)
            binding.operatorNameEditor.setText(mobileService.operatorName, false)
            binding.operatorNameEditor.requestFocus()
            binding.operatorNameEditor.post {
                binding.operatorNameEditor.showDropDown()
            }

            binding.operatorNameEditor.setOnItemClickListener { _, _, position, _ ->
                val newValue = binding.root.context.getString(operatorOptions[position])
                hideKeyboard(binding.operatorNameEditor)
                binding.operatorNameEditor.visibility = View.GONE
                binding.operatorNameTextView.visibility = View.VISIBLE
                if (newValue != mobileService.operatorName) {
                    onUpdateRequested(
                        mobileService.copy(
                            operatorName = newValue,
                            updatedAt = now()
                        )
                    )
                }
            }

            binding.operatorNameEditor.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    binding.operatorNameEditor.visibility = View.GONE
                    binding.operatorNameTextView.visibility = View.VISIBLE
                }
            }
        }

        private fun showTextEditor(
            textView: View,
            editText: EditText,
            initialValue: String,
            onCommit: (String) -> Unit
        ) {
            textView.visibility = View.GONE
            editText.visibility = View.VISIBLE
            editText.setText(initialValue)
            editText.requestFocus()
            editText.setSelection(editText.text?.length ?: 0)
            showKeyboard(editText)

            var committed = false

            fun finishEditing(commitChanges: Boolean) {
                if (committed) return
                committed = true
                hideKeyboard(editText)

                val newValue = editText.text?.toString()?.trim().orEmpty()
                if (commitChanges) {
                    if (newValue.isBlank()) {
                        Toast.makeText(
                            binding.root.context,
                            binding.root.context.getString(R.string.add_edit_required_error),
                            Toast.LENGTH_SHORT
                        ).show()
                        editText.setText(initialValue)
                    } else if (newValue != initialValue) {
                        onCommit(newValue)
                    }
                } else {
                    editText.setText(initialValue)
                }

                editText.clearFocus()
                editText.visibility = View.GONE
                textView.visibility = View.VISIBLE
            }

            editText.setOnEditorActionListener { _, actionId, event ->
                val isDoneAction = actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_NEXT ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
                if (isDoneAction) {
                    finishEditing(commitChanges = true)
                    true
                } else {
                    false
                }
            }

            editText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    finishEditing(commitChanges = true)
                }
            }
        }

        private fun showPromotionDatePicker(item: MobileServiceWithCalculations) {
            val mobileService = item.mobileService
            val currentDate = item.promotionEndDate

            DatePickerDialog(
                binding.root.context,
                { _, year, month, dayOfMonth ->
                    val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    val activationDate = selectedDate
                        .plusDays(1)
                        .minusMonths(mobileService.promotionMonths.toLong())

                    onUpdateRequested(
                        mobileService.copy(
                            activationDate = activationDate.format(dateFormatter),
                            updatedAt = now()
                        )
                    )
                },
                currentDate.year,
                currentDate.monthValue - 1,
                currentDate.dayOfMonth
            ).show()
        }

        private fun resetInlineEditors() {
            binding.operatorNameEditor.visibility = View.GONE
            binding.providerNameEditor.visibility = View.GONE
            binding.planNameEditor.visibility = View.GONE
            binding.operatorNameTextView.visibility = View.VISIBLE
            binding.providerNameTextView.visibility = View.VISIBLE
            binding.planNameTextView.visibility = View.VISIBLE
        }

        private fun buildBoldDateText(fullText: String): SpannableString {
            val value = fullText.substringAfter(": ").ifBlank { fullText }
            val start = fullText.indexOf(value)
            val end = start + value.length
            return SpannableString(fullText).apply {
                if (start >= 0) {
                    setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(binding.root.context, R.color.text_primary)
                        ),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }

        private fun buildRemainingDaysText(fullText: String): SpannableString {
            val marker = "남은 기간 "
            val start = fullText.indexOf(marker)
            if (start < 0) {
                return SpannableString(fullText)
            }
            val valueStart = start + marker.length
            val valueEnd = fullText.indexOf("일", valueStart).let {
                if (it >= 0) it + 1 else fullText.length
            }
            return SpannableString(fullText).apply {
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    valueStart,
                    valueEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        private fun now(): String = LocalDateTime.now().format(dateTimeFormatter)

        private fun showKeyboard(editText: EditText) {
            val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }

        private fun hideKeyboard(view: View) {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<MobileServiceWithCalculations>() {
        override fun areItemsTheSame(
            oldItem: MobileServiceWithCalculations,
            newItem: MobileServiceWithCalculations
        ): Boolean {
            return oldItem.mobileService.id == newItem.mobileService.id
        }

        override fun areContentsTheSame(
            oldItem: MobileServiceWithCalculations,
            newItem: MobileServiceWithCalculations
        ): Boolean {
            return oldItem == newItem
        }
    }
}
