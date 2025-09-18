package killua.dev.aitalk.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MessageEntity): Long

    @Query("UPDATE message SET content = :content WHERE id = :id")
    suspend fun updateContent(id: Long, content: String)

    @Query("UPDATE message SET status = :status, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, errorMessage: String? = null)

    @Query("SELECT * FROM message WHERE conversationId = :conversationId ORDER BY ordering ASC")
    fun observeMessages(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM message WHERE conversationId = :conversationId ORDER BY ordering ASC")
    suspend fun getMessagesSnapshot(conversationId: Long): List<MessageEntity>

    @Query("DELETE FROM message WHERE conversationId = :conversationId AND ordering > :orderingThreshold")
    suspend fun deleteAfterOrdering(conversationId: Long, orderingThreshold: Long)

    @Query("SELECT MAX(ordering) FROM message WHERE conversationId = :conversationId")
    suspend fun getMaxOrdering(conversationId: Long): Long?

    @Query("DELETE FROM message WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("SELECT * FROM message WHERE id = :id")
    suspend fun getById(id: Long): MessageEntity?
}
