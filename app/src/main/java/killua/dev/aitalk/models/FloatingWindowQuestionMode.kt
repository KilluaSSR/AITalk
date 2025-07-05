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