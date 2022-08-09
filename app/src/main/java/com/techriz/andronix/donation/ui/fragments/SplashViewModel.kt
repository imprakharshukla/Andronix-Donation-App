package com.techriz.andronix.donation.ui.fragments;

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techriz.andronix.donation.repository.SettingsRepository
import com.techriz.andronix.donation.utils.Constants.DARK_MODE
import com.techriz.andronix.donation.utils.Constants.DEVICE_MODE
import com.techriz.andronix.donation.utils.Constants.LIGHT_MODE

import javax.inject.Inject

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@HiltViewModel
class SplashViewModel @Inject
constructor(val settingsRepository: SettingsRepository) : ViewModel() {

    suspend fun applyTheme() {
        when (settingsRepository.getTheme()) {
            LIGHT_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            DARK_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            DEVICE_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }


}
