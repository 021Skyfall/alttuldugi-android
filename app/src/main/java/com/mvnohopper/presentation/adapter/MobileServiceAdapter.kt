package com.mvnohopper.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mvnohopper.data.entity.MobileService
import com.mvnohopper.databinding.ItemMobileServiceBinding

class MobileServiceAdapter :
    ListAdapter<MobileService, MobileServiceAdapter.MobileServiceViewHolder>(DiffCallback) {

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

        fun bind(item: MobileService) {
            binding.operatorNameTextView.text = item.operatorName
            binding.providerNameTextView.text = item.providerName
            binding.planNameTextView.text = item.planName
            binding.activationDateTextView.text = item.activationDate
            binding.promotionSummaryTextView.text = binding.root.context.getString(
                com.mvnohopper.R.string.home_promotion_summary,
                item.promotionMonths,
                item.minContractMonths,
                item.reminderDaysBeforeEnd
            )
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<MobileService>() {
        override fun areItemsTheSame(oldItem: MobileService, newItem: MobileService): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MobileService, newItem: MobileService): Boolean {
            return oldItem == newItem
        }
    }
}
