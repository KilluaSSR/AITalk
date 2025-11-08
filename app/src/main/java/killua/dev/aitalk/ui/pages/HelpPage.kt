package killua.dev.aitalk.ui.pages

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import killua.dev.aitalk.R
import killua.dev.aitalk.models.helppageMenuItems
import killua.dev.aitalk.ui.components.ExtendableHelpBox
import killua.dev.aitalk.ui.components.ScrollableScafflod
import killua.dev.aitalk.ui.tokens.SizeTokens
import killua.dev.aitalk.utils.LocalNavHostController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HelpPage() {
    val navController = LocalNavHostController.current!!
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    ScrollableScafflod(
        scrollBehavior = scrollBehavior,
        title = stringResource(R.string.help),
        snackbarHostState = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SizeTokens.Level6)
        ) {

            helppageMenuItems.forEach { helpPageItem ->
                ExtendableHelpBox(
                    title = stringResource(helpPageItem.titleRes),
                    content = stringResource(helpPageItem.contentRes)
                )
            }
        }

    }
}