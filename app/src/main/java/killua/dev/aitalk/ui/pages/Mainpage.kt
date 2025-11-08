package killua.dev.aitalk.ui.pages

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.aitalk.R
import killua.dev.aitalk.datastore.readSecureHistory
import killua.dev.aitalk.models.ChatRole
import killua.dev.aitalk.states.MainpageState
import killua.dev.aitalk.ui.Routes
import killua.dev.aitalk.ui.components.AIResponseCard
import killua.dev.aitalk.ui.components.BaseResponseCardContainer
import killua.dev.aitalk.ui.components.Greetings
import killua.dev.aitalk.ui.components.MainpageTextfield
import killua.dev.aitalk.ui.components.MainpageTopBar
import killua.dev.aitalk.ui.components.PrimaryScaffold
import killua.dev.aitalk.ui.components.RichInfoCard
import killua.dev.aitalk.ui.tokens.SizeTokens
import killua.dev.aitalk.ui.viewmodels.MainpageUIIntent
import killua.dev.aitalk.ui.viewmodels.MainpageViewModel
import killua.dev.aitalk.utils.BiometricAuth
import killua.dev.aitalk.utils.LocalNavHostController
import killua.dev.aitalk.utils.navigateSingle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun Mainpage() {
    val context = LocalContext.current
    val navController = LocalNavHostController.current!!
    val viewModel: MainpageViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    PrimaryScaffold(
        topBar = {
            MainpageTopBar(
                navController,
                upLeftOnClick = {
                    scope.launch {
                        if (context.readSecureHistory().first()) {
                            BiometricAuth().authenticateAndNavigate(
                                navController = navController,
                                route = Routes.HistoryPage.route,
                            )
                        } else {
                            navController.navigateSingle(Routes.HistoryPage.route)
                        }
                    }
                }
            )
        },
        bottomBar = {
            MainpageTextfield(
                uiState = uiState.value,
                onIntent = { intent ->
                    viewModel.launchOnIO { viewModel.emitIntent(intent) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding() // navigationBarsPadding 已在 Scaffold bottomBar 包装内处理
                    .padding(horizontal = SizeTokens.Level12, vertical = SizeTokens.Level8)
            )
        },
        freezeTopBarOnIme = true,
        snackbarHostState = viewModel.snackbarHostState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SizeTokens.Level12)
        ) {
            if (uiState.value.chatMode && uiState.value.conversationId != null) {
                ChatConversationList(viewModel)
            } else {
                Crossfade(
                    targetState = uiState.value.screenState,
                    animationSpec = tween(durationMillis = 500)
                ) { state ->
                    when(state){
                        MainpageState.GREETINGS -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) { Greetings() }
                        }
                        MainpageState.NO_API_KEY -> {
                            RichInfoCard(
                                modifier = Modifier.padding(horizontal = SizeTokens.Level8),
                                title = stringResource(R.string.no_api_key_set),
                                message = stringResource(R.string.no_api_key_set_desc),
                                actionText =stringResource(R.string.no_api_key_set_action),
                                onActionClick = { navController.navigate(Routes.HelpPage.route) }
                            )
                        }
                        MainpageState.NO_MODELS_ENABLED -> {
                            RichInfoCard(
                                modifier = Modifier.padding(horizontal = SizeTokens.Level8),
                                title = stringResource(R.string.no_model_enabled),
                                message = stringResource(R.string.no_model_enabled_desc),
                                actionText = stringResource(R.string.goto_settings),
                                onActionClick = { navController.navigate(Routes.SettingsPage.route) }
                            )
                        }
                        MainpageState.SHOWING_RESULTS -> {
                            BaseResponseCardContainer({
                                scope.launch { viewModel.emitIntent(MainpageUIIntent.RegenerateAll) }
                            }, {
                                scope.launch { viewModel.emitIntent(MainpageUIIntent.SaveAll) }
                            }) {
                                LazyColumn(
                                    contentPadding = PaddingValues(bottom = SizeTokens.Level4)
                                ) {
                                    items(uiState.value.aiResponses.keys.toList(), key = { it.name }) { model ->
                                        val responseState = uiState.value.aiResponses[model]
                                        AIResponseCard(
                                            responseState = responseState!!,
                                            modelName = model.name,
                                            onCopyClicked = { scope.launch { viewModel.emitIntent(MainpageUIIntent.CopyResponse(model)) } },
                                            onSaveClicked = { scope.launch { viewModel.emitIntent(MainpageUIIntent.SaveSpecificModel(model)) } },
                                            onRegenerateClicked = { scope.launch { viewModel.emitIntent(MainpageUIIntent.RegenerateSpecificModel(model)) } },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatConversationList(viewModel: MainpageViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val messages = uiState.value.chatMessages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = SizeTokens.Level56)
    ) {
        items(messages, key = { it.id }) { item ->
            val modelTag = item.message.model?.name
            ChatBubble(
                isUser = item.message.role.roleName() == "user",
                content = item.message.content,
                modelTag = modelTag,
                onCopy = {
                    // reuse existing intent
                },
                onRetry = {
                    if (modelTag != null) {
                        // Not exposing retry intent yet; could add later.
                    }
                }
            )
        }
    }
}

private fun ChatRole.roleName(): String = when(this){
    ChatRole.User -> "user"
    ChatRole.Assistant -> "assistant"
    ChatRole.System -> "system"
}

@Composable
private fun ChatBubble(
    isUser: Boolean,
    content: String,
    modelTag: String?,
    onCopy: () -> Unit,
    onRetry: () -> Unit,
) {
    val bg = if (isUser) Color(0xFF4F46E5) else Color(0xFF1E293B)
    val textColor = if (isUser) Color.White else Color(0xFFE2E8F0)
    Column(
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(bg)
                .clickable { onCopy() }
                .padding(14.dp)
                .fillMaxWidth(0.92f)
                .align(if (isUser) Alignment.End else Alignment.Start)
        ) {
            Column {
                if (!isUser && modelTag != null) {
                    androidx.compose.material3.Text(
                        text = modelTag,
                        color = textColor.copy(alpha = 0.65f),
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                androidx.compose.material3.Text(
                    text = content.ifBlank { "…" },
                    color = textColor,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    maxLines = 100,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}