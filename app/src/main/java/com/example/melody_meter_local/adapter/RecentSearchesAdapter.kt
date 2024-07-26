package com.example.melody_meter_local.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.melody_meter_local.databinding.ItemRecentSearchBinding

class RecentSearchesAdapter(
    private val onTextClick: (String) -> Unit,
    private val onClearClick: (String) -> Unit
) :
    RecyclerView.Adapter<RecentSearchesAdapter.RecentSearchesViewHolder>() {

    private var recentSearches: List<String> = listOf()

    class RecentSearchesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemRecentSearchBinding.bind(itemView)

        fun bind(searchItem: String, onTextClick: (String) -> Unit, onClearClick: (String) -> Unit) {
            binding.searchTextView.text = searchItem
            binding.searchTextView.setOnClickListener {
                onTextClick(searchItem)
            }
            binding.clearButton.setOnClickListener {
                onClearClick(searchItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchesViewHolder {
        val binding = ItemRecentSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentSearchesViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: RecentSearchesViewHolder, position: Int) {
        val searchItem = recentSearches[position]
        holder.bind(searchItem, onTextClick, onClearClick)
    }

    override fun getItemCount() = recentSearches.size

    fun submitList(list: List<String>) {
        recentSearches = list
        notifyDataSetChanged()
    }

}