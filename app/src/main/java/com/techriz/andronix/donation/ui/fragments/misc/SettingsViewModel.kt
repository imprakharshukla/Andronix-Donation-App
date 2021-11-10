package com.techriz.andronix.donation.ui.fragments.misc


import androidx.lifecycle.ViewModel
import com.techriz.andronix.donation.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class SettingsViewModel
@Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    suspend fun getCurrentTheme() = settingsRepository.getTheme()
    suspend fun setCurrentTheme(theme: String) = settingsRepository.setTheme(theme)
}