package com.smarttype.aikeyboard.ai

import android.content.Context
import com.smarttype.aikeyboard.data.model.SuggestionCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrammarEngine @Inject constructor(
    private val context: Context
) {
    
    data class GrammarResult(
        val originalText: String,
        val correctedText: String,
        val corrections: List<Correction>,
        val confidence: Float
    )
    
    data class Correction(
        val original: String,
        val corrected: String,
        val type: CorrectionType,
        val startIndex: Int,
        val endIndex: Int,
        val explanation: String
    )
    
    enum class CorrectionType {
        SPELLING,
        GRAMMAR,
        PUNCTUATION,
        STYLE,
        TONE
    }
    
    suspend fun checkGrammar(
        text: String,
        language: String = "en",
        category: SuggestionCategory = SuggestionCategory.GENERAL
    ): GrammarResult = withContext(Dispatchers.IO) {
        
        // Simulate AI processing - in real implementation, this would use
        // TensorFlow Lite models or API calls to AI services
        val corrections = mutableListOf<Correction>()
        var correctedText = text
        
        // Basic spell checking simulation
        val spellCorrections = checkSpelling(text, language)
        corrections.addAll(spellCorrections)
        
        // Grammar checking simulation
        val grammarCorrections = checkGrammarRules(text, language)
        corrections.addAll(grammarCorrections)
        
        // Punctuation checking
        val punctuationCorrections = checkPunctuation(text)
        corrections.addAll(punctuationCorrections)
        
        // Apply corrections
        correctedText = applyCorrections(text, corrections)
        
        GrammarResult(
            originalText = text,
            correctedText = correctedText,
            corrections = corrections,
            confidence = calculateConfidence(corrections)
        )
    }
    
    private fun checkSpelling(text: String, language: String): List<Correction> {
        val corrections = mutableListOf<Correction>()
        
        // Common misspellings dictionary
        val commonMisspellings = mapOf(
            "recieve" to "receive",
            "seperate" to "separate",
            "definately" to "definitely",
            "occured" to "occurred",
            "accomodate" to "accommodate",
            "begining" to "beginning",
            "beleive" to "believe",
            "calender" to "calendar",
            "cemetary" to "cemetery",
            "concious" to "conscious"
        )
        
        commonMisspellings.forEach { (wrong, correct) ->
            val index = text.indexOf(wrong, ignoreCase = true)
            if (index != -1) {
                corrections.add(
                    Correction(
                        original = wrong,
                        corrected = correct,
                        type = CorrectionType.SPELLING,
                        startIndex = index,
                        endIndex = index + wrong.length,
                        explanation = "Common spelling error"
                    )
                )
            }
        }
        
        return corrections
    }
    
    private fun checkGrammarRules(text: String, language: String): List<Correction> {
        val corrections = mutableListOf<Correction>()
        
        // Basic grammar rules
        val grammarRules = listOf(
            "its" to "it's" to "Possessive vs contraction",
            "your" to "you're" to "Possessive vs contraction",
            "there" to "their" to "Homophone confusion",
            "to" to "too" to "Homophone confusion"
        )
        
        // This is simplified - real implementation would use NLP
        grammarRules.forEach { (rule, explanation) ->
            val (wrong, correct) = rule
            val index = text.indexOf(wrong, ignoreCase = true)
            if (index != -1) {
                corrections.add(
                    Correction(
                        original = wrong,
                        corrected = correct,
                        type = CorrectionType.GRAMMAR,
                        startIndex = index,
                        endIndex = index + wrong.length,
                        explanation = explanation
                    )
                )
            }
        }
        
        return corrections
    }
    
    private fun checkPunctuation(text: String): List<Correction> {
        val corrections = mutableListOf<Correction>()
        
        // Check for missing periods at end of sentences
        if (text.isNotEmpty() && !text.endsWith('.') && !text.endsWith('!') && !text.endsWith('?')) {
            corrections.add(
                Correction(
                    original = "",
                    corrected = ".",
                    type = CorrectionType.PUNCTUATION,
                    startIndex = text.length,
                    endIndex = text.length,
                    explanation = "Missing period at end of sentence"
                )
            )
        }
        
        return corrections
    }
    
    private fun applyCorrections(text: String, corrections: List<Correction>): String {
        var result = text
        
        // Sort corrections by start index in reverse order to avoid index shifting
        val sortedCorrections = corrections.sortedByDescending { it.startIndex }
        
        sortedCorrections.forEach { correction ->
            if (correction.startIndex == correction.endIndex) {
                // Insertion
                result = result.substring(0, correction.startIndex) + 
                        correction.corrected + 
                        result.substring(correction.startIndex)
            } else {
                // Replacement
                result = result.substring(0, correction.startIndex) + 
                        correction.corrected + 
                        result.substring(correction.endIndex)
            }
        }
        
        return result
    }
    
    private fun calculateConfidence(corrections: List<Correction>): Float {
        // Simple confidence calculation based on correction types
        return when {
            corrections.isEmpty() -> 1.0f
            corrections.all { it.type == CorrectionType.SPELLING } -> 0.9f
            corrections.any { it.type == CorrectionType.GRAMMAR } -> 0.8f
            else -> 0.7f
        }
    }
    
    suspend fun getSuggestions(
        text: String,
        context: String,
        limit: Int = 5
    ): List<String> = withContext(Dispatchers.IO) {
        
        // Generate contextual suggestions based on text
        val suggestions = mutableListOf<String>()
        
        // Common phrase completions
        val commonPhrases = mapOf(
            "thank you" to listOf("Thank you for your time", "Thank you for the opportunity", "Thank you for your help"),
            "i would like" to listOf("I would like to", "I would like to know", "I would like to discuss"),
            "please let me" to listOf("Please let me know", "Please let me know if", "Please let me know when"),
            "i hope" to listOf("I hope this helps", "I hope you're doing well", "I hope to hear from you soon")
        )
        
        commonPhrases.forEach { (key, values) ->
            if (text.lowercase().contains(key)) {
                suggestions.addAll(values)
            }
        }
        
        return@withContext suggestions.take(limit)
    }
}
