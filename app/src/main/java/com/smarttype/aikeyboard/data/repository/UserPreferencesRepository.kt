package com.smarttype.aikeyboard.data.repository

import com.smarttype.aikeyboard.data.database.UserPreferencesDao
import com.smarttype.aikeyboard.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao
) {
    
    fun getUserPreferences(): Flow<UserPreferences?> = userPreferencesDao.getUserPreferences()
    
    suspend fun getUserPreferencesSync(): UserPreferences? = userPreferencesDao.getUserPreferencesSync()
    
    suspend fun insertUserPreferences(preferences: UserPreferences) {
        userPreferencesDao.insertUserPreferences(preferences)
    }
    
    suspend fun updateUserPreferences(preferences: UserPreferences) {
        userPreferencesDao.updateUserPreferences(preferences)
    }
    
    suspend fun updateTheme(theme: String) {
        userPreferencesDao.updateTheme(theme)
    }
    
    suspend fun updateLanguage(language: String) {
        userPreferencesDao.updateLanguage(language)
    }
    
    suspend fun updateAutoCorrect(enabled: Boolean) {
        userPreferencesDao.updateAutoCorrect(enabled)
    }
    
    suspend fun updateSmartPredictions(enabled: Boolean) {
        userPreferencesDao.updateSmartPredictions(enabled)
    }
    
    suspend fun updateGlideTyping(enabled: Boolean) {
        userPreferencesDao.updateGlideTyping(enabled)
    }
    
    suspend fun updateVoiceInput(enabled: Boolean) {
        userPreferencesDao.updateVoiceInput(enabled)
    }
    
    suspend fun updateHapticFeedback(enabled: Boolean) {
        userPreferencesDao.updateHapticFeedback(enabled)
    }
    
    suspend fun updateSoundFeedback(enabled: Boolean) {
        userPreferencesDao.updateSoundFeedback(enabled)
    }
    
    suspend fun updateShowSuggestions(enabled: Boolean) {
        userPreferencesDao.updateShowSuggestions(enabled)
    }
    
    suspend fun updateSuggestionCount(count: Int) {
        userPreferencesDao.updateSuggestionCount(count)
    }
    
    suspend fun updateFontSize(size: Float) {
        userPreferencesDao.updateFontSize(size)
    }
    
    suspend fun updateKeyHeight(height: Float) {
        userPreferencesDao.updateKeyHeight(height)
    }
    
    suspend fun updateKeySpacing(spacing: Float) {
        userPreferencesDao.updateKeySpacing(spacing)
    }
    
    suspend fun updateSelectedTheme(theme: String) {
        userPreferencesDao.updateSelectedTheme(theme)
    }
    
    suspend fun updateAIFeatures(enabled: Boolean) {
        userPreferencesDao.updateAIFeatures(enabled)
    }
    
    suspend fun updatePrivacyMode(enabled: Boolean) {
        userPreferencesDao.updatePrivacyMode(enabled)
    }
    
    suspend fun updateSyncEnabled(enabled: Boolean) {
        userPreferencesDao.updateSyncEnabled(enabled)
    }
    
    suspend fun updateLastSyncTime(time: Long) {
        userPreferencesDao.updateLastSyncTime(time)
    }
    
    suspend fun getDefaultPreferences(): UserPreferences {
        return userPreferencesDao.getUserPreferencesSync() ?: UserPreferences()
    }
}
