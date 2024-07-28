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

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> get() = _updateSuccess

    private var isImageUploadSuccessful = false
    private var isProfileUpdateSuccessful = false

    private var username: String? = null
    private var profileUrl: String? = null


    fun loadUserProfile() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val userRef = userDbReference.child(uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        username = user.username
                        profileUrl = user.profileUrl
                        _user.value = user
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileViewModel", "Cannot load user data ${error.message}")
                }
            })
        }
    }

    fun checkIfChanged(newUsername: String) {
        _hasChanges.value = (newUsername != username) || (profileUrl != _user.value?.profileUrl)
    }

    fun setUserProfileImageUrl(url: String) {
        profileUrl = url
        _user.value?.profileUrl = url
        _hasChanges.value = true
    }

    //TODO: image uploaded to storage but profile url not updated to database
    fun saveChanges() {
        val uid = auth.currentUser?.uid ?: return
        val user = _user.value ?: return

        val updates = mutableMapOf<String, Any>()

        // Only add fields to updates map if they have changed
        if (user.username != username) {
            updates["username"] = user.username
        }

        if (user.profileUrl != profileUrl) {
            updates["profileUrl"] = user.profileUrl ?: ""
        }

        // If there's nothing to update, exit early
        if (updates.isEmpty()) {
            _updateSuccess.value = false
            return
        }

        userDbReference.child(uid).updateChildren(updates)
            .addOnCompleteListener { task ->
                isProfileUpdateSuccessful = task.isSuccessful
                checkOverallSuccess()

//                if (task.isSuccessful) {
//                    username = user.username
//                    profileUrl = user.profileUrl
//                    _hasChanges.value = false
//                    _updateSuccess.value = true
//                } else {
//                    _updateSuccess.value = false
//                }
            }
    }

    fun uploadProfileImage(
        file: File,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val imageUri = Uri.fromFile(file)
        val imageExtension = getFileExtension(imageUri) // Get file extension from URI
        val imageRef =
            storageReference.child("profile_images/${UUID.randomUUID()}.${imageExtension}")
        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                setUserProfileImageUrl(imageUrl)
                saveChanges() // Update the profile URL in the database
                onSuccess(imageUrl)
//                _updateSuccess.value = true
                isImageUploadSuccessful = true
                checkOverallSuccess()
            }.addOnFailureListener { exception ->
                onFailure(exception)
                isImageUploadSuccessful = false
                checkOverallSuccess()
//                _updateSuccess.value = false
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
            isImageUploadSuccessful = false
            checkOverallSuccess()
//            _updateSuccess.value = false
        }
    }

    private fun checkOverallSuccess() {
        _updateSuccess.value = isImageUploadSuccessful && isProfileUpdateSuccessful
    }

    private fun getFileExtension(uri: Uri): String {
        // Extract file extension from the URI
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(uri.toString()) ?: "jpg"
    }

}