package com.example.music_metrics.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.music_metrics.databinding.ItemRecentSearchBinding

class RecentSearchesAdapter(
    private val onTextClick: (String) -> Unit,
    private val onClearClick: (String) -> Unit
) :
    RecyclerView.Adapter<RecentSearchesAdapter.RecentSearchesViewHolder>() {

    private var recentSearches: List<String> = listOf()

    inner class RecentSearchesViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemRecentSearchBinding.bind(itemView)

        fun bind(searchItem: String) {
            if (searchItem != null) {
                binding.searchTextView.text = searchItem
                binding.searchTextView.setOnClickListener {
                    onTextClick(searchItem)
                }
                binding.clearButton.setOnClickListener {
                    onClearClick(searchItem)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchesViewHolder {
        val binding =
            ItemRecentSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentSearchesViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: RecentSearchesViewHolder, position: Int) {
        val searchItem = recentSearches[position]
        holder.bind(searchItem)
    }

    override fun getItemCount() = recentSearches.size

    fun submitList(list: List<String>) {
        recentSearches = list.filterNotNull()
        notifyDataSetChanged()
    }

}