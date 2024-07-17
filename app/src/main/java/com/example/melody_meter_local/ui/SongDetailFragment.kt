package com.example.melody_meter_local.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.melody_meter_local.R
import com.example.melody_meter_local.databinding.FragmentSongDetailBinding
import com.example.melody_meter_local.viewmodel.SongDetailViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

//TODO: album image

@AndroidEntryPoint
class SongDetailFragment : Fragment() {
    private var _binding: FragmentSongDetailBinding? = null
    private val binding get() = _binding!!

    private val args: SongDetailFragmentArgs by navArgs()
    private val song by lazy { args.song }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val songDetailViewModel: SongDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSongDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showSongDetails()
        updateSaveButtonState()
        setupObservers()
        setupListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showSongDetails() {
        song.let {
            binding.trackName.text = it.name
            binding.artistName.text = it.artist
            // load album image from spotify
            if (!it.imgUrl.isNullOrEmpty()) {
                // Load image into binding.albumImage using Glide
                Glide.with(this)
                    .load(it.imgUrl)
                    .into(binding.albumImage)
                Log.d("SongDetailFragment", "Load Image URL: ${it.imgUrl}")
            } else {
                binding.albumImage.setImageResource(R.drawable.default_album_cover)
                Log.d("SongDetailFragment", "Cannot Load Image URL: ${it.imgUrl}")
            }
            // show community rating of the song
            if (it.ratings.isEmpty()) {
                binding.rating.text = getString(R.string.default_no_rating)
            } else {
                val average = it.ratings.average()
                binding.rating.text = "${average} (based on ${it.ratings.size} ratings)"
            }
        }
    }

    private fun updateSaveButtonState() {
        if (auth.currentUser == null) {
            binding.saveButton.setImageResource(R.drawable.ic_save_unpressed)
        } else {
            songDetailViewModel.updateFavoriteState(song.spotifyTrackId)
        }
    }

    private fun setupObservers() {
        songDetailViewModel.isFavorite.observe(viewLifecycleOwner, Observer { isFavorite ->
            binding.saveButton.setImageResource(
                if (isFavorite) R.drawable.ic_save_pressed else R.drawable.ic_save_unpressed
            )
        })

        songDetailViewModel.ratingSubmissionStatus.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess) {
                Toast.makeText(context, "Your rating has been submitted.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to submit rating. Please try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupListeners() {
        binding.spotifyUrl.setOnClickListener {
            val url = "https://open.spotify.com/track/${song.spotifyTrackId}"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        binding.rateButton.setOnClickListener{
            if (auth.currentUser == null) {
                showLoginPrompt()
            } else {
                showRatingDialog()
            }
        }

        binding.saveButton.setOnClickListener {
            handleSaveButtonClick()
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )

    }

    private fun showLoginPrompt() {
        Toast.makeText(context, "You must be logged in to leave a rating.", Toast.LENGTH_LONG)
            .show()
        val loginDialogFragment = LoginDialogFragment()
        loginDialogFragment.show(parentFragmentManager, "loginDialogFragment")
    }

    private fun showRatingDialog() {
        val composeView = binding.composeView
        composeView.visibility = View.VISIBLE
        composeView.setContent {
            RatingDialogComponent(
                closeSelection = {
                    composeView.visibility = View.GONE
                },
                onRatingSelected = { rating ->
                    songDetailViewModel.saveRating(song.spotifyTrackId, rating)
                })
        }
    }

    private fun handleSaveButtonClick() {
        if (auth.currentUser == null) {
            showLoginPrompt()
        } else {
            songDetailViewModel.toggleFavorite(song.spotifyTrackId)
        }
    }


}