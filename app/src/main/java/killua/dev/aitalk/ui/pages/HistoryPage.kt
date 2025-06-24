package killua.dev.aitalk.ui.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import killua.dev.aitalk.ui.components.HistoryPageTopBar
import killua.dev.aitalk.ui.components.PrimaryScaffold
import killua.dev.aitalk.utils.LocalNavHostController

@Composable
fun HistoryPage(){
    val context = LocalContext.current
    val navController = LocalNavHostController.current!!
    var showMore by remember { mutableStateOf(false) }

    PrimaryScaffold(
        topBar = {
            HistoryPageTopBar (
                navController,
                upLeftOnClick = {
                    navController.popBackStack()
                }
            ) { showMore = true }
        }
    ) {

    }
}