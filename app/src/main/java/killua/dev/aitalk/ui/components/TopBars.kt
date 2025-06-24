package killua.dev.aitalk.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import killua.dev.aitalk.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainpageTopBar(
    navHostController: NavHostController,
    upLeftOnClick: () -> Unit,
    showMoreOnClick: () -> Unit
){
    val context = LocalContext.current
    TopBar(
        navHostController = navHostController,
        title = context.getString(R.string.app_name),
        showUpLeftIcon = true,
        upLeftIcon = Icons.Filled.Menu,
        upLeftOnClick = upLeftOnClick,
        showExtraIcon = true,
        extraIcon = Icons.Filled.ExpandMore,
        showMoreOnClick = showMoreOnClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPageTopBar(
    navHostController: NavHostController,
    upLeftOnClick: () -> Unit,
    showMoreOnClick: () -> Unit
){
    val context = LocalContext.current
    TopBar(
        navHostController = navHostController,
        title = context.getString(R.string.history),
        showUpLeftIcon = true,
        upLeftIcon = Icons.AutoMirrored.Filled.ArrowBack,
        upLeftOnClick = upLeftOnClick,
        showExtraIcon = true,
        extraIcon = Icons.Filled.ExpandMore,
        showMoreOnClick = showMoreOnClick
    )
}