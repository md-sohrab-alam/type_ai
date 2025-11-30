package com.smarttype.aikeyboard.di

import android.content.Context
import com.smarttype.aikeyboard.data.remote.OpenAiClient
import com.smarttype.aikeyboard.data.repository.TextSuggestionRepository
import com.smarttype.aikeyboard.data.repository.UserPreferencesRepository
import com.smarttype.aikeyboard.keyboard.VoiceInputManager
import com.smarttype.aikeyboard.keyboard.HapticFeedbackManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        userPreferencesDao: com.smarttype.aikeyboard.data.database.UserPreferencesDao
    ): UserPreferencesRepository {
        return UserPreferencesRepository(userPreferencesDao)
    }
    
    @Provides
    @Singleton
    fun provideTextSuggestionRepository(
        textSuggestionDao: com.smarttype.aikeyboard.data.database.TextSuggestionDao,
        openAiClient: OpenAiClient
    ): TextSuggestionRepository {
        return TextSuggestionRepository(textSuggestionDao, openAiClient)
    }
    
    @Provides
    @Singleton
    fun provideVoiceInputManager(
        @ApplicationContext context: Context
    ): VoiceInputManager {
        return VoiceInputManager(context)
    }
    
    @Provides
    @Singleton
    fun provideHapticFeedbackManager(
        @ApplicationContext context: Context
    ): HapticFeedbackManager {
        return HapticFeedbackManager(context)
    }
}
