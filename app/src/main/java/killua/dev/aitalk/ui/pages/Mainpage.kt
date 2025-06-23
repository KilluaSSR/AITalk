package killua.dev.aitalk.ui.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import killua.dev.aitalk.ui.components.MainpageTextfield
import killua.dev.aitalk.ui.components.MainpageTopBar
import killua.dev.aitalk.ui.components.PrimaryScaffold
import killua.dev.aitalk.ui.viewmodels.MainpageViewModel
import killua.dev.aitalk.utils.LocalNavHostController

@SuppressLint("StateFlowValueCalledInComposition")
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
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ){
            MainpageTextfield(
                uiState = viewModel.uiState.value,
                onIntent = { intent ->
                    viewModel.launchOnIO {
                        viewModel.emitIntent(intent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}