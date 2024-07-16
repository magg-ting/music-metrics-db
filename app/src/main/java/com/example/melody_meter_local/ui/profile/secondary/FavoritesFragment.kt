package com.example.melody_meter_local.ui.profile.secondary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.melody_meter_local.databinding.FragmentFavoritesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val args: FavoritesFragmentArgs by navArgs()
    private val user by lazy { args.user }

    private lateinit var auth: FirebaseAuth
    private lateinit var userDbReference: DatabaseReference

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
        auth = FirebaseAuth.getInstance()
        userDbReference = FirebaseDatabase.getInstance().getReference("Users")

        showFavorites()
        //toggleFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showFavorites() {
        user.let {
            if(!it.favorites.isNullOrEmpty()){
                binding.noFavoritesMsg.visibility = View.GONE
                binding.favoritesRecyclerView.visibility = View.VISIBLE
            } else {
                binding.noFavoritesMsg.visibility = View.VISIBLE
                binding.favoritesRecyclerView.visibility = View.GONE
            }
        }
    }

//    private fun toggleFavorites() {
//        TODO("Not yet implemented")
//    }

}
