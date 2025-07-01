package killua.dev.aitalk.ui.pages

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.aitalk.R
import killua.dev.aitalk.datastore.SECURE_HISTORY
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.models.stringRes
import killua.dev.aitalk.ui.Routes
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.components.Clickable
import killua.dev.aitalk.ui.components.ClickableMenu
import killua.dev.aitalk.ui.components.QuestionModeMenu
import killua.dev.aitalk.ui.components.SettingsScaffold
import killua.dev.aitalk.ui.components.SwitchableSecured
import killua.dev.aitalk.ui.components.ThemeModeMenu
import killua.dev.aitalk.ui.components.Title
import killua.dev.aitalk.ui.theme.getThemeModeName
import killua.dev.aitalk.ui.viewmodels.SettingsNavigationEvent
import killua.dev.aitalk.ui.viewmodels.SettingsUIIntent
import killua.dev.aitalk.ui.viewmodels.SettingsViewmodel
import killua.dev.aitalk.utils.LocalNavHostController
import killua.dev.aitalk.utils.getVirtualPathFromTreeUri
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SettingsPage() {
    val context = LocalContext.current
    val navController = LocalNavHostController.current!!
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()
    var showThemeMenu by remember { mutableStateOf(false) }
    var showQuestionModeMenu by remember { mutableStateOf(false) }
    val viewModel: SettingsViewmodel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            scope.launch {
                viewModel.emitIntent(SettingsUIIntent.SaveDirSelected(it.toString()))
            }
        }
    }
    SettingsScaffold(
        scrollBehavior = scrollBehavior,
        title = stringResource(R.string.settings),
        snackbarHostState = null
    ) {
        LaunchedEffect(Unit) {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is SettingsNavigationEvent.NavigateToOverlaySettings -> {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            "package:${context.packageName}".toUri()
                        )
                        context.startActivity(intent)
                    }
                }
            }
        }
        LaunchedEffect(uiState.value.isChoosingDir) {
            if (uiState.value.isChoosingDir) {
                launcher.launch(null)
            }
        }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    scope.launch {
                        viewModel.emitIntent(SettingsUIIntent.OnArrive)
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        Crossfade(
            targetState = uiState.value.isLoading,
            animationSpec = tween(durationMillis = 500)
        ) { showLoading ->
            if (showLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (showThemeMenu) {
                    ThemeModeMenu(
                        expanded = showThemeMenu,
                        onDismiss = { showThemeMenu = false },
                        onThemeSelected = { theme ->
                            scope.launch {
                                viewModel.emitIntent(SettingsUIIntent.UpdateTheme(theme))
                            }
                            showThemeMenu = false
                        }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Title(title = stringResource(R.string.application_settings)) {
                        Clickable(
                            title = stringResource(R.string.theme),
                            value = getThemeModeName(uiState.value.themeMode)
                        ) {
                            showThemeMenu = true
                        }

                        Clickable(
                            title = stringResource(R.string.save_dir),
                            value = getVirtualPathFromTreeUri(uiState.value.saveDir)
                        ) {
                            scope.launch {
                                viewModel.emitIntent(SettingsUIIntent.ChooseSaveDir)
                            }
                        }

                        SwitchableSecured(
                            key = SECURE_HISTORY,
                            enabled = uiState.value.isBiometricAvailable,
                            initValue = uiState.value.isHistorySecured,
                            title = stringResource(R.string.biometrics),
                            checkedText = when {
                                !uiState.value.isBiometricAvailable -> stringResource(R.string.biometric_auth_disabled_desc)
                                uiState.value.isHistorySecured -> stringResource(R.string.biometric_auth_desc_on)
                                else -> stringResource(R.string.biometric_auth_desc_off)
                            }
                        ) { errMsg ->
                            scope.launch {
                                viewModel.emitEffect(
                                    SnackbarUIEffect.ShowSnackbar(
                                        errMsg
                                    )
                                )
                            }
                        }
                    }

                    Title(title = stringResource(R.string.flowting_window_settings)){

                        Clickable(
                            title = stringResource(R.string.overlay_permission),
                            value = if(uiState.value.canDrawOverlays){
                                stringResource(R.string.overlay_permission_on)
                            }else{
                                stringResource(R.string.overlay_permission_off)
                            }
                        ) {
                            scope.launch {
                                viewModel.emitIntent(SettingsUIIntent.GoToOverlaySettingsClicked)
                            }
                        }

                        ClickableMenu(
                            title = stringResource(R.string.flowting_window_search_mode),
                            value = when(uiState.value.questionMode){
                                FloatingWindowQuestionMode.isThatTrueYNQuestion -> stringResource(R.string.isThatTrueYNQuestion)
                                FloatingWindowQuestionMode.isThatTrueWithExplain -> stringResource(R.string.isThatTrueWithExplain)
                                FloatingWindowQuestionMode.explainBriefly -> stringResource(R.string.explainBriefly)
                                FloatingWindowQuestionMode.explainVerbose -> stringResource(R.string.explainVerbose)
                            },
                            content = {
                                QuestionModeMenu(
                                    expanded = showQuestionModeMenu,
                                    onDismissRequest = {showQuestionModeMenu = false},
                                    onSelected = { item ->
                                        scope.launch {
                                            viewModel.emitIntent(SettingsUIIntent.UpdateQuestionMode(item))
                                            showQuestionModeMenu = false
                                        }
                                    },
                                    modifier = Modifier,
                                )
                            }
                        ){
                            showQuestionModeMenu = true
                        }
                    }

                    Title(
                        title = stringResource(R.string.api_settings)
                    ) {
                        AIModel.entries.forEach { model ->
                            Clickable(
                                title = stringResource(model.stringRes()),
                            ) {
                                navController.navigate(Routes.APIConfigurationPage.createRoute(model))
                            }
                        }
                    }
                }
            }
        }

    }
}