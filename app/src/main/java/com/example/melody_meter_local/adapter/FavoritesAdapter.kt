package com.example.melody_meter_local.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.melody_meter_local.R
import com.example.melody_meter_local.databinding.ItemFavoriteBinding
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.repository.FavoritesRepository
import com.example.melody_meter_local.viewmodel.FavoritesViewModel

class FavoritesAdapter (
    private val viewModel: FavoritesViewModel,
    private val onItemClick: (Song) -> Unit
): RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>(){
    private var favorites: List<Song> = emptyList()

    inner class FavoritesViewHolder(
        itemView: View,
        private val onItemClick: (Song) -> Unit
    ) : RecyclerView.ViewHolder(itemView){

        private val binding = ItemFavoriteBinding.bind(itemView)
        fun bind(item: Song) {
            binding.trackName.text = item.name
            binding.trackArtist.text = item.artist
            if(!item.imgUrl.isNullOrEmpty()){
                // Load image into binding.albumImg using Glide
                Glide.with(binding.root).load(item.imgUrl).into(binding.albumImg)
            }
            else{
                binding.albumImg.setImageResource(R.drawable.default_album_cover)
            }

            binding.saveButton.setOnClickListener {
                val songId = item.spotifyTrackId
                if(item in favorites){
                    viewModel.removeFavorite(songId)
                    binding.saveButton.setImageResource(R.drawable.ic_save_unpressed)
                }
                else{
                    viewModel.addFavorite(songId)
                    binding.saveButton.setImageResource(R.drawable.ic_save_pressed)
                }
                notifyDataSetChanged()
            }

            itemView.setOnClickListener{
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoritesViewHolder(binding.root, onItemClick)
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