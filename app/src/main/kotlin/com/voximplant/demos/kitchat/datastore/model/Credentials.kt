/*
 * Copyright (c) 2011 - 2025, Voximplant, Inc. All rights reserved.
 */

package com.voximplant.demos.kitchat.datastore.model

data class Credentials(
    val region: String,
    val channelUUID: String,
    val token: String,
    val clientID: String,
)