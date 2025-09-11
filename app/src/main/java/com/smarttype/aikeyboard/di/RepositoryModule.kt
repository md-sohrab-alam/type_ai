package com.smarttype.aikeyboard.di

import com.smarttype.aikeyboard.data.repository.TextSuggestionRepository
import com.smarttype.aikeyboard.data.repository.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
        textSuggestionDao: com.smarttype.aikeyboard.data.database.TextSuggestionDao
    ): TextSuggestionRepository {
        return TextSuggestionRepository(textSuggestionDao)
    }
}
