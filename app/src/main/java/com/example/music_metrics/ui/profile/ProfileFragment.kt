package com.example.music_metrics.ui.profile

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.music_metrics.R
import com.example.music_metrics.databinding.FragmentProfileBinding
import com.example.music_metrics.model.User
import com.example.music_metrics.ui.LoginDialogFragment
import com.example.music_metrics.viewmodel.LoginViewModel
import com.example.music_metrics.viewmodel.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shz.imagepicker.imagepicker.ImagePicker
import com.shz.imagepicker.imagepicker.ImagePickerCallback
import com.shz.imagepicker.imagepicker.model.GalleryPicker
import com.shz.imagepicker.imagepicker.model.PickedResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

@AndroidEntryPoint
class ProfileFragment : Fragment(), ImagePickerCallback {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    private lateinit var imagePicker: ImagePicker
    private var tempImageFile: File? = null

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

        setupImagePicker()
        setupObservers()
        setupListeners()
    }

    private fun setupImagePicker() {
        imagePicker = ImagePicker.Builder(requireContext().packageName + ".provider", this)
            .useGallery(true)                           // Use gallery picker if true
            .useCamera(true)                            // Use camera picker if true
            .autoRotate(true)                           // Returns 0 degrees rotated images, instead of exif-rotated images if true
            .multipleSelection(false)                   // Allow multiple selection in gallery picker
            .galleryPicker(GalleryPicker.NATIVE)        // Available values: GalleryPicker.NATIVE, GalleryPicker.CUSTOM
            .build()
    }

    private fun setupObservers(){
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
                setupLoginButton()
            }
        }

        profileViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let { setLoggedInView(it) }
        }

        profileViewModel.hasChanges.observe(viewLifecycleOwner) { hasChanges ->
            binding.btnSaveChanges.isEnabled = hasChanges
        }

        profileViewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            success?.let {
                val message = if (it) "Profile updated successfully!" else "Failed to update profile."
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                // Disable the button only if the update was successful
                if (it) binding.btnSaveChanges.isEnabled = false
                // Reset the value to null after handling
                profileViewModel.resetUpdateSuccess()
            }
        }
    }

    private fun setupListeners(){
        binding.btnChangeImage.setOnClickListener {
            context?.let { ctx -> imagePicker.launch(ctx) }
        }

        binding.btnSaveChanges.setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            val loginDialogFragment = LoginDialogFragment()
            loginDialogFragment.show(parentFragmentManager, "loginDialogFragment")
        }
    }

    private fun setLoggedInView(user: User) {
        binding.editTextUsername.setText(user.username)
        loadProfileImage(user.profileUrl)
        setupUsernameListener()
        setupNavigationListeners(user)
    }

    private fun loadProfileImage(profileUrl: String?) {
        if (!profileUrl.isNullOrEmpty()) {
            // Load image into binding.albumImage using Glide
            Glide.with(binding.root)
                .load(profileUrl)
                .into(binding.imageViewProfile)
        } else {
            binding.imageViewProfile.setImageResource(R.drawable.user_avatar)
        }
    }

    private fun setupUsernameListener() {
        binding.editTextUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val newUsername = s.toString().trim()
                profileViewModel.checkIfNameChanged(newUsername)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupNavigationListeners(user: User) {
        binding.textViewFavorite.setOnClickListener {
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
    }

    private fun saveProfileChanges() {
        val newUsername = binding.editTextUsername.text.toString().trim()

        if (newUsername.isEmpty() || newUsername.contains(Regex("\\s"))) {
            Toast.makeText(requireContext(), getString(R.string.invalid_username), Toast.LENGTH_SHORT).show()
            return
        }

        profileViewModel.setUsername(newUsername)

        if (tempImageFile != null) {
            uploadProfileImage()
        } else {
            profileViewModel.saveChanges()
        }
    }

    private fun uploadProfileImage() {
        tempImageFile?.let { file ->
            profileViewModel.uploadProfileImage(file,
                onSuccess = { imageUrl ->
                    profileViewModel.setUserProfileImageUrl(imageUrl)
                    profileViewModel.saveChanges()
                    tempImageFile = null  //clear the temp image upon saving
                },
                onFailure = { exception ->
                    Log.e("ProfileFragment", "Image upload failed: ${exception.message}")
                    Toast.makeText(requireContext(), "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun showLogoutConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.confirm_logout))
            .setPositiveButton(getString(R.string.logout)) { _, _ ->
                loginViewModel.logout()
            }.setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    override fun onImagePickerResult(result: PickedResult) {
        when (result) {
            is PickedResult.Single -> {
                val file = result.image.file
                if (file != null && file.length() <= 2 * 1024 * 1024) { // Check if file size <= 2MB
                    Glide.with(this)
                        .load(file)
                        .into(binding.imageViewProfile)
                    tempImageFile = file // Save the temporary image file
                    binding.btnSaveChanges.isEnabled = true
                } else {
                    Toast.makeText(context, getString(R.string.image_too_big), Toast.LENGTH_SHORT).show()
                }
            }
            else -> { /* Handle other cases if necessary */ }
        }
    }


    private suspend fun showDiscardChangesDialog(): Boolean {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.discard_changes))
            .setMessage(getString(R.string.confirm_discard_changes))
            .create()
            .await(positiveText = getString(R.string.discard), negativeText = getString(R.string.cancel))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("ProfileFragment", "Destroy View")
        _binding = null
    }

}

// ref: https://federicokt.medium.com/smarter-dialogs-with-coroutines-b83e1d0e06a0
private suspend fun AlertDialog.await(
    positiveText: String,
    negativeText: String
) = suspendCancellableCoroutine { cont ->
    val listener = DialogInterface.OnClickListener { _, which ->
        cont.resume(which == DialogInterface.BUTTON_POSITIVE)
    }

    setButton(AlertDialog.BUTTON_POSITIVE, positiveText, listener)
    setButton(AlertDialog.BUTTON_NEGATIVE, negativeText, listener)

    // we can either decide to cancel the coroutine if the dialog
    // itself gets cancelled, or resume the coroutine with the
    // value [false]
    setOnCancelListener { cont.cancel() }

    // if we make this coroutine cancellable, we should also close the
    // dialog when the coroutine is cancelled
    cont.invokeOnCancellation { dismiss() }

    // remember to show the dialog before returning from the block,
    // you won't be able to do it after this function is called!
    show()
}


//TODO (future enhancement): refactor to move imagePicker and discardDialog to utils
//TODO (future enhancement): if user navigates back without saving, we should show a dialog to confirm discard changes

// Add a listener to handle navigation changes
//        findNavController().addOnDestinationChangedListener { _, destination, _ ->
//            Log.d("ProfileFragment", "Destination Changed")
//            if (profileViewModel.hasChanges.value == true) {
//                //showDiscardChangesDialog(destination.id)
//                lifecycleScope.launch {
//                    val shouldProceed = showDiscardChangesDialog()
//                    if (shouldProceed) {
//                        Log.d("ProfileFragment", "onDestinationChanged proceed to ${destination.id}")
//                            findNavController().navigate(destination.id)
//                    }
//                }
//            }
//        }
//
//        requireActivity().onBackPressedDispatcher.addCallback(
//            viewLifecycleOwner,
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    if (profileViewModel.hasChanges.value == true) {
//                        lifecycleScope.launch {
//                            val shouldProceed = showDiscardChangesDialog()
//                            if (shouldProceed) {
//                                Log.d("ProfileFragment", "onBackPressed proceed")
//                                findNavController().navigateUp()
//                            }
//                        }
//                    } else {
//                        findNavController().navigateUp()
//                    }
//                }
//            }
//        )