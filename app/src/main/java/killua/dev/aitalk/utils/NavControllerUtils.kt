package killua.dev.aitalk.utils

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

val LocalNavHostController: ProvidableCompositionLocal<NavHostController?> =
    staticCompositionLocalOf { null }


fun NavHostController.popBackStackNotNull() = if (previousBackStackEntry != null) popBackStack() else false
fun NavHostController.navigateSingle(route: String) = navigate(route) { popUpTo(route) { inclusive = true } }