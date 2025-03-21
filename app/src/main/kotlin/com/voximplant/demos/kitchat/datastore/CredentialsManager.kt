/*
 * Copyright (c) 2011 - 2025, Voximplant, Inc. All rights reserved.
 */

package com.voximplant.demos.kitchat.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.voximplant.demos.kitchat.datastore.model.Credentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialsManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val credentials: Flow<Credentials> = dataStore.data.map { prefs ->
        Credentials(
            region = prefs[KEY_REGION] ?: "",
            channelUUID = prefs[KEY_CHANNEL_UUID] ?: "",
            token = prefs[KEY_TOKEN] ?: "",
            clientID = prefs[KEY_CLIENT_TD] ?: "",
        )
    }

    suspend fun saveRegion(region: String) {
        dataStore.edit { prefs ->
            prefs[KEY_REGION] = region
        }
    }

    suspend fun saveChannelUUID(channelUUID: String) {
        dataStore.edit { prefs ->
            prefs[KEY_CHANNEL_UUID] = channelUUID
        }
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
        }
    }

    suspend fun saveClientID(clientID: String) {
        dataStore.edit { prefs ->
            prefs[KEY_CLIENT_TD] = clientID
        }
    }

    companion object {
        private val KEY_REGION = stringPreferencesKey("KEY_REGION")
        private val KEY_CHANNEL_UUID = stringPreferencesKey("KEY_CHANNEL_UUID")
        private val KEY_TOKEN = stringPreferencesKey("KEY_TOKEN")
        private val KEY_CLIENT_TD = stringPreferencesKey("KEY_CLIENT_TD")
    }
}