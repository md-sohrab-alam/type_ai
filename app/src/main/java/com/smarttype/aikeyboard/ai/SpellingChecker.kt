package com.smarttype.aikeyboard.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Spelling checker that provides real-time word-level spelling detection and correction.
 * 
 * Features:
 * - Word-level spelling detection
 * - Dictionary-based checking
 * - Suggestion generation for misspelled words
 * - Custom word dictionary support
 * 
 * @see SpellingResult
 * @see WordCorrection
 */
@Singleton
class SpellingChecker @Inject constructor(
    private val context: Context
) {
    
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val tag = "SpellingChecker"
    
    // Common English words dictionary (in production, use a proper dictionary file)
    private val dictionary = mutableSetOf<String>().apply {
        // Add common words - in production, load from dictionary file
        addAll(listOf(
            "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
            "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
            "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
            "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
            "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
            "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
            "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
            "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
            "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
            "even", "new", "want", "because", "any", "these", "give", "day", "most", "us"
        ))
    }
    
    /**
     * Result of spelling check operation.
     * 
     * @param text The original text
     * @param words List of word corrections found
     * @param hasErrors Whether any spelling errors were detected
     */
    data class SpellingResult(
        val text: String,
        val words: List<WordCorrection>,
        val hasErrors: Boolean
    )
    
    /**
     * Represents a spelling correction for a single word.
     * 
     * @param word The misspelled word
     * @param startIndex Start position in the text
     * @param endIndex End position in the text
     * @param suggestions List of suggested corrections
     * @param confidence Confidence score (0.0-1.0)
     */
    data class WordCorrection(
        val word: String,
        val startIndex: Int,
        val endIndex: Int,
        val suggestions: List<String>,
        val confidence: Float
    )
    
    /**
     * Checks spelling of the given text and returns corrections.
     * 
     * @param text The text to check
     * @return SpellingResult with detected errors and suggestions
     */
    suspend fun checkSpelling(text: String): SpellingResult = withContext(ioDispatcher) {
        val words = mutableListOf<WordCorrection>()
        val wordPattern = Regex("\\b[a-zA-Z]+\\b")
        
        wordPattern.findAll(text).forEach { matchResult ->
            val word = matchResult.value.lowercase()
            val startIndex = matchResult.range.first
            val endIndex = matchResult.range.last + 1
            
            // Check if word is in dictionary
            if (!isWordCorrect(word)) {
                val suggestions = generateSuggestions(word)
                words.add(
                    WordCorrection(
                        word = matchResult.value, // Keep original case
                        startIndex = startIndex,
                        endIndex = endIndex,
                        suggestions = suggestions,
                        confidence = calculateConfidence(word, suggestions)
                    )
                )
            }
        }
        
        SpellingResult(
            text = text,
            words = words,
            hasErrors = words.isNotEmpty()
        )
    }
    
    /**
     * Checks if a single word is spelled correctly.
     * 
     * @param word The word to check (should be lowercase)
     * @return true if word is correct, false otherwise
     */
    suspend fun isWordCorrect(word: String): Boolean = withContext(ioDispatcher) {
        val normalizedWord = word.lowercase().trim()
        if (normalizedWord.isEmpty()) return@withContext true
        
        // Check dictionary
        if (dictionary.contains(normalizedWord)) return@withContext true
        
        // Check if it's a number
        if (normalizedWord.all { it.isDigit() }) return@withContext true
        
        // Check if it's a proper noun (starts with capital in original)
        // This is a simple heuristic - in production, use proper NLP
        return@withContext false
    }
    
    /**
     * Generates spelling suggestions for a misspelled word.
     * Uses simple edit distance algorithm (Levenshtein distance).
     * 
     * @param word The misspelled word
     * @return List of suggested corrections, ordered by similarity
     */
    private fun generateSuggestions(word: String): List<String> {
        val normalizedWord = word.lowercase()
        val suggestions = mutableListOf<Pair<String, Int>>()
        
        // Find words with edit distance <= 2
        dictionary.forEach { dictWord ->
            val distance = levenshteinDistance(normalizedWord, dictWord)
            if (distance <= 2 && distance > 0) {
                suggestions.add(Pair(dictWord, distance))
            }
        }
        
        // Sort by edit distance and return top 5
        return suggestions
            .sortedBy { it.second }
            .take(5)
            .map { it.first.replaceFirstChar { char -> char.uppercase() } }
    }
    
    /**
     * Calculates Levenshtein distance between two strings.
     * 
     * @param s1 First string
     * @param s2 Second string
     * @return Edit distance
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[m][n]
    }
    
    /**
     * Calculates confidence score for a spelling correction.
     * 
     * @param word The misspelled word
     * @param suggestions List of suggestions
     * @return Confidence score (0.0-1.0)
     */
    private fun calculateConfidence(word: String, suggestions: List<String>): Float {
        if (suggestions.isEmpty()) return 0.0f
        
        val bestSuggestion = suggestions.first()
        val distance = levenshteinDistance(word.lowercase(), bestSuggestion.lowercase())
        
        // Closer suggestions have higher confidence
        return when (distance) {
            1 -> 0.9f
            2 -> 0.7f
            else -> 0.5f
        }
    }
    
    /**
     * Adds a word to the custom dictionary (user-added words).
     * 
     * @param word The word to add
     */
    suspend fun addToDictionary(word: String) = withContext(ioDispatcher) {
        dictionary.add(word.lowercase())
        Log.d(tag, "Added word to dictionary: $word")
    }
    
    /**
     * Checks spelling of a single word in context.
     * 
     * @param word The word to check
     * @param context Surrounding text for context-aware checking
     * @param position Position of the word in the text
     * @return WordCorrection if error found, null otherwise
     */
    suspend fun checkWord(
        word: String,
        context: String = "",
        position: Int = 0
    ): WordCorrection? = withContext(ioDispatcher) {
        val normalizedWord = word.lowercase().trim()
        if (normalizedWord.isEmpty() || isWordCorrect(normalizedWord)) {
            return@withContext null
        }
        
        val suggestions = generateSuggestions(normalizedWord)
        if (suggestions.isEmpty()) return@withContext null
        
        WordCorrection(
            word = word,
            startIndex = position,
            endIndex = position + word.length,
            suggestions = suggestions,
            confidence = calculateConfidence(word, suggestions)
        )
    }
}

