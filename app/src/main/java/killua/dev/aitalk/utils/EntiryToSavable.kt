package killua.dev.aitalk.utils

import killua.dev.aitalk.db.SearchHistoryEntity
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.states.AIResponseState
import killua.dev.aitalk.states.ResponseStatus

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

fun SearchHistoryEntity.toSavableMap(): Map<AIModel, AIResponseState> {
    val map = mutableMapOf<AIModel, AIResponseState>()
    geminiContent?.takeIf { it.isNotBlank() }?.let {
        map[AIModel.Gemini] = AIResponseState(status = ResponseStatus.Success, content = it)
    }
    grokContent?.takeIf { it.isNotBlank() }?.let {
        map[AIModel.Grok] = AIResponseState(status = ResponseStatus.Success, content = it)
    }
    deepSeekContent?.takeIf { it.isNotBlank() }?.let {
        map[AIModel.DeepSeek] = AIResponseState(status = ResponseStatus.Success, content = it)
    }
    claudeContent?.takeIf { it.isNotBlank() }?.let {
        map[AIModel.Claude] = AIResponseState(status = ResponseStatus.Success, content = it)
    }
    chatGPTContent?.takeIf { it.isNotBlank() }?.let {
        map[AIModel.ChatGPT] = AIResponseState(status = ResponseStatus.Success, content = it)
    }

    return map
}