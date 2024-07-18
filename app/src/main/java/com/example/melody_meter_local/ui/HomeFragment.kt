package com.example.melody_meter_local.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.melody_meter_local.adapter.NewReleasesAdapter
import com.example.melody_meter_local.adapter.TopRatedAdapter
import com.example.melody_meter_local.adapter.TrendingAdapter
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.databinding.FragmentHomeBinding
import com.example.melody_meter_local.model.spotify.Artist
import com.example.melody_meter_local.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var topRatedAdapter: TopRatedAdapter
    private lateinit var trendingAdapter: TrendingAdapter
    private lateinit var newReleasesAdapter : NewReleasesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        setupRecyclerViews()
    }

    private fun setupRecyclerViews() {
        // Top Rated Section
        topRatedAdapter = TopRatedAdapter()
        binding.topRatedRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = topRatedAdapter
        }
        // Observe the LiveData from the ViewModel and update the adapter when data changes
        homeViewModel.topRatedSongs.observe(viewLifecycleOwner) { songs ->
            topRatedAdapter.submitList(songs)
        }

        // Trending Section
        trendingAdapter = TrendingAdapter()
        binding.trendingRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = trendingAdapter
        }
        // Observe the LiveData from the ViewModel and update the adapter when data changes
        homeViewModel.trendingSongs.observe(viewLifecycleOwner) { songs ->
            trendingAdapter.submitList(songs)
        }

        // New Releases Section
        newReleasesAdapter = NewReleasesAdapter()
        binding.newReleasesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = newReleasesAdapter
        }
        // Observe the LiveData from the ViewModel and update the adapter when data changes
        homeViewModel.newReleases.observe(viewLifecycleOwner) { albums ->
            newReleasesAdapter.submitList(albums)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}