package killua.dev.aitalk.models

import androidx.annotation.StringRes
import killua.dev.aitalk.R

data class HelpPageItem(
    @StringRes val titleRes: Int,
    @StringRes val contentRes: Int,
)

val helppageMenuItems = listOf(
    HelpPageItem(
        titleRes = R.string.no_api_key_set,
        contentRes = R.string.no_api_key_set_solve,
    ),
    HelpPageItem(
        titleRes = R.string.no_connection,
        contentRes = R.string.no_connection_solve
    ),
)