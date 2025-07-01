package killua.dev.aitalk.ui.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.aitalk.R
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.SubModel
import killua.dev.aitalk.models.stringRes
import killua.dev.aitalk.ui.components.BaseTextField
import killua.dev.aitalk.ui.components.ConfigurationsScaffold
import killua.dev.aitalk.ui.tokens.SizeTokens
import killua.dev.aitalk.ui.viewmodels.ApiConfigUIIntent
import killua.dev.aitalk.ui.viewmodels.ApiConfigViewModel
import killua.dev.aitalk.utils.LocalNavHostController
import killua.dev.aitalk.utils.paddingBottom
import killua.dev.aitalk.utils.paddingHorizontal
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PageConfigurations(
    parentModel: AIModel,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val viewModel: ApiConfigViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val navController = LocalNavHostController.current!!
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val subModels = remember(parentModel) { SubModel.entries.filter { it.parent == parentModel } }
    var selectedSubModel by remember { mutableStateOf(subModels.firstOrNull()) }

    ConfigurationsScaffold(
        scrollBehavior = scrollBehavior,
        snackbarHostState = viewModel.snackbarHostState,
        title = stringResource(parentModel.stringRes()),
        actions = {
            OutlinedButton(
                onClick = { navController.popBackStack() }
            ) {
                Text(text = stringResource(R.string.cancel))
            }
            Button(
                enabled = selectedSubModel != null,
                onClick = {
                    selectedSubModel?.let { subModel ->
                        scope.launch {
                            viewModel.emitIntent(ApiConfigUIIntent.SaveApiKey(subModel))
                        }
                    }
                }
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    ) {
        LaunchedEffect(parentModel) {
            viewModel.emitIntent(ApiConfigUIIntent.LoadAll(parentModel))
        }

        Crossfade(targetState = uiState.value.isLoading, animationSpec = tween(durationMillis = 500)) { showLoading ->
            if (showLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(SizeTokens.Level16),) {
                    var enabled by remember { mutableStateOf(true) }
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = SizeTokens.Level16)
                            .padding(bottom = SizeTokens.Level16)
                    ) {
                        subModels.forEachIndexed { index, subModel ->
                            SegmentedButton(
                                enabled = enabled,
                                onClick = {
                                    scope.launch {
                                        viewModel.emitIntent(ApiConfigUIIntent.SelectOption(index))
                                    }
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = subModels.size
                                ),
                                selected = (index == uiState.value.optionIndex)
                            ) {
                                Text(
                                    subModel.displayName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    val selectedSubModel = uiState.value.subModels.getOrNull(uiState.value.optionIndex)
                    if (selectedSubModel != null) {
                        AnimatedContent(
                            targetState = selectedSubModel,
                            modifier = Modifier
                                .padding(SizeTokens.Level16)
                        ) { submodel ->
                            BaseTextField(
                                value = uiState.value.apiKeys[submodel] ?: "",
                                onValueChange = { newValue ->
                                    scope.launch {
                                        viewModel.emitIntent(ApiConfigUIIntent.UpdateApiKey(submodel, newValue))
                                    }
                                },
                                label = { Text("${submodel.displayName} ${context.getString(R.string.api_key)}") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = SizeTokens.Level8)
                            )
                        }
                    }

                }
            }
        }
    }
}