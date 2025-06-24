package killua.dev.aitalk.ui.pages

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.aitalk.R
import killua.dev.aitalk.datastore.SECURE_HISTORY
import killua.dev.aitalk.datastore.readSecureHistory
import killua.dev.aitalk.datastore.readTheme
import killua.dev.aitalk.datastore.writeTheme
import killua.dev.aitalk.ui.SnackbarUIEffect
import killua.dev.aitalk.ui.components.Clickable
import killua.dev.aitalk.ui.components.SettingsScaffold
import killua.dev.aitalk.ui.components.SwitchableSecured
import killua.dev.aitalk.ui.components.ThemeSettingsBottomSheet
import killua.dev.aitalk.ui.components.Title
import killua.dev.aitalk.ui.theme.ThemeMode
import killua.dev.aitalk.ui.theme.getThemeModeName
import killua.dev.aitalk.ui.viewmodels.SettingsUIIntent
import killua.dev.aitalk.ui.viewmodels.SettingsViewmodel
import killua.dev.aitalk.utils.LocalNavHostController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SettingsPage(){
    val context = LocalContext.current
    val navController = LocalNavHostController.current!!
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showThemeMenu by remember { mutableStateOf(false) }
    val currentTheme by context.readTheme()
        .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM.name)
    val viewModel: SettingsViewmodel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val isBiometricAvailable = uiState.value.isBiometricAvailable
    LaunchedEffect(Unit) {
        viewModel.emitIntentOnIO(SettingsUIIntent.onArrive)
    }
    val securedHistoryList by context.readSecureHistory().collectAsStateWithLifecycle(initialValue = false)
    SettingsScaffold(
        scrollBehavior = scrollBehavior,
        title = stringResource(R.string.settings),
        snackbarHostState = null
    ) {
        if (showThemeMenu){
            ThemeSettingsBottomSheet(
                onDismiss = { showThemeMenu = false },
                sheetState = sheetState,
                onThemeSelected = { theme -> context.writeTheme(theme.name) }
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
                    value = getThemeModeName(ThemeMode.valueOf(currentTheme))
                ) {
                    showThemeMenu = true
                }

                SwitchableSecured(
                    key = SECURE_HISTORY,
                    enabled = isBiometricAvailable,
                    title = stringResource(R.string.biometrics),
                    checkedText = when {
                        !isBiometricAvailable -> stringResource(R.string.biometric_auth_disabled_desc)
                        securedHistoryList -> stringResource(R.string.biometric_auth_desc_on)
                        else -> stringResource(R.string.biometric_auth_desc_off)
                    },
                    desc = stringResource(R.string.biometrics_desc),
                ){ errMsg ->
                    scope.launch {
                        viewModel.emitEffect(
                            SnackbarUIEffect.ShowSnackbar(
                                errMsg
                            )
                        )
                    }
                }
            }
        }
    }
}