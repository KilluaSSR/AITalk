package killua.dev.aitalk.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import killua.dev.aitalk.R
import killua.dev.aitalk.db.SearchHistoryEntity
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.ui.tokens.SizeTokens
import killua.dev.aitalk.utils.timestampToDate
import killua.dev.aitalk.utils.toSavable
import killua.dev.aitalk.utils.toSavableMap

@Composable
fun HistorypageItemCard(
    history: SearchHistoryEntity,
    onCopyPrompt: () -> Unit,
    onSaveAll: () -> Unit,
    onDelete: () -> Unit,
    onCopyResponse: (model: AIModel, content: String) -> Unit,
) {
    val context = LocalContext.current
    val shape: Shape = MaterialTheme.shapes.medium
    val colors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
    var expanded by remember { mutableStateOf(false) }
    val responsesMap = remember(history) {
        history.toSavableMap()
    }

    ElevatedCard(
        shape = shape,
        colors = colors,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SizeTokens.Level4)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(
                        start = SizeTokens.Level16,
                        end = SizeTokens.Level16,
                        top = SizeTokens.Level16,
                        bottom = SizeTokens.Level8
                    )
            ) {
                Text(
                    text = history.prompt,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.contentColor,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    modifier = Modifier.animateContentSize()
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SizeTokens.Level16, vertical = SizeTokens.Level8),
                    verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8)
                ) {
                    responsesMap.forEach { (model, responseState) ->
                        HistoryPageAIResponseContent(
                            content = responseState.content!!,
                            modelName = model.name,
                            onCopyClicked = { onCopyResponse(model, responseState.content.orEmpty()) },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SizeTokens.Level8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCopyPrompt) {
                    Text(text = context.getString(R.string.onCopyButton))
                }
                TextButton(onClick = onSaveAll) {
                    Text(text = context.getString(R.string.onSaveButton))
                }
                TextButton(onClick = onDelete) {
                    Text(text = context.getString(R.string.onDeleteButton))
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = context.timestampToDate(history.timestamp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = SizeTokens.Level4)
                )

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (!expanded) Icons.Rounded.ExpandMore else Icons.Rounded.ExpandLess,
                        contentDescription = null
                    )
                }
            }
        }
    }
}