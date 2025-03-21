package com.voximplant.demos.kitchat.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _credentials: MutableStateFlow<Credentials> = MutableStateFlow(Credentials("", "","",""))
    val credentials: StateFlow<Credentials> = _credentials.asStateFlow()

    init {
        viewModelScope.launch {
            _credentials.emit(credentialsManager.credentials.first())
        }
    }

    fun setRegion(region: String) {
        viewModelScope.launch {
            _credentials.value = _credentials.value.copy(region = region)
            credentialsManager.saveRegion(region)
        }
    }

    fun setChannelUUID(channelUUID: String) {
        viewModelScope.launch {
            _credentials.value = _credentials.value.copy(channelUUID = channelUUID.trimSpaces())
            credentialsManager.saveChannelUUID(channelUUID.trimSpaces())
        }
    }

    fun setToken(token: String) {
        viewModelScope.launch {
            _credentials.value = _credentials.value.copy(token = token.trimSpaces())
            credentialsManager.saveToken(token.trimSpaces())
        }
    }

    fun setClientID(clientID: String) {
        viewModelScope.launch {
            _credentials.value = _credentials.value.copy(clientID = clientID.trimSpaces())
            credentialsManager.saveClientID(clientID
                .trimSpaces()
            )
        }
    }

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
}