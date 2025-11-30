package com.smarttype.aikeyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttype.aikeyboard.ai.GrammarEngine
import com.smarttype.aikeyboard.ai.SpellingChecker
import com.smarttype.aikeyboard.data.model.KeyboardTheme

/**
 * Enhanced suggestion bar that displays:
 * - Spelling corrections
 * - Grammar corrections
 * - Word suggestions
 */
@Composable
fun EnhancedSuggestionBar(
    spellingResult: SpellingChecker.SpellingResult?,
    grammarResult: GrammarEngine.GrammarResult?,
    theme: KeyboardTheme,
    onSpellingCorrection: (SpellingChecker.WordCorrection, String) -> Unit,
    onGrammarCorrection: (String) -> Unit,
    onIgnoreSpelling: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.suggestionBackgroundColor)
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        // Spelling corrections
        spellingResult?.words?.forEach { wordCorrection ->
            if (wordCorrection.suggestions.isNotEmpty()) {
                SpellingSuggestionRow(
                    correction = wordCorrection,
                    theme = theme,
                    onApply = { suggestion ->
                        onSpellingCorrection(wordCorrection, suggestion)
                    },
                    onIgnore = {
                        onIgnoreSpelling(wordCorrection.word)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        
        // Grammar corrections
        grammarResult?.corrections?.take(3)?.forEach { correction ->
            GrammarSuggestionChip(
                correction = correction,
                theme = theme,
                onApply = {
                    onGrammarCorrection(grammarResult.correctedText)
                }
            )
        }
    }
}

@Composable
private fun SpellingSuggestionRow(
    correction: SpellingChecker.WordCorrection,
    theme: KeyboardTheme,
    onApply: (String) -> Unit,
    onIgnore: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Show misspelled word with red indicator
        Text(
            text = "${correction.word}:",
            color = Color.Red,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 4.dp)
        )
        
        // Show suggestions
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(correction.suggestions) { suggestion ->
                SuggestionChip(
                    onClick = { onApply(suggestion) },
                    label = {
                        Text(
                            text = suggestion,
                            fontSize = 14.sp,
                            color = theme.suggestionTextColor
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = theme.accentColor.copy(alpha = 0.2f)
                    )
                )
            }
        }
        
        // Ignore button
        TextButton(
            onClick = onIgnore,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Text(
                text = "Ignore",
                fontSize = 12.sp,
                color = theme.suggestionTextColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun GrammarSuggestionChip(
    correction: GrammarEngine.Correction,
    theme: KeyboardTheme,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.Blue.copy(alpha = 0.1f)
        ),
        onClick = onApply
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = correction.corrected,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.suggestionTextColor
                )
                Text(
                    text = correction.explanation,
                    fontSize = 11.sp,
                    color = theme.suggestionTextColor.copy(alpha = 0.7f)
                )
            }
            
            TextButton(onClick = onApply) {
                Text(
                    text = "Apply",
                    fontSize = 12.sp,
                    color = Color.Blue
                )
            }
        }
    }
}

