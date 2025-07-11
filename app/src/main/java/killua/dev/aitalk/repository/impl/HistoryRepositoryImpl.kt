package killua.dev.aitalk.repository.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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
    override fun getPagedHistory(): Flow<PagingData<SearchHistoryEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            pagingSourceFactory = { dao.getPagedHistory() }
        ).flow
    }
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
    override suspend fun getSpecificRecord(id: Long): SearchHistoryEntity? {
        return dao.getById(id)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }
    override fun searchHistory(query: String): Flow<List<SearchHistoryEntity>> {
        return dao.searchHistory(query)
    }
}