package com.example.melody_meter_local.ui.profile

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.melody_meter_local.R
import com.example.melody_meter_local.databinding.FragmentProfileBinding
import com.example.melody_meter_local.model.User
import com.example.melody_meter_local.ui.login.LoginDialogFragment
import com.example.melody_meter_local.ui.login.LoginViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels()

    private lateinit var auth: FirebaseAuth
    private lateinit var userDbReference: DatabaseReference
    private lateinit var songDbReference: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        userDbReference = FirebaseDatabase.getInstance().getReference("Users")
        songDbReference = FirebaseDatabase.getInstance().getReference("Songs")

        loginViewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (isLoggedIn) {
                // Update the profile view to show logged-in state
                binding.loggedInGroup.visibility = View.VISIBLE
                binding.notLoggedInGroup.visibility = View.GONE
                val uid = auth.currentUser?.uid
                if(uid != null){
                    val userRef = userDbReference.child(uid)

                    // Get User object and use the values to update the UI
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val user = dataSnapshot.getValue(User::class.java)
                            if (user != null) {
                                setLoggedInView(user)
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            // Getting User failed, log a message
                            Log.w(TAG, "Load User: onCancelled", databaseError.toException())
                        }
                    })
                }
            }
            else {
                // Show login prompt
                binding.loggedInGroup.visibility = View.GONE
                binding.notLoggedInGroup.visibility = View.VISIBLE
                binding.btnLogin.setOnClickListener {
                    val loginDialogFragment = LoginDialogFragment()
                    loginDialogFragment.show(parentFragmentManager, "loginDialogFragment")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLogoutConfirmDialog(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.confirm_logout))
            .setPositiveButton(getString(R.string.logout)) { _, _ ->
                loginViewModel.logout()
            }
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setLoggedInView(user: User){
        binding.editTextUsername.setText(user.username)
                    if(!user.profileUrl.isNullOrEmpty()){
                        // Load image into binding.albumImage using Glide
                        Glide.with(binding.root)
                            .load(user.profileUrl)
                            .into(binding.imageViewProfile)
                    } else {
                        binding.imageViewProfile.setImageResource(R.drawable.ic_profile)
                    }

        binding.textViewFavorite.setOnClickListener{
            findNavController()
                .navigate(ProfileFragmentDirections.actionProfileFragmentToFavoritesFragment(user))
        }
        binding.textViewChangePassword.setOnClickListener {
            findNavController()
                .navigate(ProfileFragmentDirections.actionProfileFragmentToChangePasswordFragment(user))
        }
        binding.textViewRatingHistory.setOnClickListener {
            findNavController()
                .navigate(ProfileFragmentDirections.actionProfileFragmentToRatingHistoryFragment(user))
        }
        binding.textViewLogout.setOnClickListener { showLogoutConfirmDialog() }
        binding.btnChangeImage.setOnClickListener {  }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

    }

//    override fun onRestoreInstanceState(savedInstanceState: Bundle){
//        super.onRestoreInstanceState(savedInstanceState)
//    }
}