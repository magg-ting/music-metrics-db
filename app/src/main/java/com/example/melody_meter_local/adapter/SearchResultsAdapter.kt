package com.example.melody_meter_local.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.melody_meter_local.R
import com.example.melody_meter_local.databinding.ItemSongBinding
import com.example.melody_meter_local.model.Song

class SearchResultsAdapter(
    private val searchResults: List<Song>,
    private val onItemClick: (Song) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.SearchResultsViewHolder>() {

    class SearchResultsViewHolder(
        itemView: View,
        private val onItemClick: (Song) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemSongBinding.bind(itemView)
        fun bind(item: Song) {
            binding.trackName.text = item.name
            binding.trackArtist.text = item.artist
            if (!item.imgUrl.isNullOrEmpty()) {
                // Load image into binding.albumImg using Glide
                Glide.with(binding.root).load(item.imgUrl).into(binding.albumImg)
            } else {
                binding.albumImg.setImageResource(R.drawable.default_album_cover)
            }
            if (item.ratings.isEmpty()) {
                binding.rating.text = "--"
            } else {
                binding.rating.text = item.avgRating.toString()
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultsViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultsViewHolder(binding.root, onItemClick)
    }

    override fun onBindViewHolder(holder: SearchResultsViewHolder, position: Int) {
        val item = searchResults[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }
}