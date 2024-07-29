package com.example.music_metrics.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music_metrics.databinding.ItemAlbumBinding
import com.example.music_metrics.model.spotify.Album

class NewReleasesAdapter(
    private val onItemClick: (Album) -> Unit
) : RecyclerView.Adapter<NewReleasesAdapter.AlbumViewHolder>() {

    private var albums: List<Album> = emptyList()

    class AlbumViewHolder(
        itemView: View,
        private val onItemClick: (Album) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemAlbumBinding.bind(itemView)
        fun bind(album: Album) {
            binding.albumName.text = album.name
            binding.artistName.text = album.artists.joinToString(", ") { it.name }

            // Load album image using Glide or any other image loading library
            Glide.with(binding.root.context)
                .load(album.images?.first()?.url)
                .into(binding.albumImage)

            itemView.setOnClickListener {
                onItemClick(album)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        // Inflate your item layout here
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding.root, onItemClick)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(albums[position])
    }

    override fun getItemCount() = albums.size

    fun submitList(newList: List<Album>) {
        albums = newList
        notifyDataSetChanged()
    }

}
