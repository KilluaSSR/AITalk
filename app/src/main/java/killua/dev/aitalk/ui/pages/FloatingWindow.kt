package killua.dev.aitalk.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import killua.dev.aitalk.ui.components.AIResponseCard
import killua.dev.aitalk.ui.components.FloatingController
import killua.dev.aitalk.ui.tokens.SizeTokens

@Composable
fun FloatingWindowContent(
    selectedText: String,
    onClose: () -> Unit,
    onSearch: (String) -> Unit
) {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SizeTokens.Level8)
        ) {
            FloatingController(onClose)
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

@Preview
@Composable
fun FloatingWindowPreview() {
    FloatingWindowContent(
        selectedText = "This is a preview of the selected text.",
        onClose = { /* Implement close logic for preview if needed */ },
        onSearch = { /* Implement search logic for preview if needed */ }
    )
}