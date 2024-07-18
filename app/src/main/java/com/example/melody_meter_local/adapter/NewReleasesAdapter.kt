package com.example.melody_meter_local.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.melody_meter_local.databinding.ItemAlbumBinding
import com.example.melody_meter_local.model.spotify.Album

class NewReleasesAdapter : RecyclerView.Adapter<NewReleasesAdapter.AlbumViewHolder>() {

    private var albums: List<Album> = emptyList()

    class AlbumViewHolder(private val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {
            binding.albumName.text = album.name
            binding.artistName.text = album.artists.joinToString(", ") { it.name }

            // Load album image using Glide or any other image loading library
            Glide.with(binding.root.context)
                .load(album.images?.first()?.url)
                .into(binding.albumImage)

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        // Inflate your item layout here
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
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
