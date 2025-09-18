package killua.dev.aitalk.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ConversationEntity): Long

    @Query("UPDATE conversation SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: Long, title: String)

    @Query("UPDATE conversation SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateUpdatedAt(id: Long, updatedAt: Long)

    @Query("UPDATE conversation SET modelSetJson = :json WHERE id = :id")
    suspend fun updateModelSet(id: Long, json: String)

    @Query("UPDATE conversation SET archived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("SELECT * FROM conversation WHERE id = :id")
    fun observeById(id: Long): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversation WHERE id = :id")
    suspend fun getById(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversation WHERE archived = 0 ORDER BY updatedAt DESC LIMIT :limit OFFSET :offset")
    suspend fun listRecent(limit: Int, offset: Int = 0): List<ConversationEntity>
}
