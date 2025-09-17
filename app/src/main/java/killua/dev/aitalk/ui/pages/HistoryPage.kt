package killua.dev.aitalk.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import killua.dev.aitalk.R
import killua.dev.aitalk.db.SearchHistoryEntity
import killua.dev.aitalk.ui.components.CancellableAlert
import killua.dev.aitalk.ui.components.HistoryPageTopBar
import killua.dev.aitalk.ui.components.HistorySearchBar
import killua.dev.aitalk.ui.components.HistorypageItemCard
import killua.dev.aitalk.ui.components.PrimaryScaffold
import killua.dev.aitalk.ui.tokens.SizeTokens
import killua.dev.aitalk.ui.viewmodels.HistoryPageUIIntent
import killua.dev.aitalk.ui.viewmodels.HistoryPageViewModel
import killua.dev.aitalk.utils.LocalNavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun HistoryPage() {
    val viewModel: HistoryPageViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val uistate by viewModel.uiState.collectAsStateWithLifecycle()
    val searchSuggestions by viewModel.searchSuggestions.collectAsStateWithLifecycle()
    val pagedSearchResults = viewModel.pagedSearchResults.collectAsLazyPagingItems()
    val historyList = viewModel.pagedHistory.collectAsLazyPagingItems()
    val deletingItemIds by viewModel.deletingItemIds.collectAsState()

    PrimaryScaffold(
        topBar = {
            HistorySearchBar(
                query = uistate.searchQuery,
                onQueryChange = { query -> scope.launch { viewModel.emitIntent(HistoryPageUIIntent.SearchQueryChanged(query)) } },
                onSearch = { scope.launch { viewModel.emitIntent(HistoryPageUIIntent.SubmitSearch) } },
                searchResults = searchSuggestions,
                onResultClick = { resultText ->
                    scope.launch {
                        viewModel.emitIntent(HistoryPageUIIntent.SearchQueryChanged(resultText))
                        viewModel.emitIntent(HistoryPageUIIntent.SubmitSearch)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {

        if (uistate.isDeleteAllDialogVisible) {
            CancellableAlert(
                title = stringResource(R.string.confirm_delete_all_history),
                mainText = stringResource(R.string.confirm_delete_all_history_desc),
                onDismiss = {
                    scope.launch {
                        viewModel.emitState(uistate.copy(isDeleteAllDialogVisible = false))
                    }
                }
            ) {
                scope.launch {
                    viewModel.emitIntent(HistoryPageUIIntent.DeleteAllHistory)
                }
            }
        }

        if (uistate.isSearchActive) {
            PagedHistoryList(
                historyList = pagedSearchResults,
                deletingItemIds = deletingItemIds,
                viewModel = viewModel,
                scope = scope
            )
        } else {
            PagedHistoryList(
                historyList = historyList,
                deletingItemIds = deletingItemIds,
                viewModel = viewModel,
                scope = scope
            )
        }
    }
}

@Composable
private fun PagedHistoryList(
    historyList: LazyPagingItems<SearchHistoryEntity>,
    deletingItemIds: Set<Long>,
    viewModel: HistoryPageViewModel,
    scope: CoroutineScope
) {
    val context = LocalContext.current

    Crossfade(
        targetState = historyList.loadState.refresh is LoadState.Loading,
        animationSpec = tween(durationMillis = 500),
        label = "Loading/Content"
    ) { isLoading ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (historyList.itemCount == 0 && historyList.loadState.refresh is LoadState.NotLoading) {
            EmptyState(
                icon = Icons.Rounded.Warning,
                text = context.getString(R.string.no_history)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                items(historyList.itemCount, key = { index -> historyList.peek(index)?.id ?: index }, contentType = { "history_item" }) { index ->
                    val history = historyList[index]
                    if (history != null) {
                        AnimatedVisibility(
                            visible = history.id !in deletingItemIds,
                            exit = shrinkVertically(tween(350)) + fadeOut(tween(350))
                        ) {
                            HistorypageItemCard(
                                history = history,
                                onCopyPrompt = { scope.launch { viewModel.emitIntent(HistoryPageUIIntent.CopyPrompt(history.prompt)) } },
                                onSaveAll = { scope.launch { viewModel.emitIntent(HistoryPageUIIntent.SavePrompt(history.id)) } },
                                onDelete = { scope.launch { viewModel.emitIntent(HistoryPageUIIntent.DeleteHistory(history.id)) } },
                                onCopyResponse = { _, content -> scope.launch { viewModel.emitIntent(HistoryPageUIIntent.CopyResponse(content)) } }
                            )
                        }
                    }
                }
                if (historyList.loadState.append is LoadState.Loading) {
                    item { LoadingIndicator() }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(icon: ImageVector, text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(72.dp).alpha(0.7F)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}