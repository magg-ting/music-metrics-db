package com.example.music_metrics.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.music_metrics.adapter.AlbumAdapter
import com.example.music_metrics.databinding.FragmentAlbumBinding
import com.example.music_metrics.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumFragment : Fragment() {
    private var _binding: FragmentAlbumBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val albumViewModel: AlbumViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve album from arguments
        val album = AlbumFragmentArgs.fromBundle(requireArguments()).album
        albumViewModel.selectAlbum(album)

        val albumAdapter = AlbumAdapter { song ->
            val action =
                AlbumFragmentDirections.actionAlbumFragmentToSongDetailFragment(song)
            findNavController().navigate(action)
        }
        binding.albumSongRecyclerView.adapter = albumAdapter

        albumViewModel.selectedAlbum.observe(viewLifecycleOwner) { album ->
            album.let {
                binding.albumName.text = it.name
                binding.artistName.text = it.artists.joinToString(", ") { artist -> artist.name }

                Glide.with(requireContext())
                    .load(it.images?.first()?.url)
                    .into(binding.albumImage)
            }
        }

        albumViewModel.albumTracks.observe(viewLifecycleOwner) { songs ->
            albumAdapter.submitList(songs)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}