package killua.dev.aitalk.ui.pages

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import killua.dev.aitalk.R
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
fun HistoryPage() {
    val context = LocalContext.current
    val navController = LocalNavHostController.current!!
    val viewModel: HistoryPageViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val historyList = viewModel.pagedHistory.collectAsLazyPagingItems()

    PrimaryScaffold(
        topBar = {
            HistoryPageTopBar(navController) { intent ->
                scope.launch {
                    viewModel.emitIntent(intent)
                }
            }
        }
    ) {
        Crossfade(
            targetState = historyList.loadState.refresh is LoadState.Loading,
            animationSpec = tween(durationMillis = 500),
            label = "Loading/Content"
        ) { isLoading ->
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (historyList.itemCount == 0) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(SizeTokens.Level6),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = "Nothing to show",
                                modifier = Modifier
                                    .size(SizeTokens.Level72)
                                    .alpha(0.7F)
                            )
                            Text(text = context.getString(R.string.no_history),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8)
                    ) {
                        items(
                            count = historyList.itemCount,
                            key = { index -> historyList.peek(index)?.id ?: index }
                        ) { index ->
                            val history = historyList[index]
                            if (history != null) {
                                HistorypageItemCard(
                                    content = history.prompt,
                                    date = context.timestampToDate(history.timestamp),
                                    onCopyClicked = { scope.launch { viewModel.emitIntent(HistoryPageUIIntent.CopyPrompt(history.prompt)) } },
                                    onSaveClicked = { scope.launch { viewModel.emitIntent(HistoryPageUIIntent.SavePrompt(history.id)) } },
                                    onDeleteClicked = { scope.launch { viewModel.emitIntent(HistoryPageUIIntent.DeleteHistory(history.id)) } },
                                    onClick = { }
                                )
                            }
                        }

                        if (historyList.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}