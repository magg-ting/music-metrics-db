package com.example.melody_meter_local.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.melody_meter_local.di.UserDatabaseReference
import com.example.melody_meter_local.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val auth: FirebaseAuth,
    @UserDatabaseReference private val userDbReference: DatabaseReference
) : AndroidViewModel(application) {

    // livedata for whether login is successful
    private val _loginStatus = MutableLiveData<Result<Boolean>>()
    val loginStatus: LiveData<Result<Boolean>> get() = _loginStatus

    // livedata for whether signup is successful
    private val _signUpStatus = MutableLiveData<Result<Boolean>>()
    val signUpStatus: LiveData<Result<Boolean>> get() = _signUpStatus

    // livedata for whether user is logged in
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val sharedPreferences =
        application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    init {
        // Load the persisted login state
        _isLoggedIn.value = sharedPreferences.getBoolean(LOGIN_KEY, false)
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginStatus.value = Result.success(true)
                    _isLoggedIn.value = true
                    persistLoginState(true)
                } else {
                    _loginStatus.value = Result.failure(task.exception ?: Exception("Unknown error"))
                }
            }
    }

    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
        persistLoginState(false)
        // Reset login/signup status to prevent incorrect status being shown
        _loginStatus.value = Result.success(false)
        _signUpStatus.value = Result.success(false)
    }

    fun signup(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // get the user id and add user to database
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val user = User(username = username)
                        userDbReference.child(uid).setValue(user)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    _signUpStatus.value = Result.success(true)
                                    _isLoggedIn.value = true
                                    persistLoginState(true)
                                } else {
                                    _signUpStatus.value = Result.failure(task.exception ?: Exception("Unknown error"))
                                }
                            }
                    } else {
                        _signUpStatus.value = Result.failure(task.exception ?: Exception("Unknown error"))
                    }
                } else {
                    _signUpStatus.value = Result.failure(task.exception ?: Exception("Unknown error"))
                }
            }
    }

    private fun persistLoginState(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(LOGIN_KEY, isLoggedIn).apply()
    }

    companion object {
        private const val PREF_NAME = "LoginPreferences"
        private const val LOGIN_KEY = "is_logged_in"
    }
}
