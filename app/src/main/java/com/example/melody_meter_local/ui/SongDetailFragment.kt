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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.melody_meter_local.R
import com.example.melody_meter_local.databinding.FragmentSongDetailBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator

//TODO: album image

class SongDetailFragment : Fragment() {
    private var _binding: FragmentSongDetailBinding? = null
    private val binding get() = _binding!!

    private val args: SongDetailFragmentArgs by navArgs()
    private val song by lazy { args.song }

    private lateinit var auth: FirebaseAuth
    private lateinit var userDbReference: DatabaseReference
    private lateinit var songDbReference: DatabaseReference

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
        auth = FirebaseAuth.getInstance()
        userDbReference = FirebaseDatabase.getInstance().getReference("Users")
        songDbReference = FirebaseDatabase.getInstance().getReference("Songs")

        showSongDetails()
        saveToFavorites()
        backNavigation()
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
                binding.rating.setText(R.string.default_no_rating)
            } else {
                val average = it.ratings.average()
                binding.rating.text = "${average} (based on ${it.ratings.size} ratings)"
            }

            binding.spotifyUrl.setOnClickListener {
                val url = "https://open.spotify.com/track/${song.spotifyTrackId}"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }

            binding.btnRate.setOnClickListener {
                if (auth.currentUser == null) {
                    showLoginPrompt()
                } else {
                    showRatingDialog()
                }
            }
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
                    saveRating(rating)
                })
        }
    }

    private fun saveRating(rating: Double) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = userDbReference.child(uid)
        val songRef = songDbReference.child(song.spotifyTrackId)

        // Task to update the song ratings
        val songTask = songRef.child("ratings").get().continueWithTask { dataSnapshot ->
            if (dataSnapshot.isSuccessful) {
                val songRatings = dataSnapshot.result.getValue(object :
                    GenericTypeIndicator<MutableList<Double>>() {}) ?: mutableListOf()
                songRatings.add(rating)
                songRef.child("ratings").setValue(songRatings)
            } else {
                throw dataSnapshot.exception ?: Exception("Failed to get song ratings")
            }
        }

        // Task to update the user ratings
        val userTask = userRef.child(uid).get().continueWithTask { dataSnapshot ->
            if (dataSnapshot.isSuccessful) {
                val userRatings = dataSnapshot.result.getValue(object :
                    GenericTypeIndicator<MutableList<MutableMap<String, Double>>>() {})
                    ?: mutableListOf()
                userRatings.add(mutableMapOf(song.spotifyTrackId to rating))
                userRef.child("ratings").setValue(userRatings)
            } else {
                throw dataSnapshot.exception ?: Exception("Failed to get user ratings")
            }
        }

        // Run both tasks in parallel
        Tasks.whenAll(songTask, userTask)
            .addOnSuccessListener {
                Toast.makeText(context, "Your rating has been submitted.", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Log.e("Song Detail Fragment", "Failed to save rating", e)
                Toast.makeText(
                    context,
                    "Failed to submit your rating. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    private fun saveToFavorites() {
        // if user is not logged in, set the save button to unpressed
        if (auth.currentUser == null) {
            binding.saveButton.setImageResource(R.drawable.ic_save_unpressed)
        }
        // if user is logged in, check if the song is already in his favorites list and displays the icon accordingly
        else {
            val uid = auth.currentUser!!.uid
            val userRef = userDbReference.child(uid)

            userRef.child("favorites").get()
                .addOnSuccessListener { dataSnapshot ->
                    val favorites = dataSnapshot.getValue(object :
                        GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()
                    if (favorites.contains(song.spotifyTrackId)) {
                        binding.saveButton.setImageResource(R.drawable.ic_save_pressed)
                    } else {
                        binding.saveButton.setImageResource(R.drawable.ic_save_unpressed)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("SongDetailFragment", "Failed to get user favorites", e)
                }
        }

        binding.saveButton.setOnClickListener {
            if (auth.currentUser == null) {
                val loginDialogFragment = LoginDialogFragment()
                loginDialogFragment.show(parentFragmentManager, "loginDialogFragment")
            } else {
                handleSaveButtonClick()
            }
        }
    }

    private fun handleSaveButtonClick() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = userDbReference.child(uid)

        userRef.child("favorites").get()
            .addOnSuccessListener { dataSnapshot ->
                val favorites =
                    dataSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {})
                        ?: mutableListOf()
                if (favorites.contains(song.spotifyTrackId)) {
                    favorites.remove(song.spotifyTrackId)
                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                    binding.saveButton.setImageResource(R.drawable.ic_save_unpressed)
                } else {
                    favorites.add(song.spotifyTrackId)
                    Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
                    binding.saveButton.setImageResource(R.drawable.ic_save_pressed)
                }
                userRef.child("favorites").setValue(favorites)
            }
            .addOnFailureListener { e ->
                Log.e("SongDetailFragment", "Failed to get user favorites", e)
            }
    }


    // handle navigation back to the search results
    private fun backNavigation() {
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
}