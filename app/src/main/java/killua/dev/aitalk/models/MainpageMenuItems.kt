package killua.dev.aitalk.models

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import killua.dev.aitalk.R
import killua.dev.aitalk.ui.Routes


data class MainpageMenuItems(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val route: String
)

val mainpageMenuItems = listOf(
    MainpageMenuItems(
        titleRes = R.string.settings,
        icon = Icons.Rounded.Settings,
        Routes.SettingsPage.route
    ),
    MainpageMenuItems(
        titleRes = R.string.feedback,
        icon = Icons.Rounded.Report,
        Routes.HelpPage.route
    ),
    MainpageMenuItems(
        titleRes = R.string.about,
        icon = Icons.AutoMirrored.Rounded.Help,
        Routes.AboutPage.route
    )
)