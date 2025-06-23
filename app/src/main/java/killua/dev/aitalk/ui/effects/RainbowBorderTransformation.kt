package killua.dev.aitalk.ui.effects

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import killua.dev.aitalk.ui.tokens.SizeTokens
import kotlin.math.PI

class RainbowBorderTransformation(
    private val isFocused: Boolean,
    private val animationProgress: Float
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(text, OffsetMapping.Identity)
    }
}

fun DrawScope.drawRainbowBorder(cornerRadius: Dp, animationProgress: Float) {
    val strokeWidth = SizeTokens.Level2.toPx()
    val cornerRadiusPx = cornerRadius.toPx()

    val colors = listOf(
        Color.Red, Color(0xFFFF7F00), Color.Yellow,
        Color.Green, Color.Blue, Color(0xFF4B0082), Color(0xFF9400D3)
    )

    val brush = Brush.sweepGradient(
        colors = colors + colors.first(),
        center = Offset(size.width / 2, size.height / 2)
    )

    val path = androidx.compose.ui.graphics.Path()

    val rectWidth = size.width - strokeWidth
    val rectHeight = size.height - strokeWidth
    val totalLength = 2 * (rectWidth + rectHeight - 4 * cornerRadiusPx) + 2 * PI.toFloat() * cornerRadiusPx
    val currentLength = totalLength * animationProgress

    path.addRoundRect(
        RoundRect(
            left = strokeWidth / 2,
            top = strokeWidth / 2,
            right = size.width - strokeWidth / 2,
            bottom = size.height - strokeWidth / 2,
            cornerRadius = CornerRadius(cornerRadiusPx)
        )
    )

    val pathEffect = PathEffect.dashPathEffect(
        floatArrayOf(currentLength, totalLength - currentLength),
        0f
    )

    drawPath(
        path = path,
        brush = brush,
        style = Stroke(
            width = strokeWidth,
            pathEffect = pathEffect
        )
    )
}