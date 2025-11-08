package killua.dev.aitalk.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
@Composable
fun Color.withState(enabled: Boolean = true) = if (enabled) {
    this
} else {
    MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledAlpha)
}

const val DisabledAlpha = 0.38f