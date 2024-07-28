package com.example.melody_meter_local.ui.profile

import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.melody_meter_local.R
import com.example.melody_meter_local.databinding.FragmentProfileBinding
import com.example.melody_meter_local.model.User
import com.example.melody_meter_local.ui.LoginDialogFragment
import com.example.melody_meter_local.viewmodel.LoginViewModel
import com.example.melody_meter_local.viewmodel.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shz.imagepicker.imagepicker.ImagePicker
import com.shz.imagepicker.imagepicker.ImagePickerCallback
import com.shz.imagepicker.imagepicker.model.GalleryPicker
import com.shz.imagepicker.imagepicker.model.PickedResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

@AndroidEntryPoint
class ProfileFragment : Fragment(), ImagePickerCallback {

    private var _binding: FragmentProfileBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    private lateinit var imagePicker: ImagePicker

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

        imagePicker = ImagePicker.Builder(requireContext().packageName + ".provider", this)
            .useGallery(true)                           // Use gallery picker if true
            .useCamera(true)                            // Use camera picker if true
            .autoRotate(true)                           // Returns 0 degrees rotated images, instead of exif-rotated images if true
            .multipleSelection(false)                   // Allow multiple selection in gallery picker
            .galleryPicker(GalleryPicker.NATIVE)        // Available values: GalleryPicker.NATIVE, GalleryPicker.CUSTOM
            .build()

        binding.btnSaveChanges.isEnabled = false

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
            binding.btnSaveChanges.isEnabled = hasChanges
        }

        //TODO: toast should not be shown unless user actually has made changes
        profileViewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (profileViewModel.hasChanges.value == true) {
                val message = if (success) {
                    "Profile updated successfully!"
                } else {
                    "Failed to update profile."
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        //TODO: if user navigates back without saving, we should show a dialog to confirm discard changes

        // Add a listener to handle navigation changes
        findNavController().addOnDestinationChangedListener { _, destination, _ ->
            Log.d("ProfileFragment", "Destination Changed")
            if (profileViewModel.hasChanges.value == true) {
                //showDiscardChangesDialog(destination.id)
                lifecycleScope.launch {
                    val shouldProceed = showDiscardChangesDialog()
                    if (shouldProceed) {
                        Log.d("ProfileFragment", "onDestinationChanged proceed to ${destination.id}")
                            findNavController().navigate(destination.id)
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (profileViewModel.hasChanges.value == true) {
                        lifecycleScope.launch {
                            val shouldProceed = showDiscardChangesDialog()
                            if (shouldProceed) {
                                Log.d("ProfileFragment", "onBackPressed proceed")
                                findNavController().navigateUp()
                            }
                        }
                    } else {
                        findNavController().navigateUp()
                    }
                }
            }
        )
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
        _binding = null
    }

    override fun onImagePickerResult(result: PickedResult) {
        when (result) {
            PickedResult.Empty -> {
                // No file was selected, noting to do
            }

            is PickedResult.Error -> {
                val throwable = result.throwable
                // Some error happened, handle this throwable
            }

            is PickedResult.Multiple -> {
                val pickedImages = result.images
                val files = pickedImages.map { it.file }
                // Selected multiple images, do whatever you want with files
            }

            //TODO: uploaded image not persists when orientation changes
            is PickedResult.Single -> {
                val pickedImage = result.image
                val file = pickedImage.file
                if (file != null && file.length() <= 2 * 1024 * 1024) { // Check if file size <= 2MB
                    Glide.with(this)
                        .load(file)
                        .into(binding.imageViewProfile)
                    binding.btnSaveChanges.isEnabled = true

                    // Upload the image
                    profileViewModel.uploadProfileImage(file,
                        onSuccess = { imageUrl ->
                            // Handle success if needed
                        },
                        onFailure = { exception ->
                            Toast.makeText(
                                context,
                                "Error uploading image: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Image size should be under 2MB", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
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

        binding.btnChangeImage.setOnClickListener {
            context?.let { it1 -> imagePicker.launch(it1) }
        }

        binding.btnSaveChanges.setOnClickListener {
            val imageUri = profileViewModel.user.value?.profileUrl
            if (imageUri != null) {
                // If an image URL is set, just save the changes
                profileViewModel.saveChanges()
            } else {
                // No image URL is set, we need to upload the new image first
                binding.imageViewProfile.drawable?.let { drawable ->
                    val bitmap = (drawable as BitmapDrawable).bitmap
                    val file = File(requireContext().cacheDir, "profile_image.jpg")
                    FileOutputStream(file).use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }

                    profileViewModel.uploadProfileImage(file,
                        onSuccess = { imageUrl ->
                            // Success message is handled by ViewModel
                        },
                        onFailure = { exception ->
                            Toast.makeText(
                                requireContext(),
                                "Image upload failed: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
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
