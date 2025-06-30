package killua.dev.aitalk.utils

import killua.dev.aitalk.db.SearchHistoryEntity
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.states.AIResponseState

fun SearchHistoryEntity.toSavable() : Map<AIModel, AIResponseState>{
    val map = mutableMapOf<AIModel, AIResponseState>()
    chatGPTContent?.let {
        map[AIModel.ChatGPT] = AIResponseState(content = it)
    }
    claudeContent?.let {
        map[AIModel.Claude] = AIResponseState(content = it)
    }
    geminiContent?.let {
        map[AIModel.Gemini] = AIResponseState(content = it)
    }
    deepSeekContent?.let {
        map[AIModel.DeepSeek] = AIResponseState(content = it)
    }
    grokContent?.let {
        map[AIModel.Grok] = AIResponseState(content = it)
    }
    return map
}