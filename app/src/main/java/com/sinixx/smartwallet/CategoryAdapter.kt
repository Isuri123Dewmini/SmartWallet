package com.sinixx.smartwallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sinixx.smartwallet.databinding.ItemCategoryBinding

class CategoryAdapter : ListAdapter<Pair<String, Double>, CategoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    class ViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        val DIFF_CALLBACK = object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Pair<String, Double>>() {
            override fun areItemsTheSame(oldItem: Pair<String, Double>, newItem: Pair<String, Double>) =
                oldItem.first == newItem.first

            override fun areContentsTheSame(oldItem: Pair<String, Double>, newItem: Pair<String, Double>) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvCategoryName.text = item.first
        holder.binding.tvCategoryAmount.text = "$%.2f".format(item.second)
    }
}
