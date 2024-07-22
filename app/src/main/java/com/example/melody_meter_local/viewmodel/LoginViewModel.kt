package com.example.melody_meter_local.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val sharedPreferences = application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    init {
        // Load the persisted login state
        _isLoggedIn.value = sharedPreferences.getBoolean(LOGIN_KEY, false)
    }

    fun login() {
        _isLoggedIn.value = true
        persistLoginState(true)
    }

    fun logout() {
        _isLoggedIn.value = false
        persistLoginState(false)
    }

    private fun persistLoginState(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(LOGIN_KEY, isLoggedIn).apply()
    }

    companion object {
        private const val PREF_NAME = "LoginPreferences"
        private const val LOGIN_KEY = "is_logged_in"
    }
}
