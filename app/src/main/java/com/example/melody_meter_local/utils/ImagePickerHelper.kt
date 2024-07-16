package com.example.melody_meter_local.utils

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.melody_meter_local.ui.profile.ProfileViewModel
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
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")
        imageUri = fragment.requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
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
        fragment.requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_REQUEST)
    }

    private fun checkStoragePermission(): Boolean {
        val context = fragment.requireContext()
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        fragment.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_REQUEST)
    }

    private fun uploadProfileCoverPhoto(uri: Uri) {
        val pd = ProgressDialog(fragment.requireContext())
        pd.setMessage("Updating Profile Picture")
        pd.show()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val filePathAndName = "users_profile_image/$uid"
        FirebaseStorage.getInstance().reference.child(filePathAndName).putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    profileViewModel.setUserProfileImageUrl(downloadUri.toString())
                    pd.dismiss()
                }
            }
            .addOnFailureListener { e ->
                pd.dismiss()
            }
    }

    companion object {
        private const val CAMERA_REQUEST = 100
        private const val STORAGE_REQUEST = 200
    }
}
