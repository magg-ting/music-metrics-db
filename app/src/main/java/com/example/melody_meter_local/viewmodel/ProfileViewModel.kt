package com.example.melody_meter_local.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.melody_meter_local.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileViewModel: ViewModel()  {
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _hasChanges = MutableLiveData<Boolean>()
    val hasChanges: LiveData<Boolean> get() = _hasChanges

    private lateinit var auth: FirebaseAuth
    private lateinit var userDbReference: DatabaseReference

    private var username: String? = null
    private var profileUrl: String? = null

    init {
        auth = FirebaseAuth.getInstance()
        userDbReference = FirebaseDatabase.getInstance().getReference("Users")
    }

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

                }
            })
        }
    }

    fun checkIfChanged(newUsername: String) {
        if (newUsername != username) {
            _hasChanges.value = true
        } else {
            _hasChanges.value = false
        }
    }

    fun saveChanges() {
        val uid = auth.currentUser?.uid ?: return
        val newUsername = _user.value?.username ?: return
        val newProfileUrl = _user.value?.profileUrl ?: return

        val updates = hashMapOf<String, Any>(
            "username" to newUsername,
            "profileUrl" to newProfileUrl
        )

        userDbReference.child(uid).updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    username = newUsername
                    profileUrl = newProfileUrl
                    _hasChanges.value = false
                }
            }
    }

    fun setUserProfileImageUrl(url: String) {
        profileUrl = url
        _user.value?.profileUrl = url
        _hasChanges.value = true
    }
}