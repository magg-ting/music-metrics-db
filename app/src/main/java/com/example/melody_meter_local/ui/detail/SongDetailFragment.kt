package com.example.melody_meter_local.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.melody_meter_local.R
import com.example.melody_meter_local.databinding.FragmentSongDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class SongDetailFragment : Fragment() {
    private var _binding: FragmentSongDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var userDbReference: DatabaseReference
    private lateinit var songDbReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSongDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: SongDetailFragmentArgs by navArgs()
        val song = args.song

        song.let {
            binding.songName.text = it.name
            binding.artistName.text = it.artist
            binding.rating.text = it.avgRating.toString()
            fun listenOnSpotify(){
                val url = "https://open.spotify.com/track/${it.spotifyTrackId}"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            binding.spotifyUrl.setOnClickListener {
                listenOnSpotify()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}