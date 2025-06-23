package killua.dev.aitalk.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PrimaryScaffold(
    topBar: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable () -> Unit
){
    Scaffold(
        topBar = topBar,
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = {
            if(snackbarHostState != null) SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ){
            content()
        }
    }
}