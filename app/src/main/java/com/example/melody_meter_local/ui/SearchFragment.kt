package com.example.melody_meter_local.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.melody_meter_local.adapter.RecentSearchesAdapter
import com.example.melody_meter_local.adapter.SearchResultsAdapter
import com.example.melody_meter_local.databinding.FragmentSearchBinding
import com.example.melody_meter_local.viewmodel.LoginViewModel
import com.example.melody_meter_local.viewmodel.SearchViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val searchViewModel: SearchViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()

    private lateinit var recentSearchesAdapter: RecentSearchesAdapter
    private lateinit var searchResultsAdapter: SearchResultsAdapter

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    //TODO: recent searches of logged in user not populated when view is first created
    //TODO: login prompt incorrectly shown when orientation changes
    //TODO: recent searches incorrectly shown with search results when orientation changes
    //TODO: app crashes when searchview is on close
    //TODO: avg rating is not shown in search results items
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("SearchFragment", "View Created")
        setUpAdapters()
        setUpUI()
        Log.d("SearchFragment", "loggedin ${loginViewModel.isLoggedIn.value} isSearching ${searchViewModel.isSearching.value}")
        observeViewModel()

        // handle SearchView results
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val trimmedQuery = it.trim()
                    if (trimmedQuery.isNotEmpty()) {
                        searchViewModel.saveSearchQuery(trimmedQuery)
                        searchViewModel.performSearch(trimmedQuery)
                        searchViewModel.setIsSearching(true)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }


    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpAdapters() {
        // set up recent search adapter
        recentSearchesAdapter = RecentSearchesAdapter(
            onTextClick = { query ->
                binding.searchView.setQuery(query, true)
            },
            onClearClick = { searchItem ->
                searchViewModel.removeSearchQuery(searchItem)
            }
        )
        binding.recentSearchesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentSearchesAdapter
        }

        // set up search results adapter
        searchResultsAdapter = SearchResultsAdapter { song ->
            val action = SearchFragmentDirections.actionSearchFragmentToSongDetailFragment(song)
            findNavController().navigate(action)
        }
        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchResultsAdapter
        }
    }

    private fun setUpUI() {
        binding.clearAllSearches.setOnClickListener {
            searchViewModel.clearAllSearches()
        }

        binding.searchView.setOnCloseListener {
            searchViewModel.setIsSearching(false)
            //searchResultsAdapter.submitList(emptyList())
            false
        }

        binding.searchView.setOnSearchClickListener {
            searchViewModel.setIsSearching(true)
        }

        binding.btnLogin.setOnClickListener {
            val loginDialogFragment = LoginDialogFragment()
            loginDialogFragment.show(parentFragmentManager, "loginDialogFragment")
        }
    }

    private fun observeViewModel() {
        searchViewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchResultsAdapter.submitList(results)
        }

        searchViewModel.recentSearches.observe(viewLifecycleOwner) { recentSearches ->
            recentSearchesAdapter.submitList(recentSearches)
            Log.d("SearchFragment", "Recent searches updated: $recentSearches")
        }

        searchViewModel.isSearching.observe(viewLifecycleOwner) {
            updateUI()
        }

        loginViewModel.isLoggedIn.observe(viewLifecycleOwner) {
            updateUI()
        }
    }

    private fun updateUI() {
        val isSearching = searchViewModel.isSearching.value ?: false
        val isLoggedIn = loginViewModel.isLoggedIn.value ?: false
        Log.d("SearchFragment", "UpdateUI isSearching ${isSearching} isLoggedIn ${isLoggedIn}")
        if (isSearching) {
            binding.recentSearchesGroup.visibility = View.GONE
            binding.searchResultsGroup.visibility = View.VISIBLE
        } else {
            binding.recentSearchesGroup.visibility = View.VISIBLE
            binding.searchResultsGroup.visibility = View.GONE

            if (isLoggedIn) {
                binding.loginPromptWrapper.visibility = View.GONE
                binding.recentSearchesRecyclerView.visibility = View.VISIBLE
                binding.clearAllSearches.visibility = View.VISIBLE
            } else {
                // Clear search results and recent searches for unlogged-in users
                recentSearchesAdapter.submitList(emptyList())
                searchResultsAdapter.submitList(emptyList())

                // prompt user to login to retrieve his recent searches
                binding.recentSearchesRecyclerView.visibility = View.GONE
                binding.clearAllSearches.visibility = View.GONE
                binding.loginPromptWrapper.visibility = View.VISIBLE
            }
        }
    }
}