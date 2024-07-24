package com.example.melody_meter_local.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.melody_meter_local.databinding.ItemAlbumSongBinding
import com.example.melody_meter_local.model.Song

class AlbumAdapter(
    private val onItemClick: (Song) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumSongViewHolder>() {

    private var songs: List<Song> = emptyList()

    class AlbumSongViewHolder(
        itemView: View,
        private val onItemClick: (Song) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemAlbumSongBinding.bind(itemView)
        fun bind(song: Song) {
            binding.songTitle.text = song.name

            itemView.setOnClickListener {
                onItemClick(song)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumSongViewHolder {
        // Inflate your item layout here
        val binding =
            ItemAlbumSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumSongViewHolder(binding.root, onItemClick)
    }

    override fun onBindViewHolder(holder: AlbumSongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount() = songs.size

    fun submitList(newList: List<Song>) {
        songs = newList
        notifyDataSetChanged()
    }

}