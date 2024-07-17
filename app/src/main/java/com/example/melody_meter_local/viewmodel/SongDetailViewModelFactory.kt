package com.example.melody_meter_local.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.auth.FirebaseAuth

class SongDetailViewModelFactory (private val auth: FirebaseAuth): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        if (modelClass.isAssignableFrom(SongDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SongDetailViewModel(auth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}