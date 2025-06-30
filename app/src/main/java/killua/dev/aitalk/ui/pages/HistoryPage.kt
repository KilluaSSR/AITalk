package killua.dev.aitalk.ui.pages

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.aitalk.ui.components.HistoryPageTopBar
import killua.dev.aitalk.ui.components.PrimaryScaffold
import killua.dev.aitalk.ui.viewmodels.HistoryPageViewModel
import killua.dev.aitalk.utils.LocalNavHostController
import kotlinx.coroutines.launch

@Composable
fun HistoryPage(){
    val context = LocalContext.current
    val navController = LocalNavHostController.current!!
    val viewModel: HistoryPageViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    PrimaryScaffold(
        topBar = {
            HistoryPageTopBar (
                navController
            ){intent ->
                scope.launch {
                    viewModel.emitIntent(intent)
                }
            }
        }
    ) {
        Crossfade(
            targetState = uiState.value.isLoading,
            animationSpec= tween(durationMillis = 500)
        ) { isLoading->
            if(isLoading){
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }else{

            }
        }
    }
}