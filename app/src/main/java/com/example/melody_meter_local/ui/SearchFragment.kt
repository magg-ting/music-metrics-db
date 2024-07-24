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
    private val recentSearches = mutableListOf<String>()
    private val searchResults = mutableListOf<Song>()

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var userDbReference: DatabaseReference

    @Inject
    lateinit var songDbReference: DatabaseReference

    @Inject
    @PopularSearchesDatabaseReference
    lateinit var popularSearchesDbReference: DatabaseReference

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

        // set up recent search view
        recentSearchesAdapter = RecentSearchesAdapter(recentSearches) {searchItem ->
            recentSearches.remove(searchItem)
            recentSearchesAdapter.notifyDataSetChanged()
        }

        binding.recentSearchesRecyclerView.apply{
            layoutManager = LinearLayoutManager(context)
            adapter = recentSearchesAdapter
        }

        // set up search results view
        searchResultsAdapter = SearchResultsAdapter(searchResults){song ->
            val action = SearchFragmentDirections.actionSearchFragmentToSongDetailFragment(song)
            findNavController().navigate(action)
        }

        binding.searchResultsRecyclerView.apply{
            layoutManager = LinearLayoutManager(context)
            adapter = searchResultsAdapter
        }

        // show recent searches group and hide search results group by default
        binding.recentSearchesGroup.visibility = View.VISIBLE
        binding.searchResultsGroup.visibility = View.GONE

        // observe login state
        loginViewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            updateUIForLoginState(isLoggedIn)
        }

        // handle SearchView results
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val trimmedQuery = it.trim()
                    if (trimmedQuery.isNotEmpty()) {
                        saveSearchQuery(trimmedQuery)
                        searchViewModel.performSearch(trimmedQuery)
                        binding.recentSearchesGroup.visibility = View.GONE
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        // observe search results changes
        searchViewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchResults.clear()
            searchResults.addAll(results)
            searchResultsAdapter.notifyDataSetChanged()
            binding.recentSearchesGroup.visibility = View.GONE
            binding.searchResultsGroup.visibility = View.VISIBLE
        }
    }

    // TODO: search results not hidden when navigated back to search fragment
    override fun onResume() {
        super.onResume()
        // show recent searches group and hide search results group by default
        binding.recentSearchesGroup.visibility = View.VISIBLE
        binding.searchResultsGroup.visibility = View.GONE

        val isLoggedIn = loginViewModel.isLoggedIn.value ?: false
        updateUIForLoginState(isLoggedIn)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUIForLoginState(isLoggedIn: Boolean) {
        if (isLoggedIn) {
            // show recent searches of the user
            binding.recentSearchesRecyclerView.visibility = View.VISIBLE
            binding.clearAllSearches.visibility = View.VISIBLE
            binding.loginPromptWrapper.visibility = View.GONE
//            TODO: onclick listener not working
            binding.clearAllSearches.setOnClickListener {
                recentSearches.clear()
                recentSearchesAdapter.notifyDataSetChanged()
            }
            loadRecentSearches()
        } else {
            // prompt user to login to retrieve his recent searches
            binding.recentSearchesRecyclerView.visibility = View.GONE
            binding.clearAllSearches.visibility = View.GONE
            binding.loginPromptWrapper.visibility = View.VISIBLE
//                TODO: onclick listener not working
            Log.d("SearchFragment", "Login Prompt shown")
            binding.btnLogin.isClickable = true
            binding.btnLogin.setOnClickListener {
                Log.d("SearchFragment", "Login dialog clicked")
                val loginDialogFragment = LoginDialogFragment()
                loginDialogFragment.show(parentFragmentManager, "loginDialogFragment")
            }
            Log.d("SearchFragment", "Other code")
        }
    }

    private fun saveSearchQuery(query: String) {
        // update the user db for recent searches
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val user = userDbReference.child(uid)
            user.child("recentSearches")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val recentSearches = dataSnapshot.getValue(object :
                            GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()
                        if (recentSearches.contains(query)) {
                            recentSearches.remove(query)
                        }
                        recentSearches.add(0, query) // Add new search at the beginning
                        if (recentSearches.size > 10) {
                            recentSearches.removeAt(recentSearches.size - 1) // Remove oldest if more than 10
                        }
                        user.child("recentSearches").setValue(recentSearches)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(ContentValues.TAG, "Load Recent Searches: onCancelled", databaseError.toException())
                    }
                })
        }

        // update the popular searches db for search count
        popularSearchesDbReference.child(query)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val currentCount = dataSnapshot.child("count").getValue(Long::class.java) ?: 0
                    popularSearchesDbReference.child(query).child("count").setValue(currentCount + 1)
                    popularSearchesDbReference.child(query).child("searchString").setValue(query)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(ContentValues.TAG, "Update Popular Searches: onCancelled", databaseError.toException())
                }
            })
    }

    private fun loadRecentSearches() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val user = userDbReference.child(uid)
            user.child("recentSearches")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val recentSearches = dataSnapshot.getValue(object :
                            GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()
                        this@SearchFragment.recentSearches.clear()
                        this@SearchFragment.recentSearches.addAll(recentSearches)
                        recentSearchesAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(ContentValues.TAG, "Load Recent Searches: onCancelled", databaseError.toException())
                    }
                })
        }
    }
}