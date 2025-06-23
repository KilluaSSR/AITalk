package killua.dev.aitalk.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalContext
import killua.dev.aitalk.R
import killua.dev.aitalk.ui.viewmodels.MainpageUIIntent
import killua.dev.aitalk.ui.viewmodels.MainpageUIState
import kotlinx.coroutines.delay

@Composable
fun MainpageTextfield(
    uiState: MainpageUIState,
    onIntent: (MainpageUIIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var localText by remember { mutableStateOf(uiState.searchQuery) }

    LaunchedEffect(localText) {
        delay(300)
        if (localText != uiState.searchQuery) {
            onIntent(MainpageUIIntent.UpdateSearchQuery(localText))
        }
    }

    LaunchedEffect(uiState.searchQuery) {
        if (localText != uiState.searchQuery) {
            localText = uiState.searchQuery
        }
    }

    val showSendButton = !uiState.isSearching && localText.isNotBlank()
    val showStopButton = uiState.isSearching

    RainbowTextField(
        value = localText,
        onValueChange = { localText = it },
        modifier = Modifier.fillMaxWidth(0.9f),
        placeholder = {
            Text(context.getString(R.string.maintextfield_ask))
        },
        trailingIcon = {
            when {
                showStopButton -> {
                    IconButton(onClick = { onIntent(MainpageUIIntent.OnStopButtonClick) }) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop")
                    }
                }
                showSendButton -> {
                    IconButton(onClick = { onIntent(MainpageUIIntent.OnSendButtonClick) }) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        },
        enabled = uiState.isInputEnabled
    )

}