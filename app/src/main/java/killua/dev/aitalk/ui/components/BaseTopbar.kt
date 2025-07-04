package killua.dev.aitalk.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navHostController: NavHostController,
    title: String,
    showUpLeftIcon: Boolean = true,
    upLeftIcon: ImageVector? = null,
    upLeftOnClick: () -> Unit,
    showExtraIcon: Boolean = true,
    extraIconAction: @Composable (RowScope.() -> Unit) = {}
){
    CenterAlignedTopAppBar(
        title = {
            Text(
            title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
        ) },
        navigationIcon = {
            if(showUpLeftIcon && upLeftIcon != null){
                IconButton(
                    onClick = upLeftOnClick
                ) {
                    Icon(
                        imageVector = upLeftIcon,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            if(showExtraIcon){
                extraIconAction()
            }
        }
    )
}