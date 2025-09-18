package killua.dev.aitalk.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SearchHistoryEntity::class,
        ConversationEntity::class,
        MessageEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SearchHistoryDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}