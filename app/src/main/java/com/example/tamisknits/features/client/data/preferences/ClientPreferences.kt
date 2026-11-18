package com.example.tamisknits.features.client.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Separate file from OnboardingPreferences' "app_preferences": two DataStores on one file crash at runtime.
private val Context.clientDataStore by preferencesDataStore(
    name = "client_preferences"
)

@Singleton
class ClientPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun recentlyViewedKey(uid: String) = stringPreferencesKey("recently_viewed_$uid")
    private fun avatarKey(uid: String) = stringPreferencesKey("avatar_$uid")

    fun recentlyViewed(uid: String): Flow<List<String>> =
        context.clientDataStore.data.map { preferences ->
            preferences[recentlyViewedKey(uid)]
                ?.split(SEPARATOR)
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        }

    suspend fun addRecentlyViewed(uid: String, productId: String) {
        context.clientDataStore.edit { preferences ->
            val current = preferences[recentlyViewedKey(uid)]
                ?.split(SEPARATOR)
                ?.filter { it.isNotBlank() }
                ?: emptyList()
            val updated = (listOf(productId) + current.filter { it != productId })
                .take(MAX_RECENTLY_VIEWED)
            preferences[recentlyViewedKey(uid)] = updated.joinToString(SEPARATOR)
        }
    }

    fun avatarId(uid: String): Flow<String> =
        context.clientDataStore.data.map { preferences ->
            preferences[avatarKey(uid)] ?: ""
        }

    suspend fun setAvatarId(uid: String, avatarId: String) {
        context.clientDataStore.edit { preferences ->
            preferences[avatarKey(uid)] = avatarId
        }
    }

    suspend fun clear(uid: String) {
        context.clientDataStore.edit { preferences ->
            preferences.remove(recentlyViewedKey(uid))
            preferences.remove(avatarKey(uid))
        }
    }

    private companion object {
        const val SEPARATOR = ","
        const val MAX_RECENTLY_VIEWED = 10
    }
}
