package killua.dev.aitalk.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import killua.dev.aitalk.api.DeepSeekApiService
import killua.dev.aitalk.api.DeepSeekApiServiceImpl
import killua.dev.aitalk.api.GeminiApiService
import killua.dev.aitalk.api.GeminiApiServiceImpl
import killua.dev.aitalk.api.GrokApiService
import killua.dev.aitalk.api.GrokApiServiceImpl
import killua.dev.aitalk.api.OpenAIApiService
import killua.dev.aitalk.api.OpenAIApiServiceImpl
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvideAppModules {

    @Provides
    fun provideGeminiApiService(httpClient: OkHttpClient): GeminiApiService =
        GeminiApiServiceImpl(httpClient)

    @Provides
    fun provideGrokApiService(httpClient: OkHttpClient): GrokApiService =
        GrokApiServiceImpl(httpClient)

    @Provides
    fun provideDeepseekApiService(httpClient: OkHttpClient): DeepSeekApiService =
        DeepSeekApiServiceImpl(httpClient)

    @Provides
    fun provideOpenAIApiService(httpClient: OkHttpClient): OpenAIApiService =
        OpenAIApiServiceImpl(httpClient)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()
}