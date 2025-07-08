package killua.dev.aitalk.ui

import killua.dev.aitalk.models.AIModel

sealed class Routes(val route: String){
    data object MainPage : Routes(route = "main_page")
    data object HistoryPage: Routes(route = "history_page")
    data object SettingsPage : Routes(route = "settings_page")
    data object FeedbackPage : Routes(route = "feedback_page")
    data object AboutPage : Routes(route = "about_page")
    data object HelpPage : Routes(route = "help_page")
    data object GrokConfigPage : Routes("grok_config_page")
    data object GeminiConfigPage : Routes("gemini_config_page")
    data object DeepSeekConfigPage : Routes("deepseek_config_page")
    data object OpenAIConfigPage : Routes("openai_config_page")
    data object ClaudeConfigPage : Routes("claude_config_page")

    // New Generic Route for models without special settings
    data object GenericApiConfigPage : Routes("generic_api_config_page/{parentModel}") {
        fun createRoute(parentModel: AIModel) = "generic_api_config_page/${parentModel.name}"
    }

}