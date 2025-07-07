package killua.dev.aitalk.ui.pages.ConfigPages
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import killua.dev.aitalk.ui.components.PreferenceSwitchWithContainer
import killua.dev.aitalk.ui.components.Slideable
import killua.dev.aitalk.ui.components.Title
import killua.dev.aitalk.ui.tokens.PaddingTokens
import killua.dev.aitalk.ui.tokens.SizeTokens
import killua.dev.aitalk.ui.viewmodels.DeepSeekConfigUIIntent
import killua.dev.aitalk.ui.viewmodels.DeepSeekConfigViewModel
import killua.dev.aitalk.utils.LocalNavHostController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DeepSeekConfigPage() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val viewModel: DeepSeekConfigViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val navController = LocalNavHostController.current!!
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val parentModel = AIModel.DeepSeek
    val subModels = SubModel.entries.filter { it.parent == parentModel }
    val radiusCorner = SizeTokens.Level16
    val shape: Shape = RoundedCornerShape(radiusCorner)


    ConfigurationsScaffold(
        scrollBehavior = scrollBehavior,
        snackbarHostState = viewModel.snackbarHostState,
        title = stringResource(parentModel.stringRes()),
        actions = {
            OutlinedButton(onClick = { navController.popBackStack() }) {
                Text(text = stringResource(R.string.cancel))
            }
            Button(
                onClick = {
                    scope.launch { viewModel.emitIntent(DeepSeekConfigUIIntent.SaveSettings) }
                    navController.popBackStack()
                }
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    ) { innerPadding ->
        Crossfade(targetState = uiState.value.isLoading, animationSpec = tween(durationMillis = 500)) { showLoading ->
            if (showLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    PreferenceSwitchWithContainer(
                        title = stringResource(R.string.toogle_model),
                        icon = null,
                        isChecked = uiState.value.isModelEnabled ,
                    ) {
                        scope.launch {
                            val newCheckedState = !uiState.value.isModelEnabled
                            viewModel.emitIntent(DeepSeekConfigUIIntent.ToggleModelEnabled(newCheckedState))
                        }
                    }
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
                                    onClick = { scope.launch { viewModel.emitIntent(DeepSeekConfigUIIntent.SelectOption(index)) } },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = subModels.size),
                                    selected = (index == uiState.value.optionIndex)
                                ) {
                                    Text(subModel.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }

                    Title(title = stringResource(R.string.api_key_settings)) {
                        BaseTextField(
                            value = uiState.value.apiKey,
                            onValueChange = { scope.launch { viewModel.emitIntent(DeepSeekConfigUIIntent.UpdateApiKey(it)) } },
                            label = { Text("${parentModel.name} ${context.getString(R.string.api_key)}") },
                            shape = shape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PaddingTokens.Level4)
                                .padding(bottom = PaddingTokens.Level4)
                        )
                    }

                    Title(title = stringResource(R.string.floating_window_system_instruction_title)) {
                        BaseTextField(
                            value = uiState.value.editableFloatingWindowSystemInstruction,
                            onValueChange = { scope.launch { viewModel.emitIntent(DeepSeekConfigUIIntent.UpdateFloatingWindowSystemInstruction(it)) } },
                            label = { Text(stringResource(uiState.value.currentFloatingWindowQuestionMode.stringRes)) },
                            shape = shape,
                            singleLine = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PaddingTokens.Level4)
                                .padding(bottom = PaddingTokens.Level4)
                        )
                    }

                    Title(title = stringResource(R.string.system_instruction)) {
                        BaseTextField(
                            value = uiState.value.deepSeekConfig.systemInstruction,
                            onValueChange = { scope.launch { viewModel.emitIntent(DeepSeekConfigUIIntent.UpdateDeepSeekSystemInstruction(it)) } },
                            label = { Text(stringResource(R.string.system_instruction)) },
                            shape = shape,
                            singleLine = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PaddingTokens.Level4)
                                .padding(bottom = PaddingTokens.Level4)
                        )
                    }

                    Title(title = stringResource(R.string.parameter_settings)) {
                        Slideable(
                            title = stringResource(R.string.temperature),
                            value = uiState.value.deepSeekConfig.temperature.toFloat(),
                            valueRange = 0F..2F,
                            steps = 0,
                            desc = uiState.value.deepSeekConfig.temperature.toString(),
                            onValueChange = { scope.launch { viewModel.emitIntent(DeepSeekConfigUIIntent.UpdateDeepSeekTemperature(it.toDouble())) } },
                        )
                    }
                }
            }
        }
    }
}