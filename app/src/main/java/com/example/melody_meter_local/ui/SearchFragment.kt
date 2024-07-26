package com.example.melody_meter_local.ui

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.melody_meter_local.R
import com.example.melody_meter_local.adapter.RecentSearchesAdapter
import com.example.melody_meter_local.adapter.SearchResultsAdapter
import com.example.melody_meter_local.model.Song
import com.example.melody_meter_local.databinding.FragmentSearchBinding
import com.example.melody_meter_local.di.PopularSearchesDatabaseReference
import com.example.melody_meter_local.di.SongDatabaseReference
import com.example.melody_meter_local.di.UserDatabaseReference
import com.example.melody_meter_local.viewmodel.LoginViewModel
import com.example.melody_meter_local.viewmodel.SearchViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val searchViewModel: SearchViewModel by hiltNavGraphViewModels(R.id.mobile_navigation)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerViews()
        setUpUI()

        // handle SearchView results
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val trimmedQuery = it.trim()
                    if (trimmedQuery.isNotEmpty()) {
                        searchViewModel.saveSearchQuery(trimmedQuery)
                        searchViewModel.performSearch(trimmedQuery)
                        searchViewModel.setIsSearching(true)
                        setUpUI()
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let{
                    val trimmedNewQuery = it.trim()
                    if (trimmedNewQuery.isEmpty()){
                        searchViewModel.setIsSearching(false)
                        setUpUI()
                    }
                }
                return false
            }
        })

        // observe search results changes
        searchViewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchResultsAdapter.submitList(results)
            binding.noResultMsg.visibility = if (searchViewModel.isSearching.value == true && results.isEmpty()) View.VISIBLE else View.GONE
        }

        searchViewModel.recentSearches.observe(viewLifecycleOwner) { recentSearches ->
            recentSearchesAdapter.submitList(recentSearches)
        }

        binding.clearAllSearches.setOnClickListener {
            searchViewModel.clearAllSearches()
        }

//        binding.clearAllSearches.setOnClickListener {
//            recentSearches.clear()
//            recentSearchesAdapter.notifyDataSetChanged()
//        }

//            TODO: onclick listeners not working
        binding.btnLogin.setOnClickListener {
            val loginDialogFragment = LoginDialogFragment()
            loginDialogFragment.show(parentFragmentManager, "loginDialogFragment")
        }
    }

    private fun setUpUI() {
        searchViewModel.isSearching.observe(viewLifecycleOwner) { isSearching ->
            val isLoggedIn = loginViewModel.isLoggedIn.value ?: false
            updateUI(isSearching, isLoggedIn)
        }
    }

    // TODO: search results not hidden when navigated back to search fragment
    override fun onResume() {
        super.onResume()
        //setUpUI()
        searchViewModel.loadRecentSearches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI(isSearching: Boolean, isLoggedIn: Boolean) {
        if(isSearching){
            binding.recentSearchesGroup.visibility = View.GONE
            binding.searchResultsGroup.visibility = View.VISIBLE
            binding.noResultMsg.visibility = if(searchResultsAdapter.itemCount == 0) View.VISIBLE else View.GONE
        }
        else {
            if(isLoggedIn){
                binding.loginPromptWrapper.visibility = View.GONE
                //loadRecentSearches()
                binding.recentSearchesRecyclerView.visibility = View.VISIBLE
                binding.clearAllSearches.visibility = View.VISIBLE
            }
            else{
                // Clear search results and recent searches for unlogged-in users
//                searchResults.clear()
//                recentSearches.clear()
//                searchResultsAdapter.notifyDataSetChanged()
//                recentSearchesAdapter.notifyDataSetChanged()

                // prompt user to login to retrieve his recent searches
                binding.recentSearchesRecyclerView.visibility = View.GONE
                binding.clearAllSearches.visibility = View.GONE
                binding.loginPromptWrapper.visibility = View.VISIBLE
            }

            binding.recentSearchesGroup.visibility = View.VISIBLE
            binding.searchResultsGroup.visibility = View.GONE
        }
    }

    private fun setUpRecyclerViews(){
        // set up recent search view
        recentSearchesAdapter = RecentSearchesAdapter { searchItem ->
            searchViewModel.removeSearchQuery(searchItem)
        }
        binding.recentSearchesRecyclerView.apply{
            layoutManager = LinearLayoutManager(context)
            adapter = recentSearchesAdapter
        }

        // set up search results view
        searchResultsAdapter = SearchResultsAdapter { song ->
            val action = SearchFragmentDirections.actionSearchFragmentToSongDetailFragment(song)
            findNavController().navigate(action)
        }
        binding.searchResultsRecyclerView.apply{
            layoutManager = LinearLayoutManager(context)
            adapter = searchResultsAdapter
        }
    }
}