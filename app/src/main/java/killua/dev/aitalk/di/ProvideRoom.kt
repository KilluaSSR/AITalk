package killua.dev.aitalk.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import killua.dev.aitalk.db.SearchHistoryDao
import killua.dev.aitalk.db.SearchHistoryDatabase
import killua.dev.aitalk.db.ConversationDao
import killua.dev.aitalk.db.MessageDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideRoomModule {
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create indices if they do not exist (IF NOT EXISTS is supported in SQLite for CREATE INDEX)
            db.execSQL("CREATE INDEX IF NOT EXISTS index_search_history_timestamp ON search_history(timestamp)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_search_history_prompt ON search_history(prompt)")
        }
    }
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Conversation table
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS conversation (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "title TEXT, " +
                        "createdAt INTEGER NOT NULL, " +
                        "updatedAt INTEGER NOT NULL, " +
                        "firstUserMessagePreview TEXT, " +
                        "modelSetJson TEXT NOT NULL, " +
                        "archived INTEGER NOT NULL DEFAULT 0)"
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_conversation_updatedAt ON conversation(updatedAt)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_conversation_createdAt ON conversation(createdAt)")

            // Message table
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS message (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "conversationId INTEGER NOT NULL, " +
                        "ordering INTEGER NOT NULL, " +
                        "role TEXT NOT NULL, " +
                        "model TEXT, " +
                        "content TEXT NOT NULL, " +
                        "status TEXT NOT NULL, " +
                        "errorMessage TEXT, " +
                        "revision INTEGER NOT NULL, " +
                        "parentMessageId INTEGER, " +
                        "createdAt INTEGER NOT NULL)"
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_message_conversationId ON message(conversationId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_message_ordering ON message(ordering)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_message_createdAt ON message(createdAt)")
        }
    }
    @Provides
    @Singleton
    fun provideHistoryDatabase(@ApplicationContext context: Context): SearchHistoryDatabase =
        Room.databaseBuilder(
            context,
            SearchHistoryDatabase::class.java,
            "search_history.db"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()

    @Provides
    fun provideHistoryDao(db: SearchHistoryDatabase): SearchHistoryDao = db.searchHistoryDao()

    @Provides
    fun provideConversationDao(db: SearchHistoryDatabase): ConversationDao = db.conversationDao()

    @Provides
    fun provideMessageDao(db: SearchHistoryDatabase): MessageDao = db.messageDao()
}