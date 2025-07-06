package killua.dev.aitalk.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import dev.jeziellago.compose.markdowntext.MarkdownText
import killua.dev.aitalk.R
import killua.dev.aitalk.ui.tokens.SizeTokens

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HistoryPageAIResponseContent(
    modelName: String,
    content: String,
    onCopyClicked: (() -> Unit),
) {
    val context = LocalContext.current
    val shape: Shape = MaterialTheme.shapes.medium
    val colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )


    Card(
        shape = shape,
        colors = colors,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SizeTokens.Level16, vertical = SizeTokens.Level8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = modelName,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.contentColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Column {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = SizeTokens.Level16),
                    thickness = SizeTokens.Level1,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                MarkdownText(
                    markdown = content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.contentColor
                    ),
                    modifier = Modifier
                        .padding(horizontal = SizeTokens.Level16, vertical = SizeTokens.Level12)
                        .fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SizeTokens.Level8)
                ) {
                    TextButton(onClick = onCopyClicked) {
                        Text(text = context.getString(R.string.onCopyButton))
                    }
                }
            }
        }
    }
}