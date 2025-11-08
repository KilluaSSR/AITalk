package killua.dev.aitalk.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import killua.dev.aitalk.R
import killua.dev.aitalk.ui.tokens.SizeTokens

@Composable
fun BaseResponseCardContainer(
    onRegenerateAllClicked: () -> Unit,
    onSaveAllClicked: () -> Unit,
    content: @Composable () -> Unit
){
    val context = LocalContext.current
    val shape: Shape = MaterialTheme.shapes.medium
    val colors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )

    Card(
        shape = shape,
        colors = colors,
        modifier = Modifier
            .fillMaxWidth()
            .padding(SizeTokens.Level6)

    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(start = SizeTokens.Level4),
            ) {

                TextButton(onClick = onSaveAllClicked) {
                    Text(
                        text = context.getString(R.string.onSaveAllButton),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(onClick = onRegenerateAllClicked) {
                    Text(
                        text = context.getString(R.string.onRegenerateAllButton),
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }

            content()
        }
    }
}