package killua.dev.aitalk.repository

import killua.dev.aitalk.api.configuration.DeepSeekConfig
import killua.dev.aitalk.api.configuration.GeminiConfig
import killua.dev.aitalk.api.configuration.GrokConfig
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.models.FloatingWindowQuestionMode
import killua.dev.aitalk.models.SubModel
import kotlinx.coroutines.flow.Flow

interface ApiConfigRepository {
    fun getApiKeyForModel(model: AIModel): Flow<String>
    suspend fun setApiKeyForModel(model: AIModel, apiKey: String)

    fun getDefaultSubModelForModel(model: AIModel): Flow<SubModel?>
    suspend fun setDefaultSubModelForModel(model: AIModel, subModel: SubModel)

    fun getFloatingWindowSystemInstruction(model: AIModel, questionMode: FloatingWindowQuestionMode): Flow<String>
    suspend fun setFloatingWindowSystemInstruction(model: AIModel, questionMode: FloatingWindowQuestionMode, instruction: String)

    //Grok
    fun getGrokConfig(): Flow<GrokConfig>
    suspend fun saveGrokSystemInstruction(message: String)

    suspend fun saveGrokTemperature(temperature: Double)

    //Gemini
    fun getGeminiConfig(): Flow<GeminiConfig>
    suspend fun setGeminiTemperature(temperature: Double)
    suspend fun setGeminiTopP(topP: Double)
    suspend fun setGeminiTopK(topK: Int)
    suspend fun setGeminiResponseMimeType(mimeType: String)
    suspend fun setGeminiSystemInstruction(instruction: String)

    //Deepseek
    fun getDeepSeekConfig(): Flow<DeepSeekConfig>
    suspend fun setDeepSeekTemperature(temperature: Double)
    suspend fun setDeepSeekSystemInstruction(instruction: String)
}
