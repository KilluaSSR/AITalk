package killua.dev.aitalk.ui

sealed class Routes(val route: String){
    data object MainPage : Routes(route = "main_page")
    data object SettingPage : Routes(route = "setting_page")
    data object AboutPage : Routes(route = "about_page")
    data object HelpPage : Routes(route = "help_page")
}