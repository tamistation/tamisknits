package com.example.tamisknits.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(
    name = "app_preferences"
)

@Singleton
class OnboardingPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val onboardingCompletedKey =
        booleanPreferencesKey("onboarding_completed")

    val onboardingCompleted: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[onboardingCompletedKey] ?: false
        }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[onboardingCompletedKey] = true
        }
    }
}