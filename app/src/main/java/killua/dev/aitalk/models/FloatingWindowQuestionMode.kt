package killua.dev.aitalk.models

import androidx.annotation.StringRes
import killua.dev.aitalk.R

enum class FloatingWindowQuestionMode(@StringRes val stringRes: Int) {
    isThatTrueYNQuestion(R.string.isThatTrueYNQuestion),
    isThatTrueWithExplain(R.string.isThatTrueWithExplain),
    explainBriefly(R.string.explainBriefly),
    explainVerbose(R.string.explainVerbose),
    translate(R.string.translate)
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
    ),
    FloatingWindowQuestionModeItems(
        stringRes = R.string.translate,
        FloatingWindowQuestionMode.translate
    )

)