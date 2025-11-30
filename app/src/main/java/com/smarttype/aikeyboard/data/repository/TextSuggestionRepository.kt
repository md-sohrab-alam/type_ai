package com.smarttype.aikeyboard.data.repository

import com.smarttype.aikeyboard.data.database.TextSuggestionDao
import com.smarttype.aikeyboard.data.model.SuggestionCategory
import com.smarttype.aikeyboard.data.model.TextSuggestion
import com.smarttype.aikeyboard.data.remote.OpenAiClient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextSuggestionRepository @Inject constructor(
    private val textSuggestionDao: TextSuggestionDao,
    private val openAiClient: OpenAiClient
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
    
    suspend fun refreshAiSuggestions(
        currentText: String,
        context: String,
        limit: Int
    ): List<String> {
        val aiSuggestions = openAiClient.generateSuggestions(currentText, context, limit)
        if (aiSuggestions.isEmpty()) return emptyList()

        val now = System.currentTimeMillis()
        val entities = aiSuggestions.map { suggestion ->
            TextSuggestion(
                text = suggestion,
                context = context,
                frequency = 1,
                lastUsed = now,
                isCustom = false,
                category = mapContextToCategory(context)
            )
        }
        textSuggestionDao.insertSuggestions(entities)
        return aiSuggestions
    }

    private fun mapContextToCategory(context: String): SuggestionCategory {
        return when (context.lowercase()) {
            "email" -> SuggestionCategory.EMAIL
            "message" -> SuggestionCategory.SOCIAL
            "professional" -> SuggestionCategory.PROFESSIONAL
            else -> SuggestionCategory.GENERAL
        }
    }
}
