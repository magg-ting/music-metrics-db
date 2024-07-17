package com.example.melody_meter_local.utils

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.melody_meter_local.viewmodel.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class ImagePickerHelper(private val fragment: Fragment, private val profileViewModel: ProfileViewModel) {

    private var imageUri: Uri? = null

    private val pickImageGallery = fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadProfileCoverPhoto(it) }
    }

    private val pickImageCamera = fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let { uploadProfileCoverPhoto(it) }
        }
    }

    private val requestPermissionLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            pickFromCamera()
        }
    }

    private val requestMultiplePermissionsLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
            pickFromCamera()
        } else if (permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
            pickFromGallery()
        }
    }

    fun showImagePickDialog() {
        val options = arrayOf("Camera", "Gallery")
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle("Pick Image From")
            .setItems(options) { dialog, which ->
                if (which == 0) {
                    if (checkCameraPermission()) {
                        pickFromCamera()
                    } else {
                        requestCameraPermission()
                    }
                } else if (which == 1) {
                    if (checkStoragePermission()) {
                        pickFromGallery()
                    } else {
                        requestStoragePermission()
                    }
                }
            }
            .show()
    }

    private fun pickFromCamera() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp Image Title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Image Description")
        imageUri = fragment.requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        pickImageCamera.launch(imageUri)
    }

    private fun pickFromGallery() {
        pickImageGallery.launch("image/*")
    }

    private fun checkCameraPermission(): Boolean {
        val context = fragment.requireContext()
        val cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return cameraPermission == PackageManager.PERMISSION_GRANTED && storagePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        requestMultiplePermissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    private fun checkStoragePermission(): Boolean {
        val context = fragment.requireContext()
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun uploadProfileCoverPhoto(uri: Uri) {
        val progressBar = ProgressBar(fragment.requireContext())
        progressBar.isIndeterminate = true

        val dialog = MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle("Updating Profile Picture")
            .setView(progressBar)
            .setCancelable(false)
            .show()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val filePathAndName = "users_profile_image/$uid"
        FirebaseStorage.getInstance().reference.child(filePathAndName).putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    profileViewModel.setUserProfileImageUrl(downloadUri.toString())
                    dialog.dismiss()
                }
            }
            .addOnFailureListener { e ->
                dialog.dismiss()
            }
    }
}

