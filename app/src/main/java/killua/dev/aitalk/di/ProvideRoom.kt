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
    @Provides
    @Singleton
    fun provideHistoryDatabase(@ApplicationContext context: Context): SearchHistoryDatabase =
        Room.databaseBuilder(
            context,
            SearchHistoryDatabase::class.java,
            "search_history.db"
        ).addMigrations(MIGRATION_1_2).build()

    @Provides
    fun provideHistoryDao(db: SearchHistoryDatabase): SearchHistoryDao = db.searchHistoryDao()
}