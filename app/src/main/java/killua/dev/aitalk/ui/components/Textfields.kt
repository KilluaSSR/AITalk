package killua.dev.aitalk.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import killua.dev.aitalk.R
import killua.dev.aitalk.states.MainpageState
import killua.dev.aitalk.ui.tokens.SizeTokens
import killua.dev.aitalk.ui.viewmodels.MainpageUIIntent
import killua.dev.aitalk.ui.viewmodels.MainpageUIState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.compose.runtime.snapshotFlow

@Composable
fun MainpageTextfield(
    uiState: MainpageUIState,
    onIntent: (MainpageUIIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var localText by remember { mutableStateOf(uiState.searchQuery) }

    @OptIn(FlowPreview::class)
    LaunchedEffect(Unit) {
        snapshotFlow { localText }
            .debounce(300)
            .distinctUntilChanged()
            .collectLatest { latest ->
                if (latest != uiState.searchQuery) {
                    onIntent(MainpageUIIntent.UpdateSearchQuery(latest))
                }
            }
    }

    LaunchedEffect(uiState.searchQuery) {
        if (localText != uiState.searchQuery) {
            localText = uiState.searchQuery
        }
    }

    val showSendButton = !uiState.isSearching && localText.isNotBlank()
    val showStopButton = uiState.isSearching

    val radiusCorner = SizeTokens.Level16
    val shape: Shape = RoundedCornerShape(radiusCorner)
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        BaseTextField(
            value = localText,
            onValueChange = { localText = it },
            placeholder = {
                Text(context.getString(R.string.maintextfield_ask))
            },
            trailingIcon = {
                Row(modifier = Modifier.padding(SizeTokens.Level4)){
                    IconButton(onClick = {
                        localText = ""
                        onIntent(MainpageUIIntent.ClearInput)
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                    when {
                        showStopButton -> {
                            IconButton(onClick = { onIntent(MainpageUIIntent.OnStopButtonClick) }) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop")
                            }
                        }
                        showSendButton -> {
                            IconButton(onClick = { onIntent(MainpageUIIntent.OnSendButtonClick(localText)) }) {
                                Icon(Icons.Default.Send, contentDescription = "Send")
                            }
                        }
                    }
                }

            },
            modifier = modifier,
            shape = shape
        )
    }
}

@Preview
@Composable

fun MainpageTextfieldPreview(){
    MainpageTextfield(
        uiState = MainpageUIState(screenState = MainpageState.SHOWING_RESULTS),
        onIntent = {},
    )
}