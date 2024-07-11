package com.example.melody_meter_local.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.melody_meter_local.R

class RecentSearchesAdapter(private val recentSearches: MutableList<String>, private val onClearClick: (String) -> Unit) :
    RecyclerView.Adapter<RecentSearchesAdapter.RecentSearchesViewHolder>() {

    class RecentSearchesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val searchTextView: TextView = itemView.findViewById(R.id.searchTextView)
        private val clearButton: ImageButton = itemView.findViewById(R.id.clearButton)

        fun bind(searchItem: String, onClearClick: (String) -> Unit) {
            searchTextView.text = searchItem
            clearButton.setOnClickListener {
                onClearClick(searchItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchesViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_search, parent, false)
        return RecentSearchesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecentSearchesViewHolder, position: Int) {
        val searchItem = recentSearches[position]
        holder.bind(searchItem, onClearClick)
    }

    override fun getItemCount() = recentSearches.size

}