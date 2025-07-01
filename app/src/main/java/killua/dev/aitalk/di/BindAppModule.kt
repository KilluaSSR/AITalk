package killua.dev.aitalk.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import killua.dev.aitalk.api.GeminiApiService
import killua.dev.aitalk.repository.AiNetworkRepository
import killua.dev.aitalk.repository.impl.AiNetworkRepositoryImpl
import killua.dev.aitalk.repository.AiRepository
import killua.dev.aitalk.repository.ApiConfigRepository
import killua.dev.aitalk.repository.FileRepository
import killua.dev.aitalk.repository.HistoryRepository
import killua.dev.aitalk.repository.SettingsRepository
import killua.dev.aitalk.repository.impl.AiRepositoryImpl
import killua.dev.aitalk.repository.impl.ApiConfigRepositoryImpl
import killua.dev.aitalk.repository.impl.FileRepositoryImpl
import killua.dev.aitalk.repository.impl.HistoryRepositoryImpl
import killua.dev.aitalk.repository.impl.SettingsRepositoryImpl
import killua.dev.aitalk.utils.ClipboardHelper
import killua.dev.aitalk.utils.ClipboardHelperImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindAppModule {
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(
        impl: AiRepositoryImpl
    ): AiRepository

    @Binds
    @Singleton
    abstract fun bindNetworkRepository(
        impl: AiNetworkRepositoryImpl
    ): AiNetworkRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        impl: HistoryRepositoryImpl
    ): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindApiConfigRepository(
        impl: ApiConfigRepositoryImpl
    ): ApiConfigRepository

    @Binds
    @Singleton
    abstract fun bindFileRepository(
        impl: FileRepositoryImpl
    ): FileRepository

    @Binds
    @Singleton
    abstract fun bindClipboardHelper(
        impl: ClipboardHelperImpl
    ): ClipboardHelper
}