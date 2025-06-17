/*
 * Copyright (c) 2011 - 2025, Voximplant, Inc. All rights reserved.
 */

package com.voximplant.demos.kitchat.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.voximplant.android.kit.chat.ui.KitChatUi
import com.voximplant.demos.kitchat.datastore.CredentialsManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@AndroidEntryPoint
class FirebasePushService : FirebaseMessagingService() {

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var credentialsManager: CredentialsManager

    override fun onMessageReceived(message: RemoteMessage) {
        coroutineScope.launch {
            val credentials = credentialsManager.credentials.firstOrNull() ?: return@launch
            val region = credentialsManager.mapRegion(credentials.region)
            if (region != null) {
                KitChatUi(
                    context = applicationContext,
                    accountRegion = region,
                    channelUuid = credentials.channelUUID,
                    token = credentials.token,
                    clientId = credentials.clientID,
                ).handlePush(message.data)
            }
        }
    }

    override fun onNewToken(token: String) {}
}
