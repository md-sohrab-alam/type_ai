package com.smarttype.aikeyboard.data.repository

import com.smarttype.aikeyboard.data.database.TextSuggestionDao
import com.smarttype.aikeyboard.data.model.SuggestionCategory
import com.smarttype.aikeyboard.data.model.TextSuggestion
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextSuggestionRepository @Inject constructor(
    private val textSuggestionDao: TextSuggestionDao
) {
    
    fun getSuggestionsByContext(context: String, limit: Int): Flow<List<TextSuggestion>> {
        return textSuggestionDao.getSuggestionsByContext(context, limit)
    }
    
    fun getSuggestionsByQuery(query: String, limit: Int): Flow<List<TextSuggestion>> {
        return textSuggestionDao.getSuggestionsByQuery(query, limit)
    }
    
    fun getSuggestionsByCategory(category: SuggestionCategory, limit: Int): Flow<List<TextSuggestion>> {
        return textSuggestionDao.getSuggestionsByCategory(category, limit)
    }
    
    fun getMostUsedSuggestions(limit: Int): Flow<List<TextSuggestion>> {
        return textSuggestionDao.getMostUsedSuggestions(limit)
    }
    
    suspend fun insertSuggestion(suggestion: TextSuggestion) {
        textSuggestionDao.insertSuggestion(suggestion)
    }
    
    suspend fun insertSuggestions(suggestions: List<TextSuggestion>) {
        textSuggestionDao.insertSuggestions(suggestions)
    }
    
    suspend fun updateSuggestion(suggestion: TextSuggestion) {
        textSuggestionDao.updateSuggestion(suggestion)
    }
    
    suspend fun incrementFrequency(text: String) {
        textSuggestionDao.incrementFrequency(text)
    }
    
    suspend fun deleteSuggestion(text: String) {
        textSuggestionDao.deleteSuggestion(text)
    }
    
    suspend fun deleteAllCustomSuggestions() {
        textSuggestionDao.deleteAllCustomSuggestions()
    }
    
    suspend fun getSuggestionCount(): Int {
        return textSuggestionDao.getSuggestionCount()
    }
    
    fun getCustomSuggestions(): Flow<List<TextSuggestion>> {
        return textSuggestionDao.getCustomSuggestions()
    }
    
    suspend fun addCustomSuggestion(text: String, category: SuggestionCategory = SuggestionCategory.GENERAL) {
        val suggestion = TextSuggestion(
            text = text,
            context = "custom",
            isCustom = true,
            category = category
        )
        insertSuggestion(suggestion)
    }
    
    suspend fun getContextualSuggestions(
        currentText: String,
        context: String,
        category: SuggestionCategory? = null,
        limit: Int = 5
    ): List<TextSuggestion> {
        // This would typically involve AI processing
        // For now, return basic suggestions based on current text
        return emptyList()
    }
}
