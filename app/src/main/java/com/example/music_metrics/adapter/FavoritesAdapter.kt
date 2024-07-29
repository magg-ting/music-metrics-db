package com.example.music_metrics.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music_metrics.R
import com.example.music_metrics.databinding.ItemFavoriteBinding
import com.example.music_metrics.model.Song

class FavoritesAdapter(
    //private val viewModel: FavoritesViewModel,
    private val onItemClick: (Song) -> Unit,
    private val onFavoriteToggle: (Song, Boolean) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {

    private var favorites: List<Song> = emptyList()

    inner class FavoritesViewHolder(
        itemView: View,
        private val onItemClick: (Song) -> Unit,
        private val onFavoriteToggle: (Song, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemFavoriteBinding.bind(itemView)
        fun bind(item: Song) {
            binding.trackName.text = item.name
            binding.trackArtist.text = item.artist
            if (!item.imgUrl.isNullOrEmpty()) {
                // Load image into binding.albumImg using Glide
                Glide.with(binding.root).load(item.imgUrl).into(binding.albumImg)
            } else {
                binding.albumImg.setImageResource(R.drawable.default_album_cover)
            }

            //TODO: update icon at real time for toggling
            val isFavorite = item in favorites
            binding.saveButton.setImageResource(
                if (isFavorite) R.drawable.ic_save_pressed else R.drawable.ic_save_unpressed
            )

            binding.saveButton.setOnClickListener {
                onFavoriteToggle(item, !isFavorite)
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val binding =
            ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoritesViewHolder(binding.root, onItemClick, onFavoriteToggle)
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        val item = favorites[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return favorites.size
    }

    fun updateFavorites(newFavorites: List<Song>) {
        favorites = newFavorites
        notifyDataSetChanged()
    }

}