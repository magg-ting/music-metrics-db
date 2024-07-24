package com.example.melody_meter_local.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.melody_meter_local.adapter.RatingHistoryAdapter
import com.example.melody_meter_local.databinding.FragmentRatingHistoryBinding
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.viewmodel.RatingHistoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RatingHistoryFragment : Fragment() {
    private var _binding: FragmentRatingHistoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val ratingHistoryViewModel: RatingHistoryViewModel by viewModels()
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

        ratingHistoryAdapter = RatingHistoryAdapter{ song ->
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
            showHistory(ratings)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

}