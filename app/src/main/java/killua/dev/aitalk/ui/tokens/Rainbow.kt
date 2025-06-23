package killua.dev.aitalk.ui.tokens

import androidx.compose.ui.graphics.Color

data class RainbowAnimationConfig(
    val duration: Int = 2000,
    val strokeWidth: Float = 2f,
    val cornerRadius: Float = 12f,
    val colors: List<Color> = listOf(
        Color.Red,
        Color(0xFFFF7F00), // Orange
        Color.Yellow,
        Color.Green,
        Color.Blue,
        Color(0xFF4B0082), // Indigo
        Color(0xFF9400D3)  // Violet
    )
)