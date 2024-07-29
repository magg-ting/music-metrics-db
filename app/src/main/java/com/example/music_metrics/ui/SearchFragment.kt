package com.example.music_metrics.ui

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
import com.example.music_metrics.adapter.RecentSearchesAdapter
import com.example.music_metrics.adapter.SearchResultsAdapter
import com.example.music_metrics.databinding.FragmentSearchBinding
import com.example.music_metrics.viewmodel.LoginViewModel
import com.example.music_metrics.viewmodel.SearchViewModel
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
        return binding.root
    }

    //TODO: when orientation changes, all views are invisible if not searching
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("SearchFragment", "Current user: ${auth.currentUser?.uid}")
        setUpAdapters()
        setUpListeners()
        observeViewModel()
    }

    private fun setUpAdapters() {
        // set up adapters
        recentSearchesAdapter = RecentSearchesAdapter(
            onTextClick = { query ->
                binding.searchView.setQuery(query, true)
            },
            onClearClick = { searchItem ->
                searchViewModel.removeSearchQuery(searchItem)
            }
        )
        searchResultsAdapter = SearchResultsAdapter { song ->
            val action = SearchFragmentDirections.actionSearchFragmentToSongDetailFragment(song)
            findNavController().navigate(action)
        }

        // apply to recylcer views
        binding.recentSearchesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentSearchesAdapter
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
        }

        searchViewModel.recentSearches.observe(viewLifecycleOwner) { recentSearches ->
            recentSearchesAdapter.submitList(recentSearches)
            Log.d("SearchFragment", "Recent searches updated: $recentSearches")
        }

        loginViewModel.isLoggedIn.observe(viewLifecycleOwner) {
            updateUI()
            if (it == true) {
                viewLifecycleOwner.lifecycleScope.launch {
                    searchViewModel.refreshRecentSearches()
                    searchViewModel.fetchRecentSearches()
                    searchViewModel.fetchSearchResults()
                }
            }
        }

        searchViewModel.isSearching.observe(viewLifecycleOwner) {
            updateUI()
            if(it == false){
                viewLifecycleOwner.lifecycleScope.launch {
                    searchViewModel.refreshRecentSearches()
                    searchViewModel.fetchRecentSearches()
                }
            }

        }
    }

    private fun updateUI() {
        allViewsGone()  //always reset first

        Log.d("SearchFragment", "Recent searches recycler visibility: ${binding.recentSearchesRecyclerView.visibility}")
        Log.d("SearchFragment", "Search results group visibility: ${binding.searchResultsGroup.visibility}")
        Log.d("SearchFragment", "Recent searches title visibility: ${binding.recentSearchesTitleWrapper.visibility}")
        Log.d("SearchFragment", "Login prompt visibility: ${binding.loginPromptWrapper.visibility}")

        val isSearching = searchViewModel.isSearching.value ?: false
        val isLoggedIn = loginViewModel.isLoggedIn.value ?: false
        Log.d("SearchFragment", "Update UI, isLoggedIn $isLoggedIn isSearching $isSearching")

        binding.recentSearchesTitleWrapper.visibility = if (!isSearching) View.VISIBLE else View.GONE
        binding.loginPromptWrapper.visibility = if (!isSearching && !isLoggedIn) View.VISIBLE else View.GONE
        binding.recentSearchesRecyclerView.visibility = if (!isSearching && isLoggedIn) View.VISIBLE else View.GONE
        binding.searchResultsGroup.visibility = if (isSearching) View.VISIBLE else View.GONE

        Log.d("SearchFragment", "Recent searches recycler visibility: ${binding.recentSearchesRecyclerView.visibility}")
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

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}