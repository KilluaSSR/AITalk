package killua.dev.aitalk.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import killua.dev.aitalk.ui.tokens.SizeTokens

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExtendableHelpBox(
    title: String,
    content: String,
) {
    val context = LocalContext.current
    val shape: Shape = MaterialTheme.shapes.medium
    val colors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    )
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        shape = shape,
        colors = colors,
        modifier = Modifier
            .fillMaxWidth()
            .padding(SizeTokens.Level8)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .clickable { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()

                    .padding(horizontal = SizeTokens.Level16, vertical = SizeTokens.Level12),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.contentColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (!expanded) Icons.Rounded.ExpandMore else Icons.Rounded.ExpandLess,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(
                        start = SizeTokens.Level16,
                        end = SizeTokens.Level16,
                        bottom = SizeTokens.Level16 
                    )
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = SizeTokens.Level12),
                        thickness = SizeTokens.Level1,
                        color = colors.contentColor.copy(alpha = 0.2f)
                    )

                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.contentColor
                    )
                }
            }
        }
    }
}

@Preview
@Composable

fun ExtendableHelpBoxPreview(){
    ExtendableHelpBox("Title", "Content",)
}