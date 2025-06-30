package killua.dev.aitalk.repository.impl

import killua.dev.aitalk.db.SearchHistoryDao
import killua.dev.aitalk.db.SearchHistoryEntity
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.repository.HistoryRepository
import killua.dev.aitalk.states.AIResponseState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val dao: SearchHistoryDao
) : HistoryRepository {
    override fun getAllHistory(): Flow<List<SearchHistoryEntity>> = dao.observeAll()

    override suspend fun insertHistoryRecord(
        prompt: String,
        modelResponses: Map<AIModel, AIResponseState>
    ) {
        val entity = SearchHistoryEntity(
            prompt = prompt,
            timestamp = System.currentTimeMillis(),
            chatGPTContent = modelResponses[AIModel.ChatGPT]?.content,
            claudeContent = modelResponses[AIModel.Claude]?.content,
            geminiContent = modelResponses[AIModel.Gemini]?.content,
            deepSeekContent = modelResponses[AIModel.DeepSeek]?.content
        )
        dao.insert(entity)
    }

    override suspend fun deleteRecord(id: Long) = dao.deleteById(id)

    override suspend fun deleteAll() {
        dao.deleteAll()
    }
}