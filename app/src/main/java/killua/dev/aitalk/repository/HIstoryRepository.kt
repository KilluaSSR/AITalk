package killua.dev.aitalk.repository

import killua.dev.aitalk.db.SearchHistoryEntity
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.states.AIResponseState
import kotlinx.coroutines.flow.Flow


interface HistoryRepository {
    fun getAllHistory(): Flow<List<SearchHistoryEntity>>
    suspend fun insertHistoryRecord(prompt: String, modelResponses: Map<AIModel, AIResponseState>)
    suspend fun deleteRecord(id: Long)

    suspend fun deleteAll()
}

