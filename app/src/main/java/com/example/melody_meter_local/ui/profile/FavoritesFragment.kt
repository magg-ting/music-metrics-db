package com.example.melody_meter_local.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.melody_meter_local.adapter.FavoritesAdapter
import com.example.melody_meter_local.databinding.FragmentFavoritesBinding
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.viewmodel.FavoritesViewModel
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

        favoritesAdapter = FavoritesAdapter(favoritesViewModel) { song ->
            val action =
                FavoritesFragmentDirections.actionFavoritesFragmentToSongDetailFragment(song)
            findNavController().navigate(action)
        }

        binding.favoritesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = favoritesAdapter
        }

        favoritesViewModel.favoriteSongs.observe(viewLifecycleOwner) { songs ->
            favoritesAdapter.updateFavorites(songs)
            showFavorites(songs)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    //TODO: remove unsaved song when navigating back to the favorites fragment

}
