package killua.dev.aitalk.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.aitalk.states.ResponseStatus
import killua.dev.aitalk.ui.components.AIResponseCard
import killua.dev.aitalk.ui.components.FloatingController
import killua.dev.aitalk.ui.tokens.SizeTokens
import killua.dev.aitalk.ui.viewmodels.FloatingWindowUIIntent
import killua.dev.aitalk.ui.viewmodels.FloatingWindowViewModel
import kotlinx.coroutines.launch

@Composable
fun FloatingWindowContent(
    selectedText: String,
    onClose: () -> Unit,
) {
    val viewModel: FloatingWindowViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    MaterialTheme {
        LaunchedEffect(Unit) {
            scope.launch { viewModel.emitIntent(FloatingWindowUIIntent.StartSearch(selectedText)) }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SizeTokens.Level8)
        ) {
            FloatingController(uiState.value.isSearching, onClose)
            LazyColumn {
                items(uiState.value.aiResponses.keys.toList()) { model ->
                    val responseState = uiState.value.aiResponses[model]
                    val isSearching = responseState?.status == ResponseStatus.Loading
                    AIResponseCard(
                        responseState = responseState!!,
                        modelName = model.name,
                        onCopyClicked = { scope.launch { viewModel.emitIntent(FloatingWindowUIIntent.CopyResponse(model)) } },
                        onSaveClicked = { scope.launch { viewModel.emitIntent(FloatingWindowUIIntent.SaveSpecificModel(model)) } },
                        onRegenerateClicked = { scope.launch { viewModel.emitIntent(FloatingWindowUIIntent.RegenerateSpecificModel(model)) } },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun FloatingWindowPreview() {
    FloatingWindowContent(
        selectedText = "This is a preview of the selected text.",
        onClose = { /* Implement close logic for preview if needed */ },
    )
}