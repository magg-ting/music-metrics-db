package com.example.melody_meter_local.viewmodel

import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.melody_meter_local.di.UserDatabaseReference
import com.example.melody_meter_local.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    @UserDatabaseReference var userDbReference: DatabaseReference,
    private val storageReference: StorageReference
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _hasChanges = MutableLiveData<Boolean>()
    val hasChanges: LiveData<Boolean> get() = _hasChanges

    private val _updateSuccess = MutableLiveData<Boolean?>(null)
    val updateSuccess: LiveData<Boolean?> get() = _updateSuccess

    private var newUsername: String? = null
    private var newProfileUrl: String? = null

    // load user's existing info from db for comparison
    fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = userDbReference.child(uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    _user.value = user
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileViewModel", "Cannot load user data ${error.message}")
            }
        })

    }

    fun checkIfNameChanged(newUsername: String) {
        _hasChanges.value = (newUsername != _user.value?.username)
    }

    fun setUsername(name: String) {
        newUsername = name
        _hasChanges.value = true
    }

    fun setUserProfileImageUrl(url: String) {
        newProfileUrl = url
        _hasChanges.value = true
    }

    fun saveChanges() {
        val uid = auth.currentUser?.uid ?: return
        val currentUser = _user.value ?: return

        val updates = mutableMapOf<String, Any>()

        // Only add fields to updates map if they have changed
        if (currentUser.username != newUsername) {
            updates["username"] = newUsername ?: ""
        }

        if (currentUser.profileUrl != newProfileUrl) {
            updates["profileUrl"] = newProfileUrl ?: ""
        }

        // If there's nothing to update, exit early
        if (updates.isEmpty()) {
            _updateSuccess.value = false
            return
        }

        userDbReference.child(uid).updateChildren(updates)
            .addOnCompleteListener { task ->
                val profileUpdateSuccessful = task.isSuccessful
                // isProfileUpdateSuccessful = task.isSuccessful
                if (profileUpdateSuccessful) {
                    _hasChanges.value = false // Reset changes after successful update
                }
                Log.d("ProfileViewModel", "Database update task is successful: ${task.isSuccessful}")
                checkOverallSuccess(profileUpdateSuccessful)
            }
    }

    fun uploadProfileImage(
        file: File,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val imageUri = Uri.fromFile(file)
        val imageExtension = getFileExtension(imageUri) // Get file extension from URI
        val imageRef = storageReference.child("profile_images/${UUID.randomUUID()}.$imageExtension")
        val uploadTask = imageRef.putFile(imageUri)

        Log.d("ProfileViewModel", "Starting upload of image file: ${file.name}")

        uploadTask.addOnSuccessListener {
            Log.d("ProfileViewModel", "Image upload successful")
            //isImageUploadSuccessful = true
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                Log.d("ProfileViewModel", "Image URL obtained: $imageUrl")
                // Notify success with the image URL
                onSuccess(imageUrl)
            }.addOnFailureListener { exception ->
                Log.e("ProfileViewModel", "Failed to get download URL: ${exception.message}")
                // Notify failure with the exception
                onFailure(exception)
            }
        }.addOnFailureListener { exception ->
            //isImageUploadSuccessful = false
            Log.e("ProfileViewModel", "Image upload failed: ${exception.message}")
            // Notify failure with the exception
            onFailure(exception)
        }
    }

    private fun checkOverallSuccess(profileUpdateSuccessful: Boolean) {
        _updateSuccess.value = profileUpdateSuccessful && (newProfileUrl == null || newProfileUrl != null)
    }


    private fun getFileExtension(uri: Uri): String {
        // Extract file extension from the URI
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(uri.toString()) ?: "jpg"
    }

    fun resetUpdateSuccess() {
        savedStateHandle["updateSuccess"] = null
    }

    //TODO (future enhancement): remove outdated profile images from Storage
    //TODO: persists temp uploaded image and username update and dont show up

}