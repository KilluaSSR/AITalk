package killua.dev.aitalk.ui.pages

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.aitalk.ui.components.AIResponseCard
import killua.dev.aitalk.ui.components.Greetings
import killua.dev.aitalk.ui.components.MainpageTextfield
import killua.dev.aitalk.ui.components.MainpageTopBar
import killua.dev.aitalk.ui.components.PrimaryScaffold
import killua.dev.aitalk.ui.effects.shimmerEffect
import killua.dev.aitalk.ui.tokens.SizeTokens
import killua.dev.aitalk.ui.viewmodels.MainpageViewModel
import killua.dev.aitalk.utils.LocalNavHostController

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun Mainpage(){
    val context = LocalContext.current
    val navController = LocalNavHostController.current!!
    var showDrawer by remember { mutableStateOf(false) }
    var showMore by remember { mutableStateOf(false) }
    val viewModel: MainpageViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    PrimaryScaffold(
        topBar = { MainpageTopBar(
            navController,
            upLeftOnClick = {showDrawer = true}
        ) { showMore = true }
        },
        snackbarHostState = viewModel.snackbarHostState
    ) {
        Column (
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
                    LazyColumn {
                        items(5) {
                            AIResponseCard(
                                "Gemini",
                                content = "This is AI Generated",
                                onCopyClicked = {},
                                onSaveClicked = {},
                                onRegenerateClicked = {},
                            )
                        }
                    }
                }
            }
        }
    }
}