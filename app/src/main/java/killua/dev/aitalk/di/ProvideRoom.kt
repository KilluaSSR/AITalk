package killua.dev.aitalk.di

import android.content.Context
import androidx.room.Room
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
    @Provides
    @Singleton
    fun provideHistoryDatabase(@ApplicationContext context: Context): SearchHistoryDatabase =
        Room.databaseBuilder(
            context,
            SearchHistoryDatabase::class.java,
            "search_history.db"
        ).build()

    @Provides
    fun provideHistoryDao(db: SearchHistoryDatabase): SearchHistoryDao = db.searchHistoryDao()
}