package com.example.melody_meter_local.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.melody_meter_local.adapter.RecentSearchesAdapter
import com.example.melody_meter_local.adapter.SearchResultsAdapter
import com.example.melody_meter_local.databinding.FragmentSearchBinding
import com.example.melody_meter_local.viewmodel.LoginViewModel
import com.example.melody_meter_local.viewmodel.SearchViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
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
        Log.d("SearchFragment", "Create View loggedin ${loginViewModel.isLoggedIn.value} isSearching ${searchViewModel.isSearching.value}")
        return binding.root
    }

    //TODO: login prompt incorrectly shown when orientation changes
    //TODO: recent searches incorrectly shown with search results when orientation changes

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("SearchFragment", "View Created, ${savedInstanceState}")
        setUpAdapters()
        setUpListeners()
        Log.d("SearchFragment", "View Created loggedin ${loginViewModel.isLoggedIn.value} isSearching ${searchViewModel.isSearching.value}")
        observeViewModel()
    }

    override fun onPause() {
        super.onPause()
        Log.d("SearchFragment", "View Paused loggedin ${loginViewModel.isLoggedIn.value} isSearching ${searchViewModel.isSearching.value}")
        allViewsGone()
    }

    override fun onResume() {
        super.onResume()
        Log.d("SearchFragment", "View Resumed loggedin ${loginViewModel.isLoggedIn.value} isSearching ${searchViewModel.isSearching.value}")
        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("SearchFragment", "View Destroyed loggedin ${loginViewModel.isLoggedIn.value} isSearching ${searchViewModel.isSearching.value}")
        //allViewsGone()
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

    private fun setUpListeners() {
        binding.clearAllSearches.setOnClickListener {
            searchViewModel.clearAllSearches()
        }

        binding.searchView.setOnCloseListener {
            searchViewModel.setIsSearching(false)
            false
        }

        // handle SearchView results
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val trimmedQuery = it.trim()
                    if (trimmedQuery.isNotEmpty()) {
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
            Log.d("SearchFragment", "search results updated: $results")
        }

        // Fetch recent searches
        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.fetchRecentSearches()
        }

        searchViewModel.recentSearches.observe(viewLifecycleOwner) { recentSearches ->
            recentSearchesAdapter.submitList(recentSearches)
            Log.d("SearchFragment", "Recent searches updated: $recentSearches")
        }

        loginViewModel.isLoggedIn.observe(viewLifecycleOwner) {
            updateUI()
        }

        searchViewModel.isSearching.observe(viewLifecycleOwner) {
            updateUI()
        }

    }

    private fun updateUI() {
        allViewsGone()  //always reset first

        val isSearching = searchViewModel.isSearching.value ?: false
        val isLoggedIn = loginViewModel.isLoggedIn.value ?: false
        Log.d("SearchFragment", "UpdateUI isLoggedIn ${isLoggedIn} isSearching ${isSearching} ")

        if (isSearching) {
            binding.recentSearchesGroup.visibility = View.GONE
            binding.searchResultsGroup.visibility = View.VISIBLE
        } else {
            binding.recentSearchesGroup.visibility = View.VISIBLE
            binding.searchResultsGroup.visibility = View.GONE

            if (isLoggedIn) {
                binding.recentSearchesTitleWrapper.visibility = View.VISIBLE
                binding.loginPromptWrapper.visibility = View.GONE
                binding.recentSearchesRecyclerView.visibility = View.VISIBLE

            } else {
                // Clear search results and recent searches for unlogged-in users
                recentSearchesAdapter.submitList(emptyList())
                searchResultsAdapter.submitList(emptyList())

                // prompt user to login to retrieve his recent searches
                binding.recentSearchesTitleWrapper.visibility = View.VISIBLE
                binding.loginPromptWrapper.visibility = View.VISIBLE
                binding.recentSearchesRecyclerView.visibility = View.GONE
            }
        }
        Log.d("SearchFragment", "Recent searches group visibility: ${binding.recentSearchesGroup.visibility}")
        Log.d("SearchFragment", "Search results group visibility: ${binding.searchResultsGroup.visibility}")
        Log.d("SearchFragment", "Recent searches title visibility: ${binding.recentSearchesTitleWrapper.visibility}")
        Log.d("SearchFragment", "Login prompt visibility: ${binding.loginPromptWrapper.visibility}")
    }

    private fun allViewsGone(){
        binding.recentSearchesGroup.visibility = View.GONE
        binding.recentSearchesTitleWrapper.visibility = View.GONE
        binding.recentSearchesRecyclerView.visibility = View.GONE
        binding.loginPromptWrapper.visibility = View.GONE
        binding.searchResultsGroup.visibility = View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save UI state here
        outState.putBoolean("IS_SEARCHING", searchViewModel.isSearching.value ?: false)
        outState.putBoolean("IS_LOGGEDIN", loginViewModel.isLoggedIn.value ?: false)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            val isSearching = savedInstanceState.getBoolean("IS_SEARCHING", false)
            searchViewModel.setIsSearching(isSearching)
            val isLoggedIn = savedInstanceState.getBoolean("IS_LOGGEDIN", false)
            Log.d("SearchFragment", "viewStateRestored isLoggedin $isLoggedIn")
        }
    }
}