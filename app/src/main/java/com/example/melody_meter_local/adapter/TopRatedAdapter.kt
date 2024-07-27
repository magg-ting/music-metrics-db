package com.example.melody_meter_local.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.melody_meter_local.R
import com.example.melody_meter_local.databinding.ItemSongCarouselBinding
import com.example.melody_meter_local.model.Song

class TopRatedAdapter(
    private val onItemClick: (Song) -> Unit
) : RecyclerView.Adapter<TopRatedAdapter.SongViewHolder>() {
    // Dynamically update the item views when data change
    private var songs: List<Song> = emptyList()

    class SongViewHolder(
        itemView: View,
        private val onItemClick: (Song) -> Unit
    ) :RecyclerView.ViewHolder(itemView) {

        private val binding = ItemSongCarouselBinding.bind(itemView)
        fun bind(song: Song) {
            binding.trackName.text = song.name
            binding.artistName.text = song.artist
            if (!song.imgUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context).load(song.imgUrl).into(binding.albumImage)
            } else {
                binding.albumImage.setImageResource(R.drawable.default_album_cover)
            }
            itemView.setOnClickListener {
                onItemClick(song)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding =
            ItemSongCarouselBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding.root, onItemClick)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount() = songs.size

    fun submitList(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }

}