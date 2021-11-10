package com.techriz.andronix.donation.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.techriz.andronix.donation.utils.Constants.DARK_MODE
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    @ApplicationContext val context: Context
) {
    private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val Context.miscDataStore: DataStore<Preferences> by preferencesDataStore(name = "misc")


    /*Settings*/
    val THEME_KEY = stringPreferencesKey("theme")

    suspend fun setTheme(theme: String) {
        println("SetTheme $theme")
        context.settingsDataStore.setValue(THEME_KEY, theme)
    }


    suspend fun getTheme(): String {
        val theme = context.settingsDataStore.getValueAsFlow(THEME_KEY, DARK_MODE).first()
        println("GetTheme $theme")
        return theme
    }

    fun getThemeLive(): Flow<String> {
        return context.settingsDataStore.getValueAsFlow(THEME_KEY, DARK_MODE)
    }

}

suspend fun <T> DataStore<Preferences>.setValue(
    key: Preferences.Key<T>,
    value: T
) {
    this.edit {
        it[key] = value
    }
}

fun <T> DataStore<Preferences>.getValueAsFlow(
    key: Preferences.Key<T>,
    defaultValue: T
): Flow<T> {
    return this.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { it[key] ?: defaultValue }
}

suspend fun DataStore<Preferences>.clearPref() {
    this.edit { it.clear() }
}

