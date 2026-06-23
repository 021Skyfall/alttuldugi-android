package com.mvnohopper.presentation.adapter

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mvnohopper.R
import com.mvnohopper.data.entity.MobileService
import com.mvnohopper.databinding.ItemMobileServiceBinding
import com.mvnohopper.domain.model.MobileServiceWithCalculations
import com.mvnohopper.util.DateFormats
import com.mvnohopper.util.OperatorOptions
import com.mvnohopper.util.hideKeyboard
import com.mvnohopper.util.parseIsoDateOrToday
import com.mvnohopper.util.showDatePicker
import com.mvnohopper.util.showKeyboard
import com.mvnohopper.util.toBoldLabeledValueSpan
import com.mvnohopper.util.toBoldMarkedSpan
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class MobileServiceAdapter(
    private val onSelectionChanged: (Int) -> Unit = {},
    private val onUpdateRequested: (MobileService) -> Unit = {}
) : ListAdapter<MobileServiceWithCalculations, MobileServiceAdapter.MobileServiceViewHolder>(DiffCallback) {

    private val selectedIds = linkedSetOf<Long>()

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

        private var operatorAdapter: ArrayAdapter<String>? = null

        fun bind(item: MobileServiceWithCalculations, isSelected: Boolean) {
            val mobileService = item.mobileService

            resetInlineEditors()
            bindDisplayValues(item)
            bindSelection(mobileService.id, isSelected)
            bindInlineEditTriggers(item)

            bindDismissOnly(
                binding.promotionLabelTextView,
                binding.recommendedLabelTextView,
                binding.progressSummaryTextView
            )
        }

        private fun bindDisplayValues(item: MobileServiceWithCalculations) {
            val mobileService = item.mobileService
            val context = binding.root.context

            binding.operatorNameTextView.text = mobileService.operatorName
            binding.providerNameTextView.text = mobileService.providerName
            binding.planNameTextView.text = mobileService.planName
            binding.activationDateTextView.text = context.getString(
                R.string.home_activation_date,
                mobileService.activationDate
            )
            binding.promotionDateTextView.text = DateFormats.formatDate(item.promotionEndDate)
            binding.recommendedDateTextView.text = DateFormats.formatDate(item.recommendedTerminationDate)
            binding.progressSummaryTextView.text = context.getString(
                R.string.home_progress_summary,
                item.elapsedMonths,
                item.remainingPromotionDays
            ).toBoldMarkedSpan(context.getString(R.string.home_remaining_days_marker))
            binding.alertDateTextView.text = context.getString(
                R.string.home_alert_date,
                DateFormats.formatDate(item.recommendedReminderDate)
            ).toBoldLabeledValueSpan(context)
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
                dismissInlineEditors()
                showOperatorEditor(mobileService)
            }
            binding.providerNameTextView.setOnClickListener {
                dismissInlineEditors()
                showTextEditor(
                    textView = binding.providerNameTextView,
                    editText = binding.providerNameEditor,
                    initialValue = mobileService.providerName
                ) { newValue ->
                    updateService(mobileService) { it.copy(providerName = newValue) }
                }
            }
            binding.planNameTextView.setOnClickListener {
                dismissInlineEditors()
                showTextEditor(
                    textView = binding.planNameTextView,
                    editText = binding.planNameEditor,
                    initialValue = mobileService.planName
                ) { newValue ->
                    updateService(mobileService) { it.copy(planName = newValue) }
                }
            }
            binding.activationDateTextView.setOnClickListener {
                dismissInlineEditors()
                showActivationDatePicker(mobileService)
            }
            binding.promotionDateTextView.setOnClickListener {
                dismissInlineEditors()
                showPromotionDatePicker(item)
            }
            binding.alertDateTextView.setOnClickListener {
                dismissInlineEditors()
                showAlertDatePicker(item)
            }
        }

        private fun bindDismissOnly(vararg views: View) {
            views.forEach { view ->
                view.setOnClickListener { dismissInlineEditors() }
            }
        }

        private fun showOperatorEditor(mobileService: MobileService) {
            val context = binding.root.context
            binding.operatorNameTextView.visibility = View.GONE
            binding.operatorNameEditor.visibility = View.VISIBLE

            val adapter = operatorAdapter ?: ArrayAdapter(
                context,
                R.layout.item_dropdown_operator,
                OperatorOptions.labels(context)
            ).also { operatorAdapter = it }

            binding.operatorNameEditor.setAdapter(adapter)
            binding.operatorNameEditor.setText(mobileService.operatorName, false)
            binding.operatorNameEditor.requestFocus()
            binding.operatorNameEditor.post { binding.operatorNameEditor.showDropDown() }

            binding.operatorNameEditor.setOnItemClickListener { _, _, position, _ ->
                val newValue = OperatorOptions.labels(context)[position]
                binding.operatorNameEditor.hideKeyboard()
                closeOperatorEditor()
                if (newValue != mobileService.operatorName) {
                    updateService(mobileService) { it.copy(operatorName = newValue) }
                }
            }

            binding.operatorNameEditor.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    closeOperatorEditor()
                }
            }
        }

        private fun closeOperatorEditor() {
            binding.operatorNameEditor.visibility = View.GONE
            binding.operatorNameTextView.visibility = View.VISIBLE
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
            editText.showKeyboard()

            var committed = false

            fun finishEditing(commitChanges: Boolean) {
                if (committed) return
                committed = true
                editText.hideKeyboard()

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
            binding.root.context.showDatePicker(item.promotionEndDate) { selectedDate ->
                val activationDate = selectedDate
                    .plusDays(1)
                    .minusMonths(mobileService.promotionMonths.toLong())
                updateService(mobileService) {
                    it.copy(activationDate = DateFormats.formatDate(activationDate))
                }
            }
        }

        private fun showActivationDatePicker(mobileService: MobileService) {
            val context = binding.root.context
            context.showDatePicker(context.parseIsoDateOrToday(mobileService.activationDate)) { selectedDate ->
                updateService(mobileService) {
                    it.copy(activationDate = DateFormats.formatDate(selectedDate))
                }
            }
        }

        private fun showAlertDatePicker(item: MobileServiceWithCalculations) {
            val mobileService = item.mobileService
            binding.root.context.showDatePicker(item.recommendedReminderDate) { selectedDate ->
                val reminderDays = ChronoUnit.DAYS.between(
                    selectedDate,
                    item.recommendedTerminationDate
                ).toInt()

                if (reminderDays < 0) {
                    Toast.makeText(
                        binding.root.context,
                        binding.root.context.getString(R.string.home_alert_date_invalid),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@showDatePicker
                }

                updateService(mobileService) {
                    it.copy(reminderDaysBeforeEnd = reminderDays)
                }
            }
        }

        private fun updateService(
            mobileService: MobileService,
            transform: (MobileService) -> MobileService
        ) {
            onUpdateRequested(
                transform(mobileService).copy(updatedAt = DateFormats.nowIsoDateTime())
            )
        }

        private fun dismissInlineEditors() {
            if (binding.providerNameEditor.visibility == View.VISIBLE) {
                binding.providerNameEditor.clearFocus()
            }
            if (binding.planNameEditor.visibility == View.VISIBLE) {
                binding.planNameEditor.clearFocus()
            }
            if (binding.operatorNameEditor.visibility == View.VISIBLE) {
                binding.operatorNameEditor.hideKeyboard()
                binding.operatorNameEditor.clearFocus()
                closeOperatorEditor()
            }
        }

        private fun resetInlineEditors() {
            closeOperatorEditor()
            binding.providerNameEditor.visibility = View.GONE
            binding.planNameEditor.visibility = View.GONE
            binding.providerNameTextView.visibility = View.VISIBLE
            binding.planNameTextView.visibility = View.VISIBLE
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<MobileServiceWithCalculations>() {
        override fun areItemsTheSame(
            oldItem: MobileServiceWithCalculations,
            newItem: MobileServiceWithCalculations
        ): Boolean = oldItem.mobileService.id == newItem.mobileService.id

        override fun areContentsTheSame(
            oldItem: MobileServiceWithCalculations,
            newItem: MobileServiceWithCalculations
        ): Boolean = oldItem == newItem
    }
}
