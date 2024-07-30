package com.example.music_metrics.ui

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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.music_metrics.R
import com.example.music_metrics.databinding.FragmentSongDetailBinding
import com.example.music_metrics.model.Song
import com.example.music_metrics.viewmodel.SongDetailViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SongDetailFragment : Fragment() {
    private var _binding: FragmentSongDetailBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var auth: FirebaseAuth

    private val args: SongDetailFragmentArgs by navArgs()
    private val song by lazy { args.song }
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
        Log.d("SongDetailFragment", "onViewCreated")
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

            // find current user rating
            val currentUserRating = song.ratings.find { it.contains(auth.currentUser?.uid ?: "") }
            val userRatingText = if (currentUserRating != null) {
                "Your rating: ${currentUserRating.values.first()}"
            } else {
                ""
            }

            // update song rating text
            binding.rating.text =
                "${String.format("%.1f", song.avgRating)} (based on ${song.ratings.size} ratings)\n$userRatingText"
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
                    val toastMessage = if (auth.currentUser?.uid in args.song.ratings.map { it.keys.first() }) {
                        "Your rating has been successfully updated."
                    } else {
                        "Your rating has been submitted."
                    }
                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                    songDetailViewModel.fetchSongDetails(args.song.spotifyTrackId)
                }
                false -> Toast.makeText(context, "Failed to submit rating. Please try again.", Toast.LENGTH_SHORT).show()
                null -> { } // No toast when isSuccess is null (i.e. reset)
            }
        }

        songDetailViewModel.songDetails.observe(viewLifecycleOwner) { updatedSong ->
            Log.d("SongDetailFragment", "Updated Song: $updatedSong")
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

        //TODO: the rate button is only working on the first click but not subsequent ones
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
            songDetailViewModel.toggleFavorite(args.song.spotifyTrackId, args.song)
        }
    }

    private fun showLoginPrompt() {
        Toast.makeText(context, "You must be logged in to leave a rating.", Toast.LENGTH_LONG)
            .show()
        val loginDialogFragment = LoginDialogFragment()
        loginDialogFragment.show(parentFragmentManager, "loginDialogFragment")
    }

    //TODO (future enhancement): change rating colors (the library doesn't seem to provide an option for this)
    private fun showRatingDialog() {
        Log.d("SongDetailFragment", "Showing rating dialog")
        val composeView = binding.composeView
        composeView.visibility = View.VISIBLE
        composeView.setContent {
            Log.d("SongDetailFragment", "Setting content for rating dialog")
            RatingDialogComponent(
                closeSelection = {
                    Log.d("SongDetailFragment", "Closing rating dialog")
                    composeView.visibility = View.GONE
                },
                onRatingSelected = { rating ->
                    Log.d("SongDetailFragment", "Rating selected: $rating")
                    songDetailViewModel.saveRating(args.song, rating)
                    composeView.visibility = View.GONE
                })
        }
    }

}