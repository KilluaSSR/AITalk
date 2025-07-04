package killua.dev.aitalk.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.paddingStart(start: Dp) =
    padding(start, 0.dp, 0.dp, 0.dp)

fun Modifier.paddingTop(top: Dp) =
    padding(0.dp, top, 0.dp, 0.dp)

fun Modifier.paddingEnd(end: Dp) =
    padding(0.dp, 0.dp, end, 0.dp)

fun Modifier.paddingBottom(bottom: Dp) =
    padding(0.dp, 0.dp, 0.dp, bottom)

fun Modifier.paddingHorizontal(horizontal: Dp) =
    padding(horizontal, 0.dp)

fun Modifier.paddingVertical(vertical: Dp) =
    padding(0.dp, vertical)

@Composable
fun InnerTopPadding(innerPadding: PaddingValues) {
    Spacer(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(innerPadding.calculateTopPadding())
    )
}

@Composable
fun InnerBottomPadding(innerPadding: PaddingValues) {
    Spacer(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(innerPadding.calculateBottomPadding())
    )
}