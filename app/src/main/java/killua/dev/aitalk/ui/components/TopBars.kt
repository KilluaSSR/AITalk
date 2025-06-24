package killua.dev.aitalk.ui.components

import android.text.Layout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import killua.dev.aitalk.R
import killua.dev.aitalk.utils.LocalNavHostController
import killua.dev.aitalk.utils.navigateSingle
import killua.dev.aitalk.utils.popBackStackNotNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainpageTopBar(
    navHostController: NavHostController,
    upLeftOnClick: () -> Unit,
){
    var expandedMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val navController = LocalNavHostController.current!!
    TopBar(
        navHostController = navHostController,
        title = context.getString(R.string.app_name),
        showUpLeftIcon = true,
        upLeftIcon = Icons.Filled.Menu,
        upLeftOnClick = upLeftOnClick,
        showExtraIcon = true,
        extraIconAction = {
            IconButton (
                onClick = {expandedMenu = true}
            ){
                Icon(Icons.Rounded.ExpandMore, null)
            }

            MainpageMenu(
                expanded = expandedMenu,
                onDismissRequest = {expandedMenu = false},
                onSelected = { item ->
                    navController.navigateSingle(item.route)
                },
                modifier = Modifier,
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPageTopBar(
    navHostController: NavHostController,
    onBackClick: (() -> Unit)? = null,
    showMoreOnClick: () -> Unit
){
    val context = LocalContext.current
    val navController = LocalNavHostController.current!!
    TopBar(
        navHostController = navHostController,
        title = context.getString(R.string.history),
        showUpLeftIcon = true,
        upLeftIcon = Icons.AutoMirrored.Filled.ArrowBack,
        upLeftOnClick = {
            if (onBackClick != null) onBackClick.invoke()
            else navController.popBackStackNotNull()
        },
        showExtraIcon = true,
        extraIconAction = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(
    scrollBehavior: TopAppBarScrollBehavior?,
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    onBackClick: (() -> Unit)? = null
){
    val navController = LocalNavHostController.current!!
    LargeTopAppBar(
        title = { Text(text = title) },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            ArrowBackButton {
                if (onBackClick != null) onBackClick.invoke()
                else navController.popBackStackNotNull()
            }
        },
        actions = actions,
    )
}