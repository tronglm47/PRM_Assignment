package com.example.prm_assignment.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_preferences")

class TokenManager(private val context: Context) {
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")

    suspend fun saveTokens(accessToken: String, refreshToken: String?) {
        context.dataStore.edit { preferences ->
            preferences[accessTokenKey] = accessToken
            if (refreshToken != null) {
                preferences[refreshTokenKey] = refreshToken
            }
        }
    }

    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
        }
    }

    suspend fun getAccessToken(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[accessTokenKey]
        }.first()
    }
}
