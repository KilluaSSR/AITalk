package killua.dev.aitalk

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.ui.Routes
import killua.dev.aitalk.ui.pages.HistoryPage
import killua.dev.aitalk.ui.pages.Mainpage
import killua.dev.aitalk.ui.pages.PageConfigurations
import killua.dev.aitalk.ui.pages.SettingsPage
import killua.dev.aitalk.ui.theme.AITalkTheme
import killua.dev.aitalk.ui.theme.ThemeMode
import killua.dev.aitalk.ui.theme.observeThemeMode
import killua.dev.aitalk.utils.AnimatedNavHost
import killua.dev.aitalk.utils.BiometricManagerSingleton
import killua.dev.aitalk.utils.LocalNavHostController

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class,
        ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BiometricManagerSingleton.init(this)
        enableEdgeToEdge()
        setContent {
            val themeMode by this.observeThemeMode()
                .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            AITalkTheme(
                themeMode = themeMode
            ) {
                val navController = rememberNavController()
                CompositionLocalProvider(
                    LocalNavHostController provides navController,
                    LocalLifecycleOwner provides LocalLifecycleOwner.current
                ) {
                    AnimatedNavHost(
                        navController = navController,
                        startDestination = Routes.MainPage.route
                    ) {
                        composable(Routes.MainPage.route) {
                            Mainpage()
                        }
                        composable (Routes.HistoryPage.route){
                            HistoryPage()
                        }
                        composable (Routes.SettingsPage.route){
                            SettingsPage()
                        }
                        composable(
                            route = Routes.APIConfigurationPage.route,
                            arguments = listOf(navArgument("parentModel") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val parentModelName = backStackEntry.arguments?.getString("parentModel") ?: AIModel.ChatGPT.name
                            val parentModel = AIModel.valueOf(parentModelName)
                            PageConfigurations(parentModel = parentModel)
                        }
                    }

                }

            }
        }
    }
}

