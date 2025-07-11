package killua.dev.aitalk.repository

import androidx.paging.PagingData
import killua.dev.aitalk.db.SearchHistoryEntity
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.states.AIResponseState
import kotlinx.coroutines.flow.Flow


interface HistoryRepository {
    fun getAllHistory(): Flow<List<SearchHistoryEntity>>
    fun getPagedHistory(): Flow<PagingData<SearchHistoryEntity>>
    suspend fun insertHistoryRecord(prompt: String, modelResponses: Map<AIModel, AIResponseState>)
    suspend fun deleteRecord(id: Long)
    suspend fun getSpecificRecord(id: Long): SearchHistoryEntity?
    suspend fun deleteAll()
    fun searchHistory(query: String): Flow<List<SearchHistoryEntity>>
}

