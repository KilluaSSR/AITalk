package killua.dev.aitalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import killua.dev.aitalk.ui.pages.Mainpage
import killua.dev.aitalk.ui.theme.AITalkTheme
import killua.dev.aitalk.utils.LocalNavHostController

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AITalkTheme {
                val navController = rememberNavController()
                CompositionLocalProvider(
                    LocalNavHostController provides navController,
                    LocalLifecycleOwner provides LocalLifecycleOwner.current
                ) {
                    Mainpage()
                }

            }
        }
    }
}

