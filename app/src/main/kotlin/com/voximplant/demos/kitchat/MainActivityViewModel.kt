/*
 * Copyright (c) 2011 - 2025, Voximplant, Inc. All rights reserved.
 */

package com.voximplant.demos.kitchat

import androidx.lifecycle.ViewModel
import com.voximplant.android.kit.chat.core.model.Region
import com.voximplant.demos.kitchat.datastore.CredentialsManager
import com.voximplant.demos.kitchat.datastore.model.Credentials
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val credentialsManager: CredentialsManager,
): ViewModel() {

    private val _uiState: MutableStateFlow<MainActivityUiState> = MutableStateFlow(MainActivityUiState.Loading)
    val uiState: StateFlow<MainActivityUiState> = _uiState.asStateFlow()

    val credentials: Flow<Credentials> = credentialsManager.credentials

    init {
        _uiState.value = MainActivityUiState.Success
    }

    fun mapRegion(region: String): Region? = credentialsManager.mapRegion(region)
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data object Success : MainActivityUiState
}