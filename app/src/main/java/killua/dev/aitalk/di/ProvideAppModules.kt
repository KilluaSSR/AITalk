package killua.dev.aitalk.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import killua.dev.aitalk.api.GeminiApiService
import killua.dev.aitalk.api.GeminiApiServiceImpl
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideAppModules {

    @Provides
    fun provideGeminiApiService(httpClient: OkHttpClient): GeminiApiService =
        GeminiApiServiceImpl(httpClient)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()
}