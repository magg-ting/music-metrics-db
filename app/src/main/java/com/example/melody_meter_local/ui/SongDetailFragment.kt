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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.melody_meter_local.R
import com.example.melody_meter_local.databinding.FragmentSongDetailBinding
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.viewmodel.SongDetailViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.round

private const val s = "Your rating has been submitted."

@AndroidEntryPoint
class SongDetailFragment : Fragment() {
    private var _binding: FragmentSongDetailBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var auth: FirebaseAuth

    private val args: SongDetailFragmentArgs by navArgs()
    //private val song by lazy { args.song }
    private val songDetailViewModel: SongDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSongDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val song = args.song
        showSongDetails(song)
        updateSaveButtonState()
        setupObservers()
        setupListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showSongDetails(song: Song) {
        song.let {
            binding.trackName.text = it.name
            binding.artistName.text = it.artist
            // load album image from spotify
            if (!it.imgUrl.isNullOrEmpty()) {
                // Load image into binding.albumImage using Glide
                Glide.with(this)
                    .load(it.imgUrl)
                    .into(binding.albumImage)
            } else {
                binding.albumImage.setImageResource(R.drawable.default_album_cover)

            }
            // show community rating of the song
            updateRatingUI(song)
        }
    }

    private fun updateRatingUI(song: Song){
        if (song.ratings.isEmpty()) {
            binding.rating.text = getString(R.string.default_no_rating)
        } else {
            song.avgRating = song.ratings.map { pair -> pair.values.first() }.average()
            binding.rating.text = String.format("%.1f", song.avgRating) + " (based on ${song.ratings.size} ratings)"
        }
    }

    private fun updateSaveButtonState() {
        if (auth.currentUser == null) {
            Log.d("SongDetailFragment", "Current User: ${auth.currentUser}")
            binding.saveButton.setImageResource(R.drawable.ic_save_unpressed)
        } else {
            songDetailViewModel.updateFavoriteState(args.song.spotifyTrackId)
        }
    }

    private fun setupObservers() {
        songDetailViewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            binding.saveButton.setImageResource(
                if (isFavorite == true) R.drawable.ic_save_pressed else R.drawable.ic_save_unpressed
            )
        }

        songDetailViewModel.ratingSubmissionStatus.observe(viewLifecycleOwner) { isSuccess ->
            when (isSuccess) {
                true -> {
                    Toast.makeText(context, "Your rating has been submitted.", Toast.LENGTH_SHORT).show()
                    songDetailViewModel.fetchSongDetails(args.song.spotifyTrackId)
                }
                false -> Toast.makeText(context, "Failed to submit rating. Please try again.", Toast.LENGTH_SHORT).show()
                null -> { } // No toast when isSuccess is null (i.e. reset)
            }
        }

        songDetailViewModel.songDetails.observe(viewLifecycleOwner) { updatedSong ->
            if (updatedSong != null) {
                showSongDetails(updatedSong)
            }
        }
    }

    private fun setupListeners() {
        binding.spotifyUrl.setOnClickListener {
            val url = "https://open.spotify.com/track/${args.song.spotifyTrackId}"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        binding.rateButton.setOnClickListener{
            Log.d("SongDetailFragment", "SetupListeners. Rating User: ${auth.currentUser}")
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
            songDetailViewModel.clearState()
            findNavController().navigateUp()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    songDetailViewModel.clearState()
                    findNavController().navigateUp()
                }
            }
        )

    }

    private fun handleSaveButtonClick() {
        Log.d("SongDetailFragment", "Handle save button click. Current user: ${auth.currentUser}")
        if (auth.currentUser == null) {
            showLoginPrompt()
        } else {
            songDetailViewModel.toggleFavorite(args.song.spotifyTrackId)
        }
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
                    songDetailViewModel.saveRating(args.song, rating)
                })
        }
    }

}