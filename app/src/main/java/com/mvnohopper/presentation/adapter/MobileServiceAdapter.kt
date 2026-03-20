package com.mvnohopper.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mvnohopper.R
import com.mvnohopper.databinding.ItemMobileServiceBinding
import com.mvnohopper.domain.model.MobileServiceWithCalculations

class MobileServiceAdapter :
    ListAdapter<MobileServiceWithCalculations, MobileServiceAdapter.MobileServiceViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MobileServiceViewHolder {
        val binding = ItemMobileServiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MobileServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MobileServiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MobileServiceViewHolder(
        private val binding: ItemMobileServiceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MobileServiceWithCalculations) {
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
            binding.progressSummaryTextView.text = binding.root.context.getString(
                R.string.home_progress_summary,
                item.elapsedMonths,
                item.remainingPromotionDays
            )
            binding.alertDateTextView.text = buildBoldDateText(
                binding.root.context.getString(
                    R.string.home_alert_date,
                    item.recommendedReminderDate.toString()
                )
            )
        }

        private fun buildBoldDateText(fullText: String): android.text.SpannableString {
            val value = fullText.substringAfter(": ").ifBlank { fullText }
            val start = fullText.indexOf(value)
            val end = start + value.length
            return android.text.SpannableString(fullText).apply {
                if (start >= 0) {
                    setSpan(
                        android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        start,
                        end,
                        android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
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
