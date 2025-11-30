package com.smarttype.aikeyboard.ai

import com.smarttype.aikeyboard.data.model.SuggestionCategory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.text.RegexOption
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Grammar correction engine that provides real-time grammar, spelling, and punctuation checking.
 * 
 * This engine uses heuristic-based rules to detect and correct common grammar mistakes.
 * It can be extended with AI/ML models for more advanced grammar checking in the future.
 * 
 * Features:
 * - Capitalization correction
 * - Pronoun capitalization (e.g., "i" -> "I")
 * - Punctuation fixes
 * - Contraction handling based on tone/category
 * - Oxford comma insertion for formal writing
 * 
 * @see Correction
 * @see GrammarResult
 */
@Singleton
class GrammarEngine @Inject constructor() {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    /**
     * Result of grammar checking operation.
     * 
     * @param originalText The original input text
     * @param correctedText The text after applying all corrections
     * @param corrections List of individual corrections made
     * @param confidence Confidence score (0.0-1.0) indicating how certain the corrections are
     */
    data class GrammarResult(
        val originalText: String,
        val correctedText: String,
        val corrections: List<Correction>,
        val confidence: Float
    )

    /**
     * Represents a single correction made to the text.
     * 
     * @param original The original text that was corrected
     * @param corrected The corrected text
     * @param type The type of correction (spelling, grammar, punctuation, etc.)
     * @param startIndex Start index of the correction in the original text
     * @param endIndex End index of the correction in the original text
     * @param explanation Human-readable explanation of the correction
     */
    data class Correction(
        val original: String,
        val corrected: String,
        val type: CorrectionType,
        val startIndex: Int,
        val endIndex: Int,
        val explanation: String
    )

    /**
     * Types of corrections that can be made.
     */
    enum class CorrectionType {
        SPELLING,      // Spelling mistakes
        GRAMMAR,       // Grammar errors
        PUNCTUATION,   // Punctuation issues
        STYLE,         // Style improvements
        TONE           // Tone-related changes
    }

    /**
     * Checks grammar and returns corrections.
     * 
     * @param text The text to check
     * @param language Language code (default: "en" for English)
     * @param category Context category (general, email, professional, etc.)
     * @return GrammarResult containing corrections and confidence score
     */
    suspend fun checkGrammar(
        text: String,
        language: String = "en",
        category: SuggestionCategory = SuggestionCategory.GENERAL
    ): GrammarResult = withContext(ioDispatcher) {
        val normalizedInput = text.trim()
        if (normalizedInput.isEmpty()) {
            return@withContext GrammarResult(
                originalText = text,
                correctedText = text,
                corrections = emptyList(),
                confidence = 1.0f
            )
        }

        val aiCorrections = runHeuristicChecks(
            text = normalizedInput,
            language = language,
            category = category
        )

        val punctuationCorrections = checkPunctuation(normalizedInput)

        val combinedCorrections = (aiCorrections + punctuationCorrections)
            .distinctBy { Triple(it.startIndex, it.endIndex, it.corrected.lowercase()) }

        val correctedText = applyCorrections(normalizedInput, combinedCorrections)

        GrammarResult(
            originalText = normalizedInput,
            correctedText = correctedText,
            corrections = combinedCorrections,
            confidence = calculateConfidence(combinedCorrections)
        )
    }

    private fun runHeuristicChecks(
        text: String,
        language: String,
        category: SuggestionCategory
    ): List<Correction> {
        val corrections = mutableListOf<Correction>()
        corrections += normalizeCapitalization(text)
        corrections += fixStandalonePronoun(text)
        corrections += fixRepeatedSpaces(text)
        corrections += fixCommonContractions(text, language, category)
        corrections += ensureOxfordComma(text, category)
        return corrections.filterNotNull()
    }

    private fun normalizeCapitalization(text: String): List<Correction> {
        if (text.isBlank()) return emptyList()
        val first = text.first()
        if (!first.isLetter() || first.isUpperCase()) return emptyList()
        return listOf(
            Correction(
                original = first.toString(),
                corrected = first.uppercase(),
                type = CorrectionType.GRAMMAR,
                startIndex = 0,
                endIndex = 1,
                explanation = "Capitalized the beginning of the sentence"
            )
        )
    }

    private fun fixStandalonePronoun(text: String): List<Correction> {
        val regex = "\\bi\\b".toRegex()
        return regex.findAll(text).map { match ->
            Correction(
                original = match.value,
                corrected = "I",
                type = CorrectionType.GRAMMAR,
                startIndex = match.range.first,
                endIndex = match.range.last + 1,
                explanation = "Capitalized the pronoun \"I\""
            )
        }.toList()
    }

    private fun fixRepeatedSpaces(text: String): List<Correction> {
        val regex = "\\s{2,}".toRegex()
        return regex.findAll(text).map { match ->
            Correction(
                original = match.value,
                corrected = " ",
                type = CorrectionType.STYLE,
                startIndex = match.range.first,
                endIndex = match.range.last + 1,
                explanation = "Replaced repeated spaces with a single space"
            )
        }.toList()
    }

    private fun fixCommonContractions(
        text: String,
        language: String,
        category: SuggestionCategory
    ): List<Correction> {
        if (language.lowercase() != "en") return emptyList()
        if (category == SuggestionCategory.CASUAL) return emptyList()
        val replacements = mapOf(
            "can't" to "cannot",
            "won't" to "will not",
            "don't" to "do not"
        )
        val regex = replacements.keys.joinToString("|") { "\\b$it\\b" }
            .toRegex(RegexOption.IGNORE_CASE)
        return regex.findAll(text).map { match ->
            val replacement = replacements[match.value.lowercase()] ?: match.value
            Correction(
                original = match.value,
                corrected = replacement,
                type = CorrectionType.TONE,
                startIndex = match.range.first,
                endIndex = match.range.last + 1,
                explanation = "Expanded contraction for a more formal tone"
            )
        }.toList()
    }

    private fun ensureOxfordComma(
        text: String,
        category: SuggestionCategory
    ): List<Correction> {
        if (category != SuggestionCategory.FORMAL) return emptyList()
        val regex = "\\b(\\w+), (\\w+) and (\\w+)".toRegex()
        val match = regex.find(text) ?: return emptyList()
        val correctionText = "${match.groupValues[1]}, ${match.groupValues[2]}, and ${match.groupValues[3]}"
        return listOf(
            Correction(
                original = match.value,
                corrected = correctionText,
                type = CorrectionType.STYLE,
                startIndex = match.range.first,
                endIndex = match.range.last + 1,
                explanation = "Added an Oxford comma for clarity"
            )
        )
    }

    private fun checkPunctuation(text: String): List<Correction> {
        if (text.isEmpty()) return emptyList()
        if (text.endsWith('.') || text.endsWith('!') || text.endsWith('?')) {
            return emptyList()
        }

        return listOf(
            Correction(
                original = "",
                corrected = ".",
                type = CorrectionType.PUNCTUATION,
                startIndex = text.length,
                endIndex = text.length,
                explanation = "Added sentence-ending punctuation"
            )
        )
    }

    private fun applyCorrections(text: String, corrections: List<Correction>): String {
        if (corrections.isEmpty()) return text

        val builder = StringBuilder(text)
        corrections
            .sortedByDescending { it.startIndex }
            .forEach { correction ->
                val start = correction.startIndex.coerceIn(0, builder.length)
                val end = correction.endIndex.coerceIn(start, builder.length)
                builder.replace(start, end, correction.corrected)
            }

        return builder.toString()
    }

    private fun calculateConfidence(corrections: List<Correction>): Float {
        if (corrections.isEmpty()) return 1.0f

        val severityScore = corrections.sumOf { correction ->
            when (correction.type) {
                CorrectionType.GRAMMAR -> 0.25
                CorrectionType.SPELLING -> 0.2
                CorrectionType.PUNCTUATION -> 0.15
                CorrectionType.STYLE -> 0.1
                CorrectionType.TONE -> 0.1
            }
        }

        val normalized = (1.0 - severityScore.coerceAtMost(0.9)).toFloat()
        return normalized.coerceIn(0.5f, 1.0f)
    }

    suspend fun getSuggestions(
        text: String,
        context: String,
        limit: Int = 5
    ): List<String> = withContext(ioDispatcher) {
        val suggestions = mutableListOf<String>()

        val normalized = text.lowercase()
        val commonPhrases = mapOf(
            "thank you" to listOf(
                "Thank you for your time.",
                "Thank you for the opportunity.",
                "Thank you for your help."
            ),
            "i would like" to listOf(
                "I would like to schedule a meeting.",
                "I would like to know more about this.",
                "I would like to discuss further."
            ),
            "please let me" to listOf(
                "Please let me know if you have any questions.",
                "Please let me know when you are available.",
                "Please let me know how I can help."
            ),
            "i hope" to listOf(
                "I hope this helps.",
                "I hope you're doing well.",
                "I hope to hear from you soon."
            )
        )

        commonPhrases.forEach { (key, values) ->
            if (normalized.contains(key)) {
                suggestions.addAll(values)
            }
        }

        return@withContext suggestions.distinct().take(limit)
    }
}
