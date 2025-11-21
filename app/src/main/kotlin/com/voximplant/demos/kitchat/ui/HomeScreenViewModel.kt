/*
 * Copyright (c) 2011 - 2025, Voximplant, Inc. All rights reserved.
 */

package com.voximplant.demos.kitchat.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.android.kit.chat.ui.model.Region
import com.voximplant.demos.kitchat.datastore.CredentialsManager
import com.voximplant.demos.kitchat.datastore.model.Credentials
import com.voximplant.demos.kitchat.push.FirebasePushTokenProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val credentialsManager: CredentialsManager,
    private val pushTokenProvider: FirebasePushTokenProvider,
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.emit(UiState.Success(credentialsManager.credentials.first()))
        }
    }

    fun setRegion(region: String) {
        viewModelScope.launch {
            val state = uiState.value
            if (state is UiState.Success) _uiState.value = state.copy(state.credentials.copy(region = region))
            credentialsManager.saveRegion(region)
        }
    }

    fun setChannelUUID(channelUUID: String) {
        viewModelScope.launch {
            val state = uiState.value
            if (state is UiState.Success) _uiState.value = state.copy(state.credentials.copy(channelUUID = channelUUID.trimSpaces()))
            credentialsManager.saveChannelUUID(channelUUID.trimSpaces())
        }
    }

    fun setToken(token: String) {
        viewModelScope.launch {
            val state = uiState.value
            if (state is UiState.Success) _uiState.value = state.copy(state.credentials.copy(token = token.trimSpaces()))
            credentialsManager.saveToken(token.trimSpaces())
        }
    }

    fun setClientID(clientID: String) {
        viewModelScope.launch {
            val state = uiState.value
            if (state is UiState.Success) _uiState.value = state.copy(state.credentials.copy(clientID = clientID.trimSpaces()))
            credentialsManager.saveClientID(clientID.trimSpaces())
        }
    }

    fun getRegion(region: String): Region? = credentialsManager.getRegion(region)

    suspend fun getPushToken(): String? {
        return try {
            pushTokenProvider.getToken()
        } catch (e: Exception) {
            Log.e("Voximplant", "getPushToken Failed", e)
            null
        }
    }

    private fun String.trimSpaces(): String {
        return this.replace(" ", "").replace(Regex("[\n\r]"), "")
    }

    sealed class UiState {
        data object Loading: UiState()
        data class Success(val credentials: Credentials): UiState()
    }
}