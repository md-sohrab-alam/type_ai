package com.smarttype.aikeyboard.di

import com.smarttype.aikeyboard.data.remote.OpenAiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenAiClient(
        okHttpClient: OkHttpClient
    ): OpenAiClient = OpenAiClient(okHttpClient)
}

