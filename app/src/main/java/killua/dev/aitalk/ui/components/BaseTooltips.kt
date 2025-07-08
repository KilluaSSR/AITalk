package killua.dev.aitalk.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import killua.dev.aitalk.ui.tokens.SizeTokens

@Composable
fun RichInfoCard(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SizeTokens.Level12),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                start = SizeTokens.Level16,
                end = SizeTokens.Level16,
                top = SizeTokens.Level20,
                bottom = SizeTokens.Level8
            )
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(SizeTokens.Level4))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(SizeTokens.Level12))
            TextButton(
                onClick = onActionClick,
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview
@Composable
fun previewToolTip(){
    RichInfoCard(title = "Title",message = "Message", actionText = "Action",onActionClick = {})
}