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
import com.example.music_metrics.adapter.RatingHistoryAdapter
import com.example.music_metrics.databinding.FragmentRatingHistoryBinding
import com.example.music_metrics.model.Song
import com.example.music_metrics.viewmodel.RatingHistoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RatingHistoryFragment : Fragment() {

    private var _binding: FragmentRatingHistoryBinding? = null
    private val binding get() = _binding!!

    private val ratingHistoryViewModel: RatingHistoryViewModel by activityViewModels()
    private lateinit var ratingHistoryAdapter: RatingHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRatingHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loadingView.visibility = View.GONE

        ratingHistoryAdapter = RatingHistoryAdapter { song ->
            val action =
                RatingHistoryFragmentDirections.actionRatingHistoryFragmentToSongDetailFragment(song)
            findNavController().navigate(action)
        }

        binding.ratingHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ratingHistoryAdapter
        }

        ratingHistoryViewModel.ratingHistory.observe(viewLifecycleOwner) { ratings ->
            ratingHistoryAdapter.submitList(ratings)
            Log.d("RatingHistoryFragment", "${ratings.first()}")
            showHistory(ratings)
        }

        ratingHistoryViewModel.isLoading.observe(viewLifecycleOwner){
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
        ratingHistoryViewModel.fetchRatingHistory()

    }

    private fun showHistory(ratings: List<Pair<Song, Double>>) {
        if (ratings.isNotEmpty()) {
            binding.noHistoryMsg.visibility = View.GONE
            binding.ratingHistoryRecyclerView.visibility = View.VISIBLE
        } else {
            binding.noHistoryMsg.visibility = View.VISIBLE
            binding.ratingHistoryRecyclerView.visibility = View.GONE
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