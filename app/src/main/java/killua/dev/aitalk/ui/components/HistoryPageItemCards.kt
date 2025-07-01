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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import killua.dev.aitalk.R
import killua.dev.aitalk.ui.tokens.SizeTokens

@Composable
fun HistorypageItemCard(
    content: String,
    date: String,
    onCopyClicked: (() -> Unit),
    onSaveClicked: (() -> Unit),
    onDeleteClicked: (() -> Unit),
    onClick: ()-> Unit,
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
        ) {
            Column(
                modifier = Modifier.padding(SizeTokens.Level16)
            ) {

                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    modifier = Modifier.animateContentSize()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
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

                TextButton(onClick = onDeleteClicked) {
                    Text(
                        text = context.getString(R.string.onDeleteButton),
                        color = MaterialTheme.colorScheme.onSurface)
                }


                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = date,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
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
fun HistorypageItemCardPreview(){
    HistorypageItemCard("Je pense, donc je suis.","2025年5月23日",{},{},{},{})
}