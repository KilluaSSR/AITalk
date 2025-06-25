package killua.dev.aitalk.ui

import killua.dev.aitalk.models.AIModel

sealed class Routes(val route: String){
    data object MainPage : Routes(route = "main_page")
    data object HistoryPage: Routes(route = "history_page")
    data object SettingsPage : Routes(route = "settings_page")
    data object AboutPage : Routes(route = "about_page")
    data object HelpPage : Routes(route = "help_page")
    data object APIConfigurationPage: Routes("api_configuration_page/{parentModel}") {
        fun createRoute(parentModel: AIModel) = "api_configuration_page/${parentModel.name}"
    }

}