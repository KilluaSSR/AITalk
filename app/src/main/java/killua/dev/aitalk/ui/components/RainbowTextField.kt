package killua.dev.aitalk.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import killua.dev.aitalk.ui.effects.RainbowBorderTransformation
import killua.dev.aitalk.ui.effects.drawRainbowBorder
import killua.dev.aitalk.ui.tokens.SizeTokens

@Composable
fun RainbowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    ),
    shape: Shape = RoundedCornerShape(SizeTokens.Level24),
    animationDurationMs: Int = 500,
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var animationCompleted by remember { mutableStateOf(false) }
    val rainbowProgress by animateFloatAsState(
        targetValue = if (isFocused && !animationCompleted) 1f else 0f,
        animationSpec = tween(animationDurationMs, easing = LinearEasing),
        finishedListener = { value ->
            if (isFocused && value >= 0.99f) {
                animationCompleted = true
            }
        }
    )

    val cornerRadius = SizeTokens.Level24

    LaunchedEffect(isFocused) {
        if (!isFocused) {
            animationCompleted = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ){
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    if (focusState.isFocused) {
                        animationCompleted = false
                    }
                }
                .drawBehind {
                    if (isFocused) {
                        drawRainbowBorder(cornerRadius, if (animationCompleted) 1f else rainbowProgress)
                    }
                },
            placeholder = placeholder,
            trailingIcon = trailingIcon,
            enabled = enabled,
            shape = shape,
            colors = colors,
            visualTransformation = RainbowBorderTransformation(isFocused, rainbowProgress)
        )
    }
}