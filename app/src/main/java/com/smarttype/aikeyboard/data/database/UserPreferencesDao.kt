package com.smarttype.aikeyboard.data.database

import androidx.room.*
import com.smarttype.aikeyboard.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {
    
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getUserPreferences(): Flow<UserPreferences?>
    
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getUserPreferencesSync(): UserPreferences?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferences)
    
    @Update
    suspend fun updateUserPreferences(preferences: UserPreferences)
    
    @Query("UPDATE user_preferences SET theme = :theme WHERE id = 1")
    suspend fun updateTheme(theme: String)
    
    @Query("UPDATE user_preferences SET language = :language WHERE id = 1")
    suspend fun updateLanguage(language: String)
    
    @Query("UPDATE user_preferences SET autoCorrect = :enabled WHERE id = 1")
    suspend fun updateAutoCorrect(enabled: Boolean)
    
    @Query("UPDATE user_preferences SET smartPredictions = :enabled WHERE id = 1")
    suspend fun updateSmartPredictions(enabled: Boolean)
    
    @Query("UPDATE user_preferences SET glideTyping = :enabled WHERE id = 1")
    suspend fun updateGlideTyping(enabled: Boolean)
    
    @Query("UPDATE user_preferences SET voiceInput = :enabled WHERE id = 1")
    suspend fun updateVoiceInput(enabled: Boolean)
    
    @Query("UPDATE user_preferences SET hapticFeedback = :enabled WHERE id = 1")
    suspend fun updateHapticFeedback(enabled: Boolean)
    
    @Query("UPDATE user_preferences SET soundFeedback = :enabled WHERE id = 1")
    suspend fun updateSoundFeedback(enabled: Boolean)
    
    @Query("UPDATE user_preferences SET showSuggestions = :enabled WHERE id = 1")
    suspend fun updateShowSuggestions(enabled: Boolean)
    
    @Query("UPDATE user_preferences SET suggestionCount = :count WHERE id = 1")
    suspend fun updateSuggestionCount(count: Int)
    
    @Query("UPDATE user_preferences SET fontSize = :size WHERE id = 1")
    suspend fun updateFontSize(size: Float)
    
    @Query("UPDATE user_preferences SET keyHeight = :height WHERE id = 1")
    suspend fun updateKeyHeight(height: Float)
    
    @Query("UPDATE user_preferences SET keySpacing = :spacing WHERE id = 1")
    suspend fun updateKeySpacing(spacing: Float)
    
    @Query("UPDATE user_preferences SET selectedTheme = :theme WHERE id = 1")
    suspend fun updateSelectedTheme(theme: String)
    
    @Query("UPDATE user_preferences SET aiFeaturesEnabled = :enabled WHERE id = 1")
    suspend fun updateAIFeatures(enabled: Boolean)
    
    @Query("UPDATE user_preferences SET privacyMode = :enabled WHERE id = 1")
    suspend fun updatePrivacyMode(enabled: Boolean)
    
    @Query("UPDATE user_preferences SET syncEnabled = :enabled WHERE id = 1")
    suspend fun updateSyncEnabled(enabled: Boolean)
    
    @Query("UPDATE user_preferences SET lastSyncTime = :time WHERE id = 1")
    suspend fun updateLastSyncTime(time: Long)
}
