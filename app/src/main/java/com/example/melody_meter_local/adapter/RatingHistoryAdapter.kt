package com.example.melody_meter_local.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.melody_meter_local.R
import com.example.melody_meter_local.databinding.ItemRatingRecordBinding
import com.example.melody_meter_local.model.Song
//import com.example.melody_meter_local.viewmodel.RatingHistoryViewModel

class RatingHistoryAdapter(
    //private val viewModel: RatingHistoryViewModel,
    private val onItemClick: (Song) -> Unit
) : RecyclerView.Adapter<RatingHistoryAdapter.RatingHistoryViewHolder>() {
    private var ratedSongs: List<Pair<Song, Double>> = emptyList()

    inner class RatingHistoryViewHolder(
        itemView: View,
        private val onItemClick: (Song) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemRatingRecordBinding.bind(itemView)
        fun bind(item: Pair<Song, Double>) {
            binding.trackName.text = item.first.name
            binding.trackArtist.text = item.first.artist
            if (!item.first.imgUrl.isNullOrEmpty()) {
                // Load image into binding.albumImg using Glide
                Glide.with(binding.root).load(item.first.imgUrl).into(binding.albumImg)
            } else {
                binding.albumImg.setImageResource(R.drawable.default_album_cover)
            }
            binding.rating.text = item.second.toString()

            itemView.setOnClickListener {
                onItemClick(item.first)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingHistoryViewHolder {
        val binding =
            ItemRatingRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RatingHistoryViewHolder(binding.root, onItemClick)
    }

    override fun onBindViewHolder(holder: RatingHistoryViewHolder, position: Int) {
        val item = ratedSongs[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return ratedSongs.size
    }

    fun submitList(newFavorites: List<Pair<Song, Double>>) {
        ratedSongs = newFavorites
        notifyDataSetChanged()
    }

}