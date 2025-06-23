package killua.dev.aitalk.ui.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import killua.dev.aitalk.ui.components.MainpageTopBar
import killua.dev.aitalk.ui.components.PrimaryScaffold
import killua.dev.aitalk.ui.viewmodels.MainpageViewModel
import killua.dev.aitalk.utils.LocalNavHostController

@Composable
fun Mainpage(){
    val context = LocalContext.current
    val navController = LocalNavHostController.current!!
    var showDrawer by remember { mutableStateOf(false) }
    var showMore by remember { mutableStateOf(false) }
    val viewModel: MainpageViewModel = hiltViewModel()
    PrimaryScaffold(
        topBar = { MainpageTopBar(
            navController,
            upLeftOnClick = {showDrawer = true}
        ) { showMore = true }
        },
        snackbarHostState = viewModel.snackbarHostState
    ) {

    }
}