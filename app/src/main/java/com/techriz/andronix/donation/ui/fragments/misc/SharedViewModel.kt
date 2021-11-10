package com.techriz.andronix.donation.ui.fragments.misc

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import androidx.lifecycle.ViewModel
import com.techriz.andronix.donation.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltViewModel
class SharedViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    suspend fun getCurrentTheme(): String {
        return settingsRepository.getTheme()
    }

    val themeLive get() = settingsRepository.getThemeLive()
}
