package com.voximplant.demos.kitchat.push

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePushTokenProvider @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
) {
    suspend fun getToken(): String = firebaseMessaging.token.await()
}