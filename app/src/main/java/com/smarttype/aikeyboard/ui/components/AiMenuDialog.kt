package com.smarttype.aikeyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.smarttype.aikeyboard.data.model.KeyboardTheme

/**
 * AI Menu that shows available AI features.
 * User can select which feature to use (Spelling & Grammar, Tone, etc.)
 * This is displayed inline in the keyboard, not as a dialog.
 */
@Composable
fun AiMenu(
    theme: KeyboardTheme,
    onDismiss: () -> Unit,
    onSpellingGrammarClick: () -> Unit,
    onToneClick: () -> Unit,
    onTranslateClick: () -> Unit,
    onSmartReplyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.backgroundColor
        )
    ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Text(
                    text = "AI Features",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.keyTextColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Spelling & Grammar option
                AiMenuItem(
                    title = "Spelling & Grammar",
                    description = "Check and correct spelling and grammar errors",
                    icon = "✓",
                    theme = theme,
                    onClick = {
                        onSpellingGrammarClick()
                        onDismiss()
                    }
                )
                
                // Tone Transformation option
                AiMenuItem(
                    title = "Tone",
                    description = "Transform text tone (professional, casual, etc.)",
                    icon = "🎭",
                    theme = theme,
                    onClick = {
                        onToneClick()
                        onDismiss()
                    }
                )
                
                // Translation option (placeholder)
                AiMenuItem(
                    title = "Translate",
                    description = "Translate text to other languages",
                    icon = "🌐",
                    theme = theme,
                    onClick = {
                        onTranslateClick()
                        onDismiss()
                    },
                    enabled = false
                )
                
                // Smart Reply option (placeholder)
                AiMenuItem(
                    title = "Smart Reply",
                    description = "Generate context-aware replies",
                    icon = "💬",
                    theme = theme,
                    onClick = {
                        onSmartReplyClick()
                        onDismiss()
                    },
                    enabled = false
                )
                
                // Close button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Close",
                        color = theme.keyTextColor
                    )
                }
            }
        }
}

@Composable
private fun AiMenuItem(
    title: String,
    description: String,
    icon: String,
    theme: KeyboardTheme,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                theme.keyBackgroundColor
            } else {
                theme.keyBackgroundColor.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                text = icon,
                fontSize = 24.sp,
                modifier = Modifier.size(40.dp),
                color = if (enabled) theme.accentColor else theme.keyTextColor.copy(alpha = 0.5f)
            )
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) theme.keyTextColor else theme.keyTextColor.copy(alpha = 0.5f)
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = if (enabled) {
                        theme.keyTextColor.copy(alpha = 0.7f)
                    } else {
                        theme.keyTextColor.copy(alpha = 0.4f)
                    }
                )
            }
            
            // Arrow indicator
            if (enabled) {
                Text(
                    text = "→",
                    fontSize = 20.sp,
                    color = theme.accentColor
                )
            }
        }
    }
}

