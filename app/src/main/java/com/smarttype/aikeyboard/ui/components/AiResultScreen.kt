package com.smarttype.aikeyboard.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttype.aikeyboard.ai.GrammarEngine
import com.smarttype.aikeyboard.ai.SpellingChecker
import com.smarttype.aikeyboard.data.model.KeyboardTheme

/**
 * AI Result Screen that displays spelling and grammar corrections.
 * Shows Copy and Replace buttons like Samsung keyboard.
 * This is displayed inline in the keyboard, not as a dialog.
 */
@Composable
fun AiResultScreen(
    originalText: String,
    spellingResult: SpellingChecker.SpellingResult?,
    grammarResult: GrammarEngine.GrammarResult?,
    theme: KeyboardTheme,
    onDismiss: () -> Unit,
    onReplace: (String) -> Unit
) {
    val context = LocalContext.current
    val correctedText = grammarResult?.correctedText ?: originalText
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.backgroundColor
        )
    ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spelling & Grammar Check",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.keyTextColor
                    )
                    IconButton(onClick = onDismiss) {
                        Text(
                            text = "✕",
                            fontSize = 20.sp,
                            color = theme.keyTextColor
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Content - Only show original and corrected text
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Original text section
                    TextSection(
                        title = "Original Text",
                        text = originalText,
                        theme = theme
                    )
                    
                    // Corrected text section
                    TextSection(
                        title = "Corrected Text",
                        text = correctedText,
                        theme = theme,
                        isCorrected = true
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Copy button
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Corrected text", correctedText)
                            clipboard.setPrimaryClip(clip)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Copy")
                    }
                    
                    // Replace button
                    Button(
                        onClick = {
                            onReplace(correctedText)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = correctedText != originalText
                    ) {
                        Text("Replace")
                    }
                }
            }
        }
}

@Composable
private fun TextSection(
    title: String,
    text: String,
    theme: KeyboardTheme,
    isCorrected: Boolean = false
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.keyTextColor.copy(alpha = 0.7f)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isCorrected) {
                    Color.Green.copy(alpha = 0.1f)
                } else {
                    theme.keyBackgroundColor
                }
            )
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = theme.keyTextColor,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun SpellingErrorsSection(
    errors: List<SpellingChecker.WordCorrection>,
    theme: KeyboardTheme
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Spelling Errors (${errors.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Red
        )
        errors.forEach { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Red.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "❌ ${error.word}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    if (error.suggestions.isNotEmpty()) {
                        Text(
                            text = "Suggestions: ${error.suggestions.joinToString(", ")}",
                            fontSize = 12.sp,
                            color = theme.keyTextColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GrammarCorrectionsSection(
    corrections: List<GrammarEngine.Correction>,
    theme: KeyboardTheme
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Grammar Corrections (${corrections.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Blue
        )
        corrections.forEach { correction ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Blue.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "✏️ ${correction.original} → ${correction.corrected}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Blue
                    )
                    Text(
                        text = correction.explanation,
                        fontSize = 12.sp,
                        color = theme.keyTextColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

