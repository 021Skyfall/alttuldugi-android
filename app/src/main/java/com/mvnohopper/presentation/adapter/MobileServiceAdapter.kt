package com.mvnohopper.presentation.adapter

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
            binding.promotionSummaryTextView.text = buildPromotionEndText(
                binding.root.context.getString(
                    R.string.home_promotion_end_date,
                    item.promotionEndDate.toString()
                )
            )
            binding.recommendedDateTextView.text = buildBoldDateText(
                binding.root.context.getString(
                    R.string.home_recommended_termination_date,
                    item.recommendedReminderDate.toString()
                )
            )
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

        private fun buildPromotionEndText(fullText: String): SpannableString {
            val context = binding.root.context
            val value = fullText.substringAfter(": ").ifBlank { fullText }
            val start = fullText.indexOf(value)
            val end = start + value.length
            return SpannableString(fullText).apply {
                val labelEnd = if (start > 0) start else fullText.length
                setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.accent_warning)),
                    0,
                    labelEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    labelEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                if (start >= 0) {
                    setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
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
