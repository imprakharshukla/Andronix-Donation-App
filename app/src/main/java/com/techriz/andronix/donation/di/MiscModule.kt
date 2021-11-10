package com.techriz.andronix.donation.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import studio.com.techriz.andronix.ui.Loader

@Module
@InstallIn(FragmentComponent::class)
object MiscModule {
    @Provides
    @FragmentScoped
    fun providesLoader(@ActivityContext context: Context): Loader {
        return Loader(context)
    }
}