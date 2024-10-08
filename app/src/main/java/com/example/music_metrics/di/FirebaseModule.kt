package com.example.music_metrics.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    @UserDatabaseReference
    fun provideUserDatabaseReference(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference("Users")
    }

    @Provides
    @Singleton
    @SongDatabaseReference
    fun provideSongDatabaseReference(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference("Songs")
    }


    @Provides
    @Singleton
    @PopularSearchesDatabaseReference
    fun providePopularSearchesDatabaseReference(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference("PopularSearches")
    }

    @Provides
    @Singleton
    fun provideStorageReference(): StorageReference {
        return FirebaseStorage.getInstance().reference
    }
}