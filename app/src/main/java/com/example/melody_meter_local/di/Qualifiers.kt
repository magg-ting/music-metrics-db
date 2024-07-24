package com.example.melody_meter_local.di

import javax.inject.Qualifier


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserDatabaseReference

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SongDatabaseReference

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PopularSearchesDatabaseReference