package killua.dev.aitalk.ui.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.models.FloatingWindowQuestionModeItems
import killua.dev.aitalk.models.MainpageMenuItems
import killua.dev.aitalk.models.floatingWindowQuestionModeItemsItems
import killua.dev.aitalk.models.mainpageMeunItems
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
        mainpageMeunItems.forEach { item ->
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