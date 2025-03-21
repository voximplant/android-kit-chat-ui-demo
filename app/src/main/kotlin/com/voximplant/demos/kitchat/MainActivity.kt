/*
 * Copyright (c) 2011 - 2025, Voximplant, Inc. All rights reserved.
 */

package com.voximplant.demos.kitchat

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.voximplant.android.kit.chat.ui.KitChatUi
import com.voximplant.demos.kitchat.ui.HomeRoute
import com.voximplant.demos.kitchat.ui.theme.KitChatDemoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach {
                        uiState = it
                    }
                    .collect {}
            }
        }

        checkIntent(intent)

        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                MainActivityUiState.Loading -> true
                is MainActivityUiState.Success -> false
            }
        }

        enableEdgeToEdge()
        setContent {
            KitChatDemoTheme {
                HomeRoute()
            }
        }
    }

    private fun checkIntent(intent: Intent) {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                if (uiState is MainActivityUiState.Success) {
                    if (KitChatUi.checkIntent(intent)) {
                        val credentials = viewModel.credentials.firstOrNull() ?: return@collect

                        KitChatUi(
                            context = applicationContext,
                            accountRegion = credentials.region,
                            channelUuid = credentials.channelUUID,
                            token = credentials.token,
                            clientId = credentials.clientID,
                        ).startActivity()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkIntent(intent)
    }
}

