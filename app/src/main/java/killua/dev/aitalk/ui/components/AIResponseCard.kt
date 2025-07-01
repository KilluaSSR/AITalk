package killua.dev.aitalk.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
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
import killua.dev.aitalk.states.AIResponseState
import killua.dev.aitalk.states.ResponseStatus
import killua.dev.aitalk.ui.effects.shimmerEffect
import killua.dev.aitalk.ui.tokens.SizeTokens

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AIResponseCard(
    responseState: AIResponseState,
    modelName: String,
    onCopyClicked: (() -> Unit),
    onSaveClicked: (() -> Unit),
    onRegenerateClicked: (() -> Unit),
){
    val context = LocalContext.current
    val shape: Shape = MaterialTheme.shapes.medium
    val colors = when(responseState.status){
        ResponseStatus.Error -> {
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
        else ->{
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
    val isSearching = !(responseState.status == ResponseStatus.Success || responseState.status == ResponseStatus.Error)
    val modifierShimmer: Modifier =
        if (isSearching) {
            Modifier
                .fillMaxWidth()
                .shimmerEffect()
        } else {
            Modifier
                .fillMaxWidth()
        }
    var expanded by remember { mutableStateOf(false) }
    ElevatedCard (
        shape = shape,
        colors = colors,
        modifier = Modifier
            .fillMaxWidth()
            .padding(SizeTokens.Level8)

    ){
        Column(
            modifier = modifierShimmer
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
                AnimatedContent(
                    targetState = responseState.status
                ) { status->
                    if(status == ResponseStatus.Success){
                        AnimatedTextContainer(
                            targetState = responseState.content!!
                        ) { text ->
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = if (expanded) Int.MAX_VALUE else 3,
                                modifier = Modifier.animateContentSize()
                            )
                        }
                    }else if(status == ResponseStatus.Error){
                        AnimatedTextContainer(
                            targetState = responseState.errorMessage.toString()
                        ) { text ->
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = if (expanded) Int.MAX_VALUE else 3,
                                modifier = Modifier.animateContentSize()
                            )
                        }
                    }
                }

            }

            AnimatedContent(
                targetState = responseState.status,
            ) { state ->
                if(state == ResponseStatus.Success){
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
    }
}


@Preview
@Composable
fun AIResponseCardPreview(){
    AIResponseCard(
        AIResponseState(status = ResponseStatus.Error),
        "Gemini",
        onCopyClicked = {},
        onSaveClicked = {},
        onRegenerateClicked = {},
    )
}