package killua.dev.aitalk.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import killua.dev.aitalk.R
import killua.dev.aitalk.ui.effects.shimmerEffect
import killua.dev.aitalk.ui.tokens.SizeTokens

@Composable
fun AIResponseCard(
    modelName: String,
    content: String,
    onCopyClicked: (() -> Unit),
    onSaveClicked: (() -> Unit),
    onRegenerateClicked: (() -> Unit),
){
    val context = LocalContext.current
    val shape: Shape = MaterialTheme.shapes.medium
    val colors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
    var expanded by remember { mutableStateOf(false) }
    ElevatedCard (
        shape = shape,
        colors = colors,
        modifier = Modifier
            .fillMaxWidth()
            .padding(SizeTokens.Level8)

    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shimmerEffect()
        ) {
            Column(
                modifier = Modifier.padding(SizeTokens.Level16)
            ) {
                Text(
                    text = modelName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = SizeTokens.Level8)
                )

                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    modifier = Modifier.animateContentSize()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextButton(onClick = onCopyClicked) {
                    Text(
                        text = context.getString(R.string.onCopyButton),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(onClick = onSaveClicked) {
                    Text(
                        text = context.getString(R.string.onSaveButton),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(onClick = onRegenerateClicked) {
                    Text(
                        text = context.getString(R.string.onRegenerateButton),
                        color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        expanded = !expanded
                    }
                ) {
                    Icon(
                        imageVector = if(!expanded) Icons.Rounded.ExpandMore else Icons.Rounded.ExpandLess,
                        null
                    )
                }

            }
        }
    }
}


@Preview
@Composable
fun AIResponseCardPreview(){
    AIResponseCard(
        "Gemini",
        content = "This is AI Generated",
        onCopyClicked = {},
        onSaveClicked = {},
        onRegenerateClicked = {},
    )
}