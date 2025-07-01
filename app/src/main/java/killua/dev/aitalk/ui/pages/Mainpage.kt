package killua.dev.aitalk.ui.pages

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.aitalk.datastore.readSecureHistory
import killua.dev.aitalk.states.ResponseStatus
import killua.dev.aitalk.ui.Routes
import killua.dev.aitalk.ui.components.AIResponseCard
import killua.dev.aitalk.ui.components.BaseResponseCardContainer
import killua.dev.aitalk.ui.components.Greetings
import killua.dev.aitalk.ui.components.MainpageTextfield
import killua.dev.aitalk.ui.components.MainpageTopBar
import killua.dev.aitalk.ui.components.PrimaryScaffold
import killua.dev.aitalk.ui.tokens.SizeTokens
import killua.dev.aitalk.ui.viewmodels.MainpageUIIntent
import killua.dev.aitalk.ui.viewmodels.MainpageViewModel
import killua.dev.aitalk.utils.BiometricAuth
import killua.dev.aitalk.utils.LocalNavHostController
import killua.dev.aitalk.utils.navigateSingle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("StateFlowValueCalledInComposition")
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
        snackbarHostState = viewModel.snackbarHostState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SizeTokens.Level12)
        ) {

            MainpageTextfield(
                uiState = viewModel.uiState.value,
                onIntent = { intent ->
                    viewModel.launchOnIO {
                        viewModel.emitIntent(intent)
                    }
                },
                modifier = Modifier
                    .padding(SizeTokens.Level4)
                    .fillMaxWidth()
            )
            Crossfade(
                targetState = uiState.value.showGreetings,
                animationSpec = tween(durationMillis = 500)
            ) { showGreetings ->
                if (showGreetings) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Greetings()
                    }
                } else {
                    BaseResponseCardContainer({
                        scope.launch { viewModel.emitIntent(MainpageUIIntent.RegenerateAll) }
                    }, {
                        scope.launch { viewModel.emitIntent(MainpageUIIntent.SaveAll) }
                    }) {
                        LazyColumn {
                            items(uiState.value.aiResponses.keys.toList()) { model ->
                                val responseState = uiState.value.aiResponses[model]
                                val isSearching = responseState?.status == ResponseStatus.Loading
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