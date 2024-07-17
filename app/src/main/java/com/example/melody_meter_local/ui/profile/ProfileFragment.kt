package com.example.melody_meter_local.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.melody_meter_local.ui.LoginDialogFragment
import com.example.melody_meter_local.viewmodel.LoginViewModel
import com.example.melody_meter_local.utils.ImagePickerHelper
import com.example.melody_meter_local.viewmodel.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    private lateinit var imagePickerHelper: ImagePickerHelper


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
        imagePickerHelper = ImagePickerHelper(this, profileViewModel)

        // TODO: set button save to visible when either profile pic or username is changed
        binding.btnSaveChanges.visibility = View.GONE

        loginViewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (isLoggedIn) {
                // Update the profile view to show logged-in state
                binding.loggedInGroup.visibility = View.VISIBLE
                binding.notLoggedInGroup.visibility = View.GONE
                profileViewModel.loadUserProfile()
            } else {
                // Show login prompt
                binding.loggedInGroup.visibility = View.GONE
                binding.notLoggedInGroup.visibility = View.VISIBLE
                binding.btnLogin.setOnClickListener {
                    val loginDialogFragment = LoginDialogFragment()
                    loginDialogFragment.show(parentFragmentManager, "loginDialogFragment")
                }
            }
        }

        profileViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                setLoggedInView(user)
            }
        }

        profileViewModel.hasChanges.observe(viewLifecycleOwner) { hasChanges ->
            binding.btnSaveChanges.visibility = if (hasChanges) View.VISIBLE else View.GONE
        }

        binding.btnSaveChanges.setOnClickListener { profileViewModel.saveChanges() }
        binding.btnChangeImage.setOnClickListener { imagePickerHelper.showImagePickDialog() }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLogoutConfirmDialog() {
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

    private fun setLoggedInView(user: User) {
        binding.editTextUsername.setText(user.username)
        if (!user.profileUrl.isNullOrEmpty()) {
            // Load image into binding.albumImage using Glide
            Glide.with(binding.root)
                .load(user.profileUrl)
                .into(binding.imageViewProfile)
        } else {
            binding.imageViewProfile.setImageResource(R.drawable.user_avatar)
        }

        binding.editTextUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                profileViewModel.checkIfChanged(binding.editTextUsername.text.toString().trim())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.textViewFavorite.setOnClickListener {
            findNavController()
                .navigate(ProfileFragmentDirections.actionProfileFragmentToFavoritesFragment(user))
        }
        binding.textViewChangePassword.setOnClickListener {
            findNavController()
                .navigate(
                    ProfileFragmentDirections.actionProfileFragmentToChangePasswordFragment(
                        user
                    )
                )
        }
        binding.textViewRatingHistory.setOnClickListener {
            findNavController()
                .navigate(
                    ProfileFragmentDirections.actionProfileFragmentToRatingHistoryFragment(
                        user
                    )
                )
        }
        binding.textViewLogout.setOnClickListener { showLogoutConfirmDialog() }
//        binding.btnChangeImage.setOnClickListener { changeProfileImage() }

    }


    //    override fun onRestoreInstanceState(savedInstanceState: Bundle){
//        super.onRestoreInstanceState(savedInstanceState)
//    }
}