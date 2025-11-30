package com.smarttype.aikeyboard.data.database

import androidx.room.*
import com.smarttype.aikeyboard.data.model.SuggestionCategory
import com.smarttype.aikeyboard.data.model.TextSuggestion
import kotlinx.coroutines.flow.Flow

@Dao
interface TextSuggestionDao {
    
    @Query("SELECT * FROM text_suggestions WHERE context = :context ORDER BY frequency DESC, lastUsed DESC LIMIT :limit")
    fun getSuggestionsByContext(context: String, limit: Int): Flow<List<TextSuggestion>>
    
    @Query("SELECT * FROM text_suggestions WHERE text LIKE :query || '%' ORDER BY frequency DESC, lastUsed DESC LIMIT :limit")
    fun getSuggestionsByQuery(query: String, limit: Int): Flow<List<TextSuggestion>>
    
    @Query("SELECT * FROM text_suggestions WHERE category = :category ORDER BY frequency DESC, lastUsed DESC LIMIT :limit")
    fun getSuggestionsByCategory(category: SuggestionCategory, limit: Int): Flow<List<TextSuggestion>>
    
    @Query("SELECT * FROM text_suggestions ORDER BY frequency DESC, lastUsed DESC LIMIT :limit")
    fun getMostUsedSuggestions(limit: Int): Flow<List<TextSuggestion>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestion(suggestion: TextSuggestion)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestions(suggestions: List<TextSuggestion>)
    
    @Update
    suspend fun updateSuggestion(suggestion: TextSuggestion)
    
    @Query("UPDATE text_suggestions SET frequency = frequency + 1, lastUsed = :currentTime WHERE text = :text")
    suspend fun incrementFrequency(text: String, currentTime: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM text_suggestions WHERE text = :text")
    suspend fun deleteSuggestion(text: String)
    
    @Query("DELETE FROM text_suggestions WHERE isCustom = 1")
    suspend fun deleteAllCustomSuggestions()
    
    @Query("SELECT COUNT(*) FROM text_suggestions")
    suspend fun getSuggestionCount(): Int
    
    @Query("SELECT * FROM text_suggestions WHERE isCustom = 1")
    fun getCustomSuggestions(): Flow<List<TextSuggestion>>
}
