package killua.dev.aitalk.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable

fun Greetings(){
    val LightBlue = Color(0xFF0066FF)
    val Purple = Color(0xFF800080)
    val gradientColors = listOf(Cyan, LightBlue, Purple)

    Text(
        text = "Hello!",
        style = TextStyle(
            brush = Brush.linearGradient(
                colors = gradientColors
            )
        ),
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold
    )
}