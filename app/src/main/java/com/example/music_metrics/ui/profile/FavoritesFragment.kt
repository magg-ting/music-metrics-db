package com.example.music_metrics.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music_metrics.adapter.FavoritesAdapter
import com.example.music_metrics.databinding.FragmentFavoritesBinding
import com.example.music_metrics.model.Song
import com.example.music_metrics.viewmodel.FavoritesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val favoritesViewModel: FavoritesViewModel by activityViewModels()
    private lateinit var favoritesAdapter: FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loadingView.visibility = View.GONE

        favoritesAdapter = FavoritesAdapter(
            onItemClick = { song ->
                    val action =
                        FavoritesFragmentDirections.actionFavoritesFragmentToSongDetailFragment(song)
                    findNavController().navigate(action)
                },
            onFavoriteToggle = {song: Song, isFavorite: Boolean ->
                Log.d("FavoritesFragment", "onToggle for ${song.name}")
                if (isFavorite) {
                    favoritesViewModel.addFavorite(song.spotifyTrackId)
                } else {
                    favoritesViewModel.removeFavorite(song.spotifyTrackId)
                }
            }
        )

        binding.favoritesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = favoritesAdapter
        }

        favoritesViewModel.favoriteSongs.observe(viewLifecycleOwner) { songs ->
            favoritesAdapter.updateFavorites(songs)
            showFavorites(songs)
        }

        favoritesViewModel.isLoading.observe(viewLifecycleOwner) {
            showLoading(it)
        }

        binding.btnBack.setOnClickListener {
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

        favoritesViewModel.fetchFavoriteSongs()

    }

    private fun showFavorites(songs: List<Song>) {
        if (songs.isNotEmpty()) {
            binding.noFavoritesMsg.visibility = View.GONE
            binding.favoritesRecyclerView.visibility = View.VISIBLE
        } else {
            binding.noFavoritesMsg.visibility = View.VISIBLE
            binding.favoritesRecyclerView.visibility = View.GONE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
