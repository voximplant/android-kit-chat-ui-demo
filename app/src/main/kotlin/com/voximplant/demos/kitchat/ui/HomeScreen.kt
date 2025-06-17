/*
 * Copyright (c) 2011 - 2025, Voximplant, Inc. All rights reserved.
 */

package com.voximplant.demos.kitchat.ui

import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voximplant.android.kit.chat.ui.KitChatUi
import com.voximplant.android.kit.chat.ui.core.model.AuthorizationError
import com.voximplant.demos.kitchat.R
import com.voximplant.demos.kitchat.ui.theme.Gray05
import com.voximplant.demos.kitchat.ui.theme.Gray10
import com.voximplant.demos.kitchat.ui.theme.Gray100
import com.voximplant.demos.kitchat.ui.theme.Gray50
import com.voximplant.demos.kitchat.ui.theme.Gray90
import com.voximplant.demos.kitchat.ui.theme.KitChatDemoTheme
import com.voximplant.demos.kitchat.ui.theme.Purple40
import com.voximplant.demos.kitchat.ui.theme.Purple90
import com.voximplant.demos.kitchat.ui.theme.Red50
import com.voximplant.demos.kitchat.ui.theme.Red95
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    val credentials by viewModel.credentials.collectAsStateWithLifecycle()

    var expanded by rememberSaveable { mutableStateOf(false) }
    val listValues = listOf("ru", "ru2", "eu", "us", "br", "kz")

    var regionError: String? by rememberSaveable { mutableStateOf(null) }
    var channelUuidError: String? by rememberSaveable { mutableStateOf(null) }
    var tokenError: String? by rememberSaveable { mutableStateOf(null) }
    var clientIDError: String? by rememberSaveable { mutableStateOf(null) }

    var notificationsPermissionGranted by rememberSaveable { mutableStateOf(true) }
    var notificationsPermissionRequest by rememberSaveable { mutableStateOf(false) }

    var showRegistrationErrorBanner by rememberSaveable { mutableStateOf(false) }
    var showRepeatRegistrationBanner by rememberSaveable { mutableStateOf(false) }

    fun checkCredentials(): Boolean {
        if (credentials.region.isEmpty()) {
            regionError = context.getString(R.string.field_cannot_be_empty_error)
        }

        if (credentials.channelUUID.isEmpty()) {
            channelUuidError = context.getString(R.string.field_cannot_be_empty_error)
        }

        if (credentials.token.isEmpty()) {
            tokenError = context.getString(R.string.field_cannot_be_empty_error)
        }

        if (credentials.clientID.isEmpty()) {
            clientIDError = context.getString(R.string.field_cannot_be_empty_error)
        }

        return regionError == null && channelUuidError == null && tokenError == null && clientIDError == null
    }

    fun checkAuthorizationState(error: AuthorizationError) {
        when (error) {
            is AuthorizationError.InvalidChannelUuid -> {
                channelUuidError = context.getString(R.string.invalid_value_error)
            }

            is AuthorizationError.InvalidToken -> {
                tokenError = context.getString(R.string.invalid_value_error)
            }

            is AuthorizationError.InvalidClientId -> {
                clientIDError = context.getString(R.string.invalid_value_error)
            }
        }
    }

    fun registerPushToken() {
        job?.cancel()
        job = scope.launch {
            val pushToken = viewModel.getPushToken()
            if (pushToken == null) {
                showRegistrationErrorBanner = true
                showRepeatRegistrationBanner = false
                return@launch
            }
            val region = viewModel.getRegion(credentials.region)
            if (region != null) {
                KitChatUi(
                    context = context,
                    accountRegion = region,
                    channelUuid = credentials.channelUUID,
                    token = credentials.token,
                    clientId = credentials.clientID,
                ).apply {
                    onAuthorizationError = ::checkAuthorizationState
                }.registerPushToken(pushToken)
                    .onSuccess {
                        showRegistrationErrorBanner = false
                        showRepeatRegistrationBanner = false
                    }
                    .onFailure {
                        showRegistrationErrorBanner = true
                        showRepeatRegistrationBanner = false
                    }
            } else {
                regionError = context.getString(R.string.field_cannot_be_empty_error)
            }
        }
    }

    NotificationsPermissionEffect(
        notificationsPermissionRequest = notificationsPermissionRequest,
        notificationsPermissionGranted = { isGranted ->
            notificationsPermissionGranted = isGranted
        },
        onDismiss = {
            notificationsPermissionRequest = false
        },
        onConfirm = {
            notificationsPermissionRequest = false
            context.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
            )
        },
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                focusManager.clearFocus()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        (context as ComponentActivity).enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                Color(0x1b, 0x1b, 0x1b, 0x80).toArgb()
            )
        )
    }

    LaunchedEffect(Unit) {
        if (notificationsPermissionGranted) {
            val dropInitCredentials = viewModel.credentials.drop(1).first()
            if (dropInitCredentials.region.isEmpty() || dropInitCredentials.channelUUID.isEmpty() || dropInitCredentials.token.isEmpty() || dropInitCredentials.clientID.isEmpty()) return@LaunchedEffect
            registerPushToken()
        }
    }

    HomeScreen(
        selectedRegion = credentials.region,
        channelUuid = credentials.channelUUID,
        token = credentials.token,
        clientID = credentials.clientID,
        expanded = expanded,
        listValues = listValues,
        regionError = regionError,
        channelUuidError = channelUuidError,
        tokenError = tokenError,
        clientIDError = clientIDError,
        isShowRegistrationError = showRegistrationErrorBanner,
        isShowRepeatRegistrationBanner = showRepeatRegistrationBanner,
        isNotificationPermissionGranted = notificationsPermissionGranted,
        onBannerClick = {
            showRegistrationErrorBanner = false
            showRepeatRegistrationBanner = true
            registerPushToken()
        },
        onRequestPermissionClick = {
            notificationsPermissionRequest = true
        },
        onOpenChatClick = {
            if (checkCredentials()) {
                if (notificationsPermissionGranted) {
                    registerPushToken()
                }
                val region = viewModel.getRegion(credentials.region)
                if (region != null) {
                    KitChatUi(
                        context = context,
                        accountRegion = region,
                        channelUuid = credentials.channelUUID,
                        token = credentials.token,
                        clientId = credentials.clientID,
                    ).apply {
                        onAuthorizationError = ::checkAuthorizationState
                    }.startActivity()
                } else {
                    regionError = context.getString(R.string.field_cannot_be_empty_error)
                }
            }
        },
        onItemClick = { item ->
            expanded = false
            regionError = null
            viewModel.setRegion(item)
        },
        onDismissRequest = {
            expanded = false
        },
        onExpandedChange = { isExpanded ->
            expanded = isExpanded
        },
        onChannelUuidValueChange = {
            channelUuidError = null
            viewModel.setChannelUUID(it)
        },
        onTokenValueChange = {
            tokenError = null
            viewModel.setToken(it)
        },
        onClientIDValueChange = {
            clientIDError = null
            viewModel.setClientID(it)
        },
    )
}

@Composable
fun HomeScreen(
    selectedRegion: String,
    channelUuid: String,
    token: String,
    clientID: String,
    expanded: Boolean,
    listValues: List<String>,
    regionError: String?,
    channelUuidError: String?,
    tokenError: String?,
    clientIDError: String?,
    isNotificationPermissionGranted: Boolean,
    isShowRegistrationError: Boolean,
    isShowRepeatRegistrationBanner: Boolean,
    onBannerClick: () -> Unit,
    onRequestPermissionClick: () -> Unit,
    onOpenChatClick: () -> Unit,
    onItemClick: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onChannelUuidValueChange: (String) -> Unit,
    onTokenValueChange: (String) -> Unit,
    onClientIDValueChange: (String) -> Unit,
) {
    Scaffold(
        bottomBar = {
            ButtonsBar(
                modifier = Modifier
                    .background(Gray100)
                    .navigationBarsPadding(),
                backgroundColor = Gray100,
                isNotificationPermissionGranted = isNotificationPermissionGranted,
                onRequestPermissionClick = onRequestPermissionClick,
                onOpenChatClick = onOpenChatClick
            )
        },
        containerColor = Gray05,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(WindowInsets(bottom = paddingValues.calculateBottomPadding()))
                .padding(bottom = paddingValues.calculateBottomPadding())
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Header(
                modifier = Modifier,
                contentPadding = paddingValues,
            )
            Column(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Gray100)
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp),
            ) {
                if (isShowRegistrationError) {
                    RegistrationErrorBanner(
                        onBannerClick = onBannerClick
                    )
                }
                if (isShowRepeatRegistrationBanner) {
                    UpdateBanner()
                }
                MenuField(
                    expanded = expanded,
                    listValues = listValues,
                    selectedOptions = selectedRegion.ifEmpty { stringResource(R.string.select_region) },
                    fieldName = stringResource(R.string.region),
                    errorText = regionError,
                    onItemClick = onItemClick,
                    onDismissRequest = onDismissRequest,
                    onExpandedChange = onExpandedChange,
                )
                InputField(
                    fieldName = stringResource(R.string.channel_uuid),
                    labelField = stringResource(R.string.enter_channel_uuid),
                    errorText = channelUuidError,
                    textValue = channelUuid,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    onValueChange = onChannelUuidValueChange,
                )
                InputField(
                    fieldName = stringResource(R.string.token),
                    labelField = stringResource(R.string.enter_token),
                    errorText = tokenError,
                    textValue = token,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    onValueChange = onTokenValueChange,
                )
                InputField(
                    fieldName = stringResource(R.string.client_id),
                    labelField = stringResource(R.string.enter_client_id),
                    errorText = clientIDError,
                    isSingleLine = false,
                    maxLines = 3,
                    textValue = clientID,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    onValueChange = onClientIDValueChange,
                )
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Gray100),
            )
        }
    }
}

@Composable
fun InputField(
    fieldName: String,
    labelField: String,
    errorText: String?,
    isSingleLine: Boolean = true,
    maxLines: Int = 1,
    textValue: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Text(
            modifier = Modifier
                .sizeIn(minHeight = 20.dp)
                .padding(bottom = 6.dp),
            text = fieldName,
            fontSize = 15.sp,
        )
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (errorText?.isNotEmpty() == true) Red50 else Gray90,
                    RoundedCornerShape(8.dp)
                )
                .background(Gray90, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            value = textValue,
            onValueChange = onValueChange,
            maxLines = maxLines,
            singleLine = isSingleLine,
            textStyle = TextStyle(fontSize = 15.sp, color = Gray10),
            keyboardOptions = keyboardOptions,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.sizeIn(minHeight = 38.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (textValue.isEmpty()) {
                        Text(
                            modifier = Modifier,
                            text = labelField,
                            fontSize = 15.sp,
                            color = Gray50,
                        )
                    }
                    Box(modifier = Modifier.padding(vertical = 10.dp)) {
                        innerTextField()
                    }
                }
            },
        )
        Column(
            modifier = Modifier.height(18.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            if (errorText?.isNotEmpty() == true) {
                Text(
                    text = errorText,
                    fontSize = 12.sp,
                    color = Red50,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuField(
    expanded: Boolean,
    listValues: List<String>,
    selectedOptions: String,
    fieldName: String,
    errorText: String?,
    onItemClick: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onExpandedChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Text(
            modifier = Modifier
                .sizeIn(minHeight = 20.dp)
                .padding(bottom = 6.dp),
            text = fieldName,
            fontSize = 15.sp,
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            content = {
                BasicTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (errorText?.isNotEmpty() == true) Red50 else Gray90,
                            RoundedCornerShape(8.dp)
                        )
                        .background(Gray90, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp),
                    readOnly = true,
                    value = "",
                    onValueChange = {},
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp, color = Gray10),
                    decorationBox = { _ ->
                        Row(
                            modifier = Modifier.sizeIn(minHeight = 38.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = selectedOptions,
                                fontSize = 15.sp,
                                color = if (selectedOptions != stringResource(R.string.select_region)) Black else Gray50,
                            )
                            IconButton(
                                modifier = Modifier.size(28.dp),
                                onClick = { },
                                content = {
                                    Icon(
                                        painter = painterResource(if (expanded) R.drawable.ic_up else R.drawable.ic_down),
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                )
                ExposedDropdownMenu(
                    modifier = Modifier
                        .background(Gray100)
                        .padding(horizontal = 8.dp),
                    expanded = expanded,
                    onDismissRequest = onDismissRequest,
                ) {
                    Column {
                        listValues.forEachIndexed { index, value ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (value == selectedOptions) Purple90 else Gray100)
                                    .clickable {
                                        onItemClick(value)
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
                                    text = value,
                                    fontSize = 15.sp,
                                    color = if (value == selectedOptions) Purple40 else Black,
                                )
                                if (value == selectedOptions) {
                                    Icon(
                                        modifier = Modifier.padding(4.dp),
                                        painter = painterResource(R.drawable.ic_close),
                                        contentDescription = null,
                                        tint = Purple40,
                                    )
                                }
                            }
                            if (listValues.size - 1 != index) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    thickness = 1.dp,
                                    color = Gray50.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
            }
        )
        Column(
            modifier = Modifier.height(18.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            if (errorText?.isNotEmpty() == true) {
                Text(
                    text = errorText,
                    fontSize = 12.sp,
                    color = Red50,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun ButtonsBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Gray100,
    isNotificationPermissionGranted: Boolean,
    onRequestPermissionClick: () -> Unit,
    onOpenChatClick: () -> Unit,
) {
    Column(
        modifier
            .background(backgroundColor)
            .padding(16.dp),
    ) {
        if (!isNotificationPermissionGranted) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                onClick = onRequestPermissionClick,
                content = {
                    Icon(
                        painter = painterResource(R.drawable.ic_notification_on),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(R.string.allow_notifications),
                        fontSize = 16.sp
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = Purple90,
                    contentColor = Purple40
                )
            )
        }
        Button(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .height(44.dp),
            onClick = onOpenChatClick,
            content = {
                Text(
                    text = stringResource(R.string.open_chat),
                    fontSize = 16.sp
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = Purple40,
                contentColor = Gray100
            )
        )
    }
}

@Composable
private fun Header(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Box(modifier = Modifier.height(70.dp + contentPadding.calculateTopPadding())) {
        Box(
            modifier = Modifier.padding(top = contentPadding.calculateTopPadding()),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                modifier = Modifier
                    .requiredSize(134.dp)
                    .graphicsLayer {
                        translationX = -116.dp.toPx()
                        translationY = 67.dp.toPx()
                        rotationZ = -15f
                    },
                painter = painterResource(R.drawable.ic_rectangle),
                contentDescription = null,
            )
            Image(
                modifier = Modifier
                    .requiredSize(134.dp)
                    .graphicsLayer {
                        translationX = 116.dp.toPx()
                        translationY = -67.dp.toPx()
                        rotationZ = 165f
                    },
                painter = painterResource(R.drawable.ic_rectangle),
                contentDescription = null,
            )
            Image(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp)
                    .size(40.dp),
                painter = painterResource(R.drawable.ic_sdk_purple),
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun RegistrationErrorBanner(
    onBannerClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .padding(bottom = 24.dp),
        colors = CardDefaults.cardColors().copy(
            containerColor = Red95
        ),
        onClick = onBannerClick,
        content = {
            Column(
                modifier = Modifier.padding(
                    start = 12.dp,
                    top = 12.dp,
                    end = 12.dp,
                    bottom = 16.dp
                ),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(R.drawable.ic_fail_big),
                        contentDescription = null,
                        tint = Red50,
                    )
                    Row(
                        modifier = Modifier
                            .sizeIn(minHeight = 32.dp)
                            .padding(start = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.error_of_registration_of_the_push_token),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(32.dp))
                    Text(
                        modifier = Modifier.padding(start = 6.dp),
                        text = stringResource(R.string.try_again),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Red50,
                    )
                }
            }
        },
    )
}

@Composable
private fun UpdateBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .padding(bottom = 24.dp),
        colors = CardDefaults.cardColors().copy(
            containerColor = Purple90,
        ),
        content = {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Gray10,
                        strokeWidth = 2.dp
                    )
                }
                Row(
                    modifier = Modifier
                        .sizeIn(minHeight = 32.dp)
                        .padding(start = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.repeat_attempt),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
    )
}

@Preview
@Composable
fun HomeScreenPreview() {
    KitChatDemoTheme {
        HomeScreen(
            selectedRegion = "eu",
            channelUuid = "5dde4c631920b5",
            token = "",
            clientID = "dffa083 aaef3895dde4c631920b54b53dffa083aaef3895dde4c631920b54b53dffa083aaef3895dde4c631920b54b53dffa083aaef3",
            expanded = false,
            listValues = listOf("ru", "ru2", "eu", "us", "br", "kz"),
            regionError = null,
            channelUuidError = null,
            tokenError = "Токен не может быть пустым",
            clientIDError = "Невалидное значение",
            isShowRegistrationError = true,
            isShowRepeatRegistrationBanner = true,
            onBannerClick = {},
            onRequestPermissionClick = {},
            onOpenChatClick = {},
            isNotificationPermissionGranted = false,
            onItemClick = { _ -> },
            onDismissRequest = {},
            onExpandedChange = { _ -> },
            onChannelUuidValueChange = {},
            onTokenValueChange = {},
            onClientIDValueChange = {},
        )
    }
}

@Preview
@Composable
private fun HeaderPreview() {
    KitChatDemoTheme {
        Header()
    }
}

@Preview
@Composable
private fun MenuFieldPreview() {
    KitChatDemoTheme {
        MenuField(
            expanded = false,
            listValues = listOf("ru", "ru2", "eu", "us", "br", "kz"),
            selectedOptions = "ru",
            fieldName = stringResource(R.string.region),
            errorText = null,
            onItemClick = {},
            onDismissRequest = {},
            onExpandedChange = {}
        )
    }
}

@Preview
@Composable
private fun InputFieldSingleLineErrorPreview() {
    KitChatDemoTheme {
        InputField(
            fieldName = stringResource(R.string.channel_uuid),
            labelField = stringResource(R.string.enter_channel_uuid),
            errorText = stringResource(R.string.field_cannot_be_empty_error),
            isSingleLine = true,
            maxLines = 1,
            textValue = "",
            onValueChange = {}
        )
    }
}

@Preview
@Composable
private fun InputFieldPreview() {
    KitChatDemoTheme {
        InputField(
            fieldName = stringResource(R.string.channel_uuid),
            labelField = stringResource(R.string.enter_channel_uuid),
            errorText = "",
            isSingleLine = false,
            maxLines = 3,
            textValue = "dffa083aaef3895dde4c631920b54b53dffa083aaef3895dde4c631920b54b53dffa083aaef3895dde4c631920b54b53dffa083aaef3",
            onValueChange = {}
        )
    }
}

@Preview
@Composable
private fun ButtonsBarPreview() {
    KitChatDemoTheme {
        ButtonsBar(
            modifier = Modifier,
            isNotificationPermissionGranted = false,
            onRequestPermissionClick = {},
            onOpenChatClick = {}
        )
    }
}

@Preview
@Composable
private fun RegistrationErrorBannerPreview() {
    KitChatDemoTheme {
        RegistrationErrorBanner(
            onBannerClick = {}
        )
    }
}

@Preview
@Composable
private fun UpdateBannerPreview() {
    KitChatDemoTheme {
        UpdateBanner()
    }
}