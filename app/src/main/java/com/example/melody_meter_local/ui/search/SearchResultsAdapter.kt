package com.example.melody_meter_local.ui.search

import android.app.appsearch.SearchResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.melody_meter_local.data.Song
import com.example.melody_meter_local.databinding.ItemSongBinding

class SearchResultsAdapter (private val searchResults: List<Song>):
    RecyclerView.Adapter<SearchResultsAdapter.SearchResultsViewHolder>(){
    class SearchResultsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val binding = ItemSongBinding.bind(itemView)
        fun bind(item: Song) {
            binding.trackName.text = item.name
            binding.trackArtist.text = item.artist
            binding.spotifyUrl.text = "https://open.spotify.com/track/${item.spotifyId}"
            // Load image into binding.albumImg using Glide or Picasso
            Glide.with(binding.root).load(item.imgUrl).into(binding.albumImg)
            binding.rating.text = item.avgRating.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultsViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultsViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: SearchResultsViewHolder, position: Int) {
        val item = searchResults[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }
}