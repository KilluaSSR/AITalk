package killua.dev.aitalk.ui.pages

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.aitalk.ui.components.HistoryPageTopBar
import killua.dev.aitalk.ui.components.HistorypageItemCard
import killua.dev.aitalk.ui.components.PrimaryScaffold
import killua.dev.aitalk.ui.tokens.SizeTokens
import killua.dev.aitalk.ui.viewmodels.HistoryPageUIIntent
import killua.dev.aitalk.ui.viewmodels.HistoryPageViewModel
import killua.dev.aitalk.utils.LocalNavHostController
import killua.dev.aitalk.utils.timestampToDate
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
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8)
                ){
                    items(uiState.value.historyList){ history ->
                        HistorypageItemCard(
                            content = history.prompt,
                            date = context.timestampToDate(history.timestamp),
                            {scope.launch { viewModel.emitIntent(HistoryPageUIIntent.CopyPrompt(history.prompt)) }},
                            {scope.launch { viewModel.emitIntent(HistoryPageUIIntent.SavePrompt(history.id)) }},
                            { scope.launch { viewModel.emitIntent(HistoryPageUIIntent.DeleteHistory(history.id)) }
                            },
                        ) { }
                    }
                }
            }
        }
    }
}