package killua.dev.aitalk.ui.pages

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
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
import killua.dev.aitalk.ui.components.Slideable
import killua.dev.aitalk.ui.components.Title
import killua.dev.aitalk.ui.tokens.PaddingTokens
import killua.dev.aitalk.ui.tokens.SizeTokens
import killua.dev.aitalk.ui.viewmodels.ApiConfigUIIntent
import killua.dev.aitalk.ui.viewmodels.ApiConfigViewModel
import killua.dev.aitalk.utils.LocalNavHostController
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
    val radiusCorner = SizeTokens.Level16
    val shape: Shape = RoundedCornerShape(radiusCorner)

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
                onClick = {
                    scope.launch {
                        viewModel.emitIntent(ApiConfigUIIntent.SaveApiSettings(parentModel))
                    }
                    navController.popBackStack()
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
                    .padding(horizontal = PaddingTokens.Level4)
                    .padding(top = PaddingTokens.Level1) 
                ) {
                    val enabled = true

                    Title(title = stringResource(R.string.default_model)) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PaddingTokens.Level4) 
                                .padding(bottom = PaddingTokens.Level4) 
                                .padding(top = PaddingTokens.Level3)
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
                    }

                    Title(title = stringResource(R.string.api_key_settings)) {
                        BaseTextField(
                            value = uiState.value.apiKey,
                            onValueChange = { newValue ->
                                scope.launch {
                                    viewModel.emitIntent(ApiConfigUIIntent.UpdateApiKey(parentModel, newValue))
                                }
                            },
                            label = { Text("${parentModel.name} ${context.getString(R.string.api_key)}") },
                            shape = shape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PaddingTokens.Level4) 
                                .padding(bottom = PaddingTokens.Level4) 
                        )
                    }

                    when (parentModel) {
                        AIModel.Grok -> {
                            Title(
                                title = stringResource(R.string.system_instruction) 
                            ) {
                                BaseTextField(
                                    value = uiState.value.grokConfig.systemInstruction,
                                    onValueChange = { newValue ->
                                        scope.launch {
                                            viewModel.emitIntent(ApiConfigUIIntent.UpdateGrokSystemInstruction(newValue))
                                        }
                                    },
                                    label = { Text(stringResource(R.string.system_instruction)) }, 
                                    shape = shape,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = PaddingTokens.Level4) 
                                        .padding(bottom = PaddingTokens.Level4) 
                                )
                            }

                            Title(title = stringResource(R.string.parameter_settings)) {
                                Slideable(
                                    title = stringResource(R.string.temperature), 
                                    value = uiState.value.grokConfig.temperature.toFloat(),
                                    valueRange = 0F .. 1F,
                                    steps = 100,
                                    desc = uiState.value.grokConfig.temperature.toString(),
                                    onValueChange = { newValue ->
                                        scope.launch {
                                            viewModel.emitIntent(ApiConfigUIIntent.UpdateGrokTemperature(newValue.toDouble()))
                                        }
                                    }
                                )
                            }
                        }
                        AIModel.Gemini -> {
                            Title(
                                title = stringResource(R.string.system_instruction) 
                            ) {
                                BaseTextField(
                                    value = uiState.value.geminiConfig.systemInstruction,
                                    onValueChange = { newValue ->
                                        scope.launch {
                                            viewModel.emitIntent(ApiConfigUIIntent.UpdateGeminiSystemInstruction(newValue))
                                        }
                                    },
                                    label = { Text(stringResource(R.string.system_instruction)) }, 
                                    shape = shape,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = PaddingTokens.Level4) 
                                        .padding(bottom = PaddingTokens.Level4) 
                                )
                            }

                            Title(title = stringResource(R.string.parameter_settings)) {
                                Slideable(
                                    title = stringResource(R.string.temperature), 
                                    value = uiState.value.geminiConfig.temperature.toFloat(),
                                    valueRange = 0F .. 2F,
                                    steps = 0,
                                    desc = uiState.value.geminiConfig.temperature.toString(),
                                    onValueChange = { newValue ->
                                        scope.launch {
                                            viewModel.emitIntent(ApiConfigUIIntent.UpdateGeminiTemperature(newValue.toDouble()))
                                        }
                                    },
                                )
                                Slideable(
                                    title = stringResource(R.string.top_p), 
                                    value = uiState.value.geminiConfig.topP.toFloat(),
                                    valueRange = 0F .. 1F,
                                    steps = 0,
                                    desc = uiState.value.geminiConfig.topP.toString(),
                                    onValueChange = { newValue ->
                                        scope.launch {
                                            viewModel.emitIntent(ApiConfigUIIntent.UpdateGeminiTopP(newValue.toDouble()))
                                        }
                                    },
                                )
                                BaseTextField(
                                    value = uiState.value.geminiConfig.topK.toString(),
                                    onValueChange = { newValue ->
                                        val intValue = newValue.toIntOrNull() ?: 0
                                        scope.launch {
                                            viewModel.emitIntent(ApiConfigUIIntent.UpdateGeminiTopK(intValue))
                                        }
                                    },
                                    label = { Text(stringResource(R.string.top_k)) }, 
                                    shape = shape,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = PaddingTokens.Level4) 
                                        .padding(bottom = PaddingTokens.Level6)
                                )
                                BaseTextField(
                                    value = uiState.value.geminiConfig.responseMimeType,
                                    onValueChange = { newValue ->
                                        scope.launch {
                                            viewModel.emitIntent(ApiConfigUIIntent.UpdateGeminiResponseMimeType(newValue))
                                        }
                                    },
                                    label = { Text(stringResource(R.string.response_mime_type)) }, 
                                    shape = shape,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = PaddingTokens.Level4) 
                                        .padding(bottom = PaddingTokens.Level6),
                                    enabled = false
                                )
                            }
                        }
                        else -> {
                            // 对于其他模型，目前没有额外配置，保持原样
                        }
                    }
                }
            }
        }
    }
}
