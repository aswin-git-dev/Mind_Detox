package com.example.mind_detox.ui.apps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mind_detox.databinding.ItemAppSelectionBinding

class AppAdapter(private val onToggle: (String, String, Boolean) -> Unit) :
    ListAdapter<AppItem, AppAdapter.AppViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppSelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppViewHolder(private val binding: ItemAppSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppItem) {
            binding.ivAppIcon.setImageDrawable(item.icon)
            binding.tvAppName.text = item.name
            binding.tvPackageName.text = item.packageName
            
            // Remove listener before setting state to avoid infinite loop/incorrect triggers
            binding.switchBlock.setOnCheckedChangeListener(null)
            binding.switchBlock.isChecked = item.isBlocked
            
            binding.switchBlock.setOnCheckedChangeListener { _, isChecked ->
                onToggle(item.packageName, item.name, isChecked)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AppItem>() {
        override fun areItemsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
            return oldItem == newItem
        }
    }
}
