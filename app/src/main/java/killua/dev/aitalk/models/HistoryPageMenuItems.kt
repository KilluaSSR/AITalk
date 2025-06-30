package killua.dev.aitalk.models

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.ui.graphics.vector.ImageVector
import killua.dev.aitalk.R
import killua.dev.aitalk.ui.viewmodels.HistoryPageUIIntent

data class HistoryPageMenuItems(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val intent: HistoryPageUIIntent
)

val historyPageMenuItems = listOf(
    HistoryPageMenuItems(
        titleRes = R.string.history_clear_all,
        icon = Icons.Rounded.ClearAll,
        intent = HistoryPageUIIntent.DeleteAllHistory
    ),
)