package killua.dev.aitalk.ui.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.models.MainpageMenuItems
import killua.dev.aitalk.models.floatingWindowQuestionModeItemsItems
import killua.dev.aitalk.models.historyPageMenuItems
import killua.dev.aitalk.models.mainpageMenuItems
import killua.dev.aitalk.models.supportedLanguageMenuItems
import killua.dev.aitalk.models.themeSettingItems
import killua.dev.aitalk.ui.theme.ThemeMode
import killua.dev.aitalk.ui.viewmodels.HistoryPageUIIntent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainpageMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelected: (MainpageMenuItems) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        mainpageMenuItems.forEach { item ->
            BaseDropdownMenu(
                icon = item.icon,
                text = stringResource(item.titleRes)
            ) {
                scope.launch {
                    onSelected(item)
                    onDismissRequest()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorypageMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelected: (HistoryPageUIIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        historyPageMenuItems.forEach { item ->
            BaseDropdownMenu(
                icon = item.icon,
                text = stringResource(item.titleRes)
            ) {
                scope.launch {
                    onSelected(item.intent)
                    onDismissRequest()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionModeMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelected: (FloatingWindowQuestionMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        floatingWindowQuestionModeItemsItems.forEach { item ->
            BaseDropdownMenu(
                text = stringResource(item.stringRes)
            ) {
                scope.launch {
                    onSelected(item.mode)
                    onDismissRequest()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeModeMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    DropdownMenu(expanded, onDismiss, modifier) {
        themeSettingItems.forEach { item ->
            BottomSheetItem(
                icon = item.icon,
                text = stringResource(item.titleRes)
            ) {
                scope.launch {
                    onThemeSelected(item.mode)
                    onDismiss()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocaleMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        supportedLanguageMenuItems.forEach { item ->
            BaseDropdownMenu(
                text = stringResource(item.nameResId)
            ) {
                scope.launch {
                    onSelected(item.localeTag)
                    onDismissRequest()
                }
            }
        }
    }
}