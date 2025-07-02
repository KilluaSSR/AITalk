package killua.dev.aitalk.models

import killua.dev.aitalk.R

enum class AIModel {
    ChatGPT,
    Claude,
    Gemini,
    DeepSeek,
    Grok
}

enum class SubModel(val parent: AIModel, val displayName: String) {
    // ChatGPT
    GPT_4_1(AIModel.ChatGPT, "GPT-4.1"),
    GPT_4o(AIModel.ChatGPT, "GPT-4o"),
    GPT_o1(AIModel.ChatGPT, "GPT-o1"),
    GPT_o3(AIModel.ChatGPT, "GPT-o3"),
    // Claude
    Claude_Sonnet_3_7(AIModel.Claude, "Claude Sonnet 3.7"),
    Claude_Sonnet_3_5(AIModel.Claude, "Claude Sonnet 3.5"),
    Claude_Sonnet_4(AIModel.Claude, "Claude Sonnet 4"),
    // Gemini
    Gemini_2_5_Flash(AIModel.Gemini, "Gemini-2.5-Flash"),
    Gemini_2_5_Pro(AIModel.Gemini, "Gemini-2.5-Pro"),
    // DeepSeek
    DeepSeek_Chat(AIModel.DeepSeek, "DeepSeek-Chat"),
    DeepSeek_Reasoner(AIModel.DeepSeek, "DeepSeek-Reasoner"),
    // Grok
    Grok_3(AIModel.Grok, "Grok-3"),
    Grok_3_Mini(AIModel.Grok, "Grok-3-Mini"),
    Grok_3_mini_fast(AIModel.Grok, "Grok-3-mini-fast"),
}

fun getSubModelsForModel(model: AIModel): List<SubModel> =
    SubModel.entries.filter { it.parent == model }

fun AIModel.stringRes(): Int = when (this) {
    AIModel.ChatGPT -> R.string.openai_api_settings
    AIModel.Claude -> R.string.claude_api_settings
    AIModel.Gemini -> R.string.gemini_api_settings
    AIModel.DeepSeek -> R.string.deepseek_api_settings
    AIModel.Grok -> R.string.grok_api_settings
}