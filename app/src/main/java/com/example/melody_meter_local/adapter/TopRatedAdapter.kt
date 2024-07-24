package com.example.melody_meter_local.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.melody_meter_local.R
import com.example.melody_meter_local.databinding.ItemSongCarouselBinding
import com.example.melody_meter_local.model.Song

class TopRatedAdapter : RecyclerView.Adapter<TopRatedAdapter.SongViewHolder>() {
    // Dynamically update the item views when data change
    private var songs: List<Song> = emptyList()

    class SongViewHolder(private val binding: ItemSongCarouselBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            Log.d("Top Rated Adapter", song.name)
            Log.d("Top Rated Adapter", "${song.artist}")
            //TODO: cannot load song details
//            binding.trackName.text = song.name
//            binding.artistName.text = song.artist
            if (!song.imgUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context).load(song.imgUrl).into(binding.albumImage)
            } else {
                binding.albumImage.setImageResource(R.drawable.default_album_cover)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding =
            ItemSongCarouselBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
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