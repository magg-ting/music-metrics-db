package com.example.music_metrics.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music_metrics.R
import com.example.music_metrics.databinding.ItemSongBinding
import com.example.music_metrics.model.Song
import java.util.Locale

class SearchResultsAdapter(
    private val onItemClick: (Song) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.SearchResultsViewHolder>() {

    private var searchResults: List<Song> = listOf()

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
            if (item.ratings.isNullOrEmpty()) {
                binding.rating.setText(R.string.no_rating)
            } else {
                binding.rating.text = String.format(Locale.US, "%.1f", item.avgRating)
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

    fun submitList(list: List<Song>) {
        searchResults = list
        notifyDataSetChanged()
    }

}