package com.smarttype.aikeyboard.ai

import android.content.Context
import com.smarttype.aikeyboard.data.model.ToneStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tone transformation engine that modifies text to match different writing styles.
 * 
 * Supports 15+ tone styles including:
 * - Professional, Casual, Polite, Friendly
 * - Confident, Empathetic, Persuasive
 * - Concise, Detailed, Creative, Technical
 * - Romantic, Humorous, Sarcastic, Gen Z
 * 
 * The engine uses rule-based transformations (word replacements, contractions, etc.)
 * and can be extended with AI models for more sophisticated tone adaptation.
 * 
 * @see ToneStyle
 * @see ToneResult
 */
@Singleton
class ToneEngine @Inject constructor(
    private val context: Context
) {
    
    /**
     * Result of tone transformation operation.
     * 
     * @param originalText The original input text
     * @param transformedText The text after tone transformation
     * @param tone The target tone style that was applied
     * @param confidence Confidence score (0.0-1.0) for the transformation
     * @param changes List of individual changes made during transformation
     */
    data class ToneResult(
        val originalText: String,
        val transformedText: String,
        val tone: ToneStyle,
        val confidence: Float,
        val changes: List<ToneChange>
    )
    
    /**
     * Represents a single change made during tone transformation.
     * 
     * @param original The original text/phrase
     * @param transformed The transformed text/phrase
     * @param reason Explanation of why this change was made
     */
    data class ToneChange(
        val original: String,
        val transformed: String,
        val reason: String
    )
    
    /**
     * Transforms text to match the target tone style.
     * 
     * @param text The text to transform
     * @param targetTone The desired tone style
     * @param language Language code (default: "en" for English)
     * @return ToneResult containing the transformed text and change details
     */
    suspend fun transformTone(
        text: String,
        targetTone: ToneStyle,
        language: String = "en"
    ): ToneResult = withContext(Dispatchers.IO) {
        
        val changes = mutableListOf<ToneChange>()
        var transformedText = text
        
        when (targetTone.id) {
            "professional" -> {
                transformedText = makeProfessional(text, changes)
            }
            "casual" -> {
                transformedText = makeCasual(text, changes)
            }
            "polite" -> {
                transformedText = makePolite(text, changes)
            }
            "friendly" -> {
                transformedText = makeFriendly(text, changes)
            }
            "confident" -> {
                transformedText = makeConfident(text, changes)
            }
            "empathetic" -> {
                transformedText = makeEmpathetic(text, changes)
            }
            "persuasive" -> {
                transformedText = makePersuasive(text, changes)
            }
            "concise" -> {
                transformedText = makeConcise(text, changes)
            }
            "detailed" -> {
                transformedText = makeDetailed(text, changes)
            }
            "creative" -> {
                transformedText = makeCreative(text, changes)
            }
            "technical" -> {
                transformedText = makeTechnical(text, changes)
            }
            "romantic" -> {
                transformedText = makeRomantic(text, changes)
            }
            "humorous" -> {
                transformedText = makeHumorous(text, changes)
            }
            "sarcasm" -> {
                transformedText = makeSarcastic(text, changes)
            }
            "gen_z" -> {
                transformedText = makeGenZ(text, changes)
            }
        }
        
        ToneResult(
            originalText = text,
            transformedText = transformedText,
            tone = targetTone,
            confidence = calculateToneConfidence(text, transformedText),
            changes = changes
        )
    }
    
    private fun makeProfessional(text: String, changes: MutableList<ToneChange>): String {
        var result = text
        
        // Remove contractions
        val contractions = mapOf(
            "don't" to "do not",
            "won't" to "will not",
            "can't" to "cannot",
            "isn't" to "is not",
            "aren't" to "are not",
            "wasn't" to "was not",
            "weren't" to "were not",
            "haven't" to "have not",
            "hasn't" to "has not",
            "hadn't" to "had not",
            "wouldn't" to "would not",
            "couldn't" to "could not",
            "shouldn't" to "should not"
        )
        
        contractions.forEach { (contraction, full) ->
            if (result.contains(contraction, ignoreCase = true)) {
                result = result.replace(contraction, full, ignoreCase = true)
                changes.add(ToneChange(contraction, full, "Professional tone requires full words"))
            }
        }
        
        // Add formal greetings
        if (result.startsWith("hi", ignoreCase = true)) {
            result = result.replaceFirst("hi", "Hello", ignoreCase = true)
            changes.add(ToneChange("hi", "Hello", "Professional greeting"))
        }
        
        return result
    }
    
    private fun makeCasual(text: String, changes: MutableList<ToneChange>): String {
        var result = text
        
        // Add contractions
        val contractions = mapOf(
            "do not" to "don't",
            "will not" to "won't",
            "cannot" to "can't",
            "is not" to "isn't",
            "are not" to "aren't",
            "was not" to "wasn't",
            "were not" to "weren't",
            "have not" to "haven't",
            "has not" to "hasn't",
            "had not" to "hadn't",
            "would not" to "wouldn't",
            "could not" to "couldn't",
            "should not" to "shouldn't"
        )
        
        contractions.forEach { (full, contraction) ->
            if (result.contains(full, ignoreCase = true)) {
                result = result.replace(full, contraction, ignoreCase = true)
                changes.add(ToneChange(full, contraction, "Casual tone uses contractions"))
            }
        }
        
        // Make greetings more casual
        if (result.startsWith("Hello", ignoreCase = true)) {
            result = result.replaceFirst("Hello", "Hey", ignoreCase = true)
            changes.add(ToneChange("Hello", "Hey", "Casual greeting"))
        }
        
        return result
    }
    
    private fun makePolite(text: String, changes: MutableList<ToneChange>): String {
        var result = text
        
        // Add polite words
        if (!result.contains("please", ignoreCase = true) && result.contains("can you", ignoreCase = true)) {
            result = result.replace("can you", "Could you please", ignoreCase = true)
            changes.add(ToneChange("can you", "Could you please", "More polite request"))
        }
        
        if (!result.contains("thank you", ignoreCase = true) && !result.contains("thanks", ignoreCase = true)) {
            result += " Thank you."
            changes.add(ToneChange("", "Thank you.", "Polite closing"))
        }
        
        return result
    }
    
    private fun makeFriendly(text: String, changes: MutableList<ToneChange>): String {
        var result = text
        
        // Add friendly expressions
        if (!result.contains("!", ignoreCase = true)) {
            result += "!"
            changes.add(ToneChange("", "!", "Friendly exclamation"))
        }
        
        // Add friendly words
        val friendlyWords = mapOf(
            "good" to "great",
            "okay" to "awesome",
            "fine" to "fantastic"
        )
        
        friendlyWords.forEach { (neutral, friendly) ->
            if (result.contains(neutral, ignoreCase = true)) {
                result = result.replace(neutral, friendly, ignoreCase = true)
                changes.add(ToneChange(neutral, friendly, "More friendly word choice"))
            }
        }
        
        return result
    }
    
    private fun makeConfident(text: String, changes: MutableList<ToneChange>): String {
        var result = text
        
        // Remove uncertain words
        val uncertainWords = mapOf(
            "maybe" to "definitely",
            "perhaps" to "certainly",
            "might" to "will",
            "could" to "can",
            "possibly" to "absolutely"
        )
        
        uncertainWords.forEach { (uncertain, confident) ->
            if (result.contains(uncertain, ignoreCase = true)) {
                result = result.replace(uncertain, confident, ignoreCase = true)
                changes.add(ToneChange(uncertain, confident, "More confident language"))
            }
        }
        
        return result
    }
    
    private fun makeEmpathetic(text: String, changes: MutableList<ToneChange>): String {
        var result = text
        
        // Add empathetic phrases
        if (!result.contains("understand", ignoreCase = true)) {
            result = "I understand that " + result.lowercase()
            changes.add(ToneChange("", "I understand that ", "Empathetic opening"))
        }
        
        // Add supportive words
        val supportivePhrases = listOf(
            "I'm here for you",
            "I can relate",
            "That must be difficult"
        )
        
        if (!result.contains("difficult", ignoreCase = true) && !result.contains("challenging", ignoreCase = true)) {
            result += " I'm here to help."
            changes.add(ToneChange("", "I'm here to help.", "Supportive closing"))
        }
        
        return result
    }
    
    private fun makePersuasive(text: String, changes: MutableList<ToneChange>): String {
        var result = text
        
        // Add persuasive words
        val persuasiveWords = mapOf(
            "good" to "excellent",
            "nice" to "outstanding",
            "okay" to "remarkable"
        )
        
        persuasiveWords.forEach { (neutral, persuasive) ->
            if (result.contains(neutral, ignoreCase = true)) {
                result = result.replace(neutral, persuasive, ignoreCase = true)
                changes.add(ToneChange(neutral, persuasive, "More persuasive language"))
            }
        }
        
        // Add call to action
        if (!result.contains("!", ignoreCase = true)) {
            result += " Don't miss out!"
            changes.add(ToneChange("", "Don't miss out!", "Call to action"))
        }
        
        return result
    }
    
    private fun makeConcise(text: String, changes: MutableList<ToneChange>): String {
        var result = text
        
        // Remove unnecessary words
        val unnecessaryWords = listOf(
            "very", "really", "quite", "rather", "somewhat", "pretty",
            "I think", "I believe", "in my opinion", "it seems like"
        )
        
        unnecessaryWords.forEach { word ->
            if (result.contains(word, ignoreCase = true)) {
                result = result.replace(word, "", ignoreCase = true)
                changes.add(ToneChange(word, "", "Removed unnecessary word for conciseness"))
            }
        }
        
        // Clean up extra spaces
        result = result.replace(Regex("\\s+"), " ").trim()
        
        return result
    }
    
    private fun makeDetailed(text: String, changes: MutableList<ToneChange>): String {
        var result = text
        
        // Add descriptive words
        val descriptiveWords = mapOf(
            "good" to "exceptionally good",
            "bad" to "significantly problematic",
            "big" to "substantially large",
            "small" to "considerably small"
        )
        
        descriptiveWords.forEach { (simple, detailed) ->
            if (result.contains(simple, ignoreCase = true)) {
                result = result.replace(simple, detailed, ignoreCase = true)
                changes.add(ToneChange(simple, detailed, "More detailed description"))
            }
        }
        
        return result
    }
    
    private fun makeCreative(text: String, changes: MutableList<ToneChange>): String {
        var result = text
        
        // Add creative expressions
        val creativeWords = mapOf(
            "good" to "brilliant",
            "bad" to "terrible",
            "big" to "enormous",
            "small" to "tiny"
        )
        
        creativeWords.forEach { (plain, creative) ->
            if (result.contains(plain, ignoreCase = true)) {
                result = result.replace(plain, creative, ignoreCase = true)
                changes.add(ToneChange(plain, creative, "More creative word choice"))
            }
        }
        
        return result
    }
    
    private fun makeTechnical(text: String, changes: MutableList<ToneChange>): String {
        var result = text
        
        // Add technical terms
        val technicalTerms = mapOf(
            "good" to "optimal",
            "bad" to "suboptimal",
            "big" to "large-scale",
            "small" to "minimal"
        )
        
        technicalTerms.forEach { (simple, technical) ->
            if (result.contains(simple, ignoreCase = true)) {
                result = result.replace(simple, technical, ignoreCase = true)
                changes.add(ToneChange(simple, technical, "Technical terminology"))
            }
        }
        
        return result
    }
    
    private fun makeRomantic(text: String, changes: MutableList<ToneChange>): String {
        var result = text
        
        // Add romantic expressions
        val romanticWords = mapOf(
            "love" to "adore",
            "like" to "cherish",
            "good" to "wonderful",
            "beautiful" to "stunning"
        )
        
        romanticWords.forEach { (plain, romantic) ->
            if (result.contains(plain, ignoreCase = true)) {
                result = result.replace(plain, romantic, ignoreCase = true)
                changes.add(ToneChange(plain, romantic, "More romantic language"))
            }
        }
        
        return result
    }
    
    private fun makeHumorous(text: String, changes: MutableList<ToneChange>): String {
        var result = text
        
        // Add humor
        if (!result.contains("!", ignoreCase = true)) {
            result += " 😄"
            changes.add(ToneChange("", "😄", "Added humor"))
        }
        
        return result
    }
    
    private fun makeSarcastic(text: String, changes: MutableList<ToneChange>): String {
        var result = text
        
        // Add sarcastic tone
        if (!result.contains("!", ignoreCase = true)) {
            result += " (not really)"
            changes.add(ToneChange("", "(not really)", "Sarcastic addition"))
        }
        
        return result
    }
    
    private fun makeGenZ(text: String, changes: MutableList<ToneChange>): String {
        var result = text
        
        // Add Gen Z slang
        val genZWords = mapOf(
            "good" to "slaps",
            "bad" to "mid",
            "cool" to "fire",
            "awesome" to "no cap"
        )
        
        genZWords.forEach { (old, new) ->
            if (result.contains(old, ignoreCase = true)) {
                result = result.replace(old, new, ignoreCase = true)
                changes.add(ToneChange(old, new, "Gen Z slang"))
            }
        }
        
        return result
    }
    
    private fun calculateToneConfidence(original: String, transformed: String): Float {
        // Simple confidence calculation based on text changes
        val changeRatio = if (original.length > 0) {
            (transformed.length - original.length).toFloat() / original.length
        } else 0f
        
        return when {
            changeRatio > 0.5f -> 0.9f
            changeRatio > 0.2f -> 0.8f
            changeRatio > 0.1f -> 0.7f
            else -> 0.6f
        }
    }
}
