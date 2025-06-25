package killua.dev.aitalk.models

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import killua.dev.aitalk.R

enum class FloatingWindowQuestionMode {
    isThatTrueYNQuestion,
    isThatTrueWithExplain,
    explainBriefly,
    explainVerbose,
}

data class FloatingWindowQuestionModeItems(
    @StringRes val stringRes: Int,
    val mode: FloatingWindowQuestionMode
)

val floatingWindowQuestionModeItemsItems = listOf(
    FloatingWindowQuestionModeItems(
        stringRes = R.string.isThatTrueYNQuestion,
        FloatingWindowQuestionMode.isThatTrueYNQuestion
    ),
    FloatingWindowQuestionModeItems(
        stringRes = R.string.isThatTrueWithExplain,
        FloatingWindowQuestionMode.isThatTrueWithExplain
    ),
    FloatingWindowQuestionModeItems(
        stringRes = R.string.explainBriefly,
        FloatingWindowQuestionMode.explainBriefly
    ),
    FloatingWindowQuestionModeItems(
        stringRes = R.string.explainVerbose,
        FloatingWindowQuestionMode.explainVerbose
    )
)