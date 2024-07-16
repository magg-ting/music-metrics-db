package com.example.melody_meter_local.ui.profile

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.melody_meter_local.R
import com.example.melody_meter_local.databinding.FragmentProfileBinding
import com.example.melody_meter_local.model.User
import com.example.melody_meter_local.ui.login.LoginDialogFragment
import com.example.melody_meter_local.ui.login.LoginViewModel
import com.example.melody_meter_local.utils.ImagePickerHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    private lateinit var imagePickerHelper: ImagePickerHelper
//
//    private lateinit var auth: FirebaseAuth
//    private lateinit var userDbReference: DatabaseReference
//    private lateinit var songDbReference: DatabaseReference
//    private lateinit var storageReference: StorageReference
//
//    private var username: String? = null
//    private var profileUrl: String? = null
//    private var hasChanges = false
//
//    private val CAMERA_REQUEST = 100
//    private val STORAGE_REQUEST = 200
//    private val IMAGE_PICK_GALLERY_REQUEST = 300
//    private val IMAGE_PICK_CAMERA_REQUEST = 400
//    private var profileOrCoverPhoto: String = ""
//    private val storagePath: String = "users_profile_image/"
//    lateinit var cameraPermission: Array<String>
//    lateinit var storagePermission: Array<String>



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
//        auth = FirebaseAuth.getInstance()
//        userDbReference = FirebaseDatabase.getInstance().getReference("Users")
//        songDbReference = FirebaseDatabase.getInstance().getReference("Songs")
//        cameraPermission =
//            arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        storagePermission = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        // TODO: set button save to visible when either profile pic or username is changed
        binding.btnSaveChanges.visibility = View.GONE

        loginViewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (isLoggedIn) {
                // Update the profile view to show logged-in state
                binding.loggedInGroup.visibility = View.VISIBLE
                binding.notLoggedInGroup.visibility = View.GONE
                profileViewModel.loadUserProfile()

//                val uid = auth.currentUser?.uid
//                if(uid != null){
//                    val userRef = userDbReference.child(uid)
//
//                    // Get User object and use the values to update the UI
//                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                        override fun onDataChange(dataSnapshot: DataSnapshot) {
//                            val user = dataSnapshot.getValue(User::class.java)
//                            if (user != null) {
//                                username = user.username
//                                profileUrl = user.profileUrl
//                                setLoggedInView(user)
//                            }
//                        }
//                        override fun onCancelled(databaseError: DatabaseError) {
//                            // Getting User failed, log a message
//                            Log.w(TAG, "Load User: onCancelled", databaseError.toException())
//                        }
//                    })
//                }
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

//    private fun changeProfileImage(): Boolean {
//        TODO("Not yet implemented")
//
//    }

//    private fun checkIfChanged(){
//        val newUsername = binding.editTextUsername.text.toString().trim()
//        // TODO: update newProfileUrl logic
//        val newProfileUrl = profileUrl
//        if(newUsername != username || newProfileUrl != profileUrl){
//            hasChanges = true
//            binding.btnSaveChanges.visibility = View.VISIBLE
//        }
//    }

//    private fun saveChanges() {
//        val uid = auth.currentUser?.uid?: return
//        val newUsername = binding.editTextUsername.text.toString().trim()
//        // TODO: update newProfileUrl logic, newProfileurl can be null?
//        val newProfileUrl = profileUrl
//        if(newUsername.isNotEmpty() && newProfileUrl != null){
//            val updates = hashMapOf<String, Any>(
//                "username" to newUsername,
//                "profileUrl" to newProfileUrl
//            )
//
//            userDbReference.child(uid).updateChildren(updates)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
//                        username = newUsername
//                        profileUrl = newProfileUrl
//                        hasChanges = false
//                    } else {
//                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
//                    }
//                }
//        }
//    }

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
//        binding.btnSaveChanges.visibility = if(hasChanges) View.VISIBLE else View.GONE
//        binding.btnSaveChanges.setOnClickListener { saveChanges() }
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
//        binding.btnChangeImage.setOnClickListener { changeProfileImage() }

    }

//    private fun checkStoragePermission(): Boolean {
//        val result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//        return result
//    }
//
//    private fun requestStoragePermission() {
//        requestPermissions(storagePermission, STORAGE_REQUEST)
//    }
//
//    private fun checkCameraPermission(): Boolean {
//        val result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
//        val result1 = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//        return result && result1
//    }
//
//    private fun requestCameraPermission() {
//        requestPermissions(cameraPermission, CAMERA_REQUEST)
//    }
//
//    private fun showImagePickDialog() {
//        val options = arrayOf("Camera", "Gallery")
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setTitle("Pick Image From")
//        builder.setItems(options) { dialog, which ->
//            if (which == 0) {
//                if (!checkCameraPermission()) {
//                    requestCameraPermission()
//                } else {
//                    pickFromCamera()
//                }
//            } else if (which == 1) {
//                if (!checkStoragePermission()) {
//                    requestStoragePermission()
//                } else {
//                    pickFromGallery()
//                }
//            }
//        }
//        builder.create().show()
//    }
//
//    private fun pickFromCamera() {
//        val contentValues = ContentValues()
//        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image")
//        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")
//        imageUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
//        startActivityForResult(intent, IMAGE_PICK_CAMERA_REQUEST)
//    }
//
//    private fun pickFromGallery() {
//        val galleryIntent = Intent(Intent.ACTION_PICK)
//        galleryIntent.type = "image/*"
//        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_REQUEST)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == IMAGE_PICK_GALLERY_REQUEST) {
//                imageUri = data?.data
//                uploadProfileCoverPhoto(imageUri)
//            } else if (requestCode == IMAGE_PICK_CAMERA_REQUEST) {
//                uploadProfileCoverPhoto(imageUri)
//            }
//        }
//    }
//
//
//    private fun uploadProfileCoverPhoto(uri: Uri?) {
//        val pd = ProgressDialog(requireContext())
//        pd.setMessage("Updating Profile Picture")
//        pd.show()
//
//        val storagePath = "uers_profile_cover_iages/"
//        val profileOrCoverPhoto = "image"  // Assuming you have this as a field
//        val filePathAndName = "$storagePath$profileOrCoverPhoto_${FirebaseAuth.getInstance().uid}"
//        val storageReference = FirebaseStorage.getInstance().reference.child(filePathAndName)
//
//        storageReference.putFile(uri!!)
//            .addOnSuccessListener { taskSnapshot ->
//                val uriTask = taskSnapshot.storage.downloadUrl
//                while (!uriTask.isSuccessful);
//                val downloadUri = uriTask.result
//                if (uriTask.isSuccessful) {
//                    val hashMap = hashMapOf<String, Any>(profileOrCoverPhoto to downloadUri.toString())
//                    val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
//                    databaseReference.child(FirebaseAuth.getInstance().uid!!).updateChildren(hashMap)
//                        .addOnSuccessListener {
//                            pd.dismiss()
//                            Toast.makeText(requireContext(), "Updated", Toast.LENGTH_LONG).show()
//                        }
//                        .addOnFailureListener { e ->
//                            pd.dismiss()
//                            Toast.makeText(requireContext(), "Error Updating", Toast.LENGTH_LONG).show()
//                        }
//                } else {
//                    pd.dismiss()
//                    Toast.makeText(requireContext(), "Some error occurred", Toast.LENGTH_LONG).show()
//                }
//            }
//            .addOnFailureListener { e ->
//                pd.dismiss()
//                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
//            }
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

    }

//    override fun onRestoreInstanceState(savedInstanceState: Bundle){
//        super.onRestoreInstanceState(savedInstanceState)
//    }
}