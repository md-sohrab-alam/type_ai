package com.smarttype.aikeyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttype.aikeyboard.data.model.DefaultThemes
import com.smarttype.aikeyboard.data.model.KeyboardTheme
import com.smarttype.aikeyboard.data.model.UserPreferences
import com.smarttype.aikeyboard.ui.viewmodel.KeyboardViewModel
import kotlinx.coroutines.delay

@Composable
fun KeyboardComposeView(
    viewModel: KeyboardViewModel,
    onKeyPress: (String) -> Unit,
    onTextInput: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onSpace: () -> Unit,
    onSuggestionSelect: (String) -> Unit,
    onToneChange: (String) -> Unit,
    onVoiceInput: () -> Unit,
    onApplyGrammar: (String) -> Unit,
    onApplyTone: (String) -> Unit
) {
    val userPreferences by viewModel.userPreferences.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val selectedTone by viewModel.selectedTone.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val grammarResult by viewModel.grammarResult.collectAsState()
    val currentText by viewModel.currentText.collectAsState()
    val toneResult by viewModel.toneResult.collectAsState()

    val theme = getCurrentTheme(userPreferences)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.backgroundColor)
            .padding(4.dp)
    ) {
        if (suggestions.isNotEmpty()) {
            SuggestionBar(
                suggestions = suggestions,
                theme = theme,
                onSuggestionSelect = onSuggestionSelect
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        grammarResult?.let { result ->
            if (result.correctedText.isNotBlank() && result.correctedText != currentText) {
                GrammarSuggestionCard(
                    suggestedText = result.correctedText,
                    explanation = result.corrections.firstOrNull()?.explanation
                        ?: "Suggested improvement",
                    confidence = result.confidence,
                    theme = theme,
                    onApply = onApplyGrammar
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        ToneSelectionBar(
            selectedTone = selectedTone,
            theme = theme,
            onToneChange = onToneChange
        )
        Spacer(modifier = Modifier.height(4.dp))

        toneResult?.let { result ->
            if (result.transformedText.isNotBlank() && result.transformedText != currentText) {
                ToneSuggestionCard(
                    toneName = result.tone.name,
                    suggestedText = result.transformedText,
                    changes = result.changes.take(3),
                    confidence = result.confidence,
                    theme = theme,
                    onApply = onApplyTone
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        KeyboardLayout(
            theme = theme,
            onKeyPress = onKeyPress,
            onBackspace = onBackspace,
            onEnter = onEnter,
            onSpace = onSpace,
            onVoiceInput = onVoiceInput
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = theme.accentColor
                )
            }
        }

        error?.let { errorMessage ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Red.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(8.dp),
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuggestionBar(
    suggestions: List<String>,
    theme: KeyboardTheme,
    onSuggestionSelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(suggestions) { suggestion ->
            SuggestionChip(
                onClick = { onSuggestionSelect(suggestion) },
                label = {
                    Text(
                        text = suggestion,
                        fontSize = 14.sp,
                        color = theme.suggestionTextColor
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = theme.suggestionBackgroundColor
                ),
            )
        }
    }
}

@Composable
private fun GrammarSuggestionCard(
    suggestedText: String,
    explanation: String,
    confidence: Float,
    theme: KeyboardTheme,
    onApply: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.suggestionBackgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Suggested correction",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = theme.suggestionTextColor
            )
            Text(
                text = suggestedText,
                style = MaterialTheme.typography.bodyMedium,
                color = theme.suggestionTextColor
            )
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodySmall,
                color = theme.suggestionTextColor.copy(alpha = 0.7f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Confidence ${(confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.suggestionTextColor.copy(alpha = 0.6f)
                )
                Button(onClick = { onApply(suggestedText) }) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
private fun ToneSuggestionCard(
    toneName: String,
    suggestedText: String,
    changes: List<com.smarttype.aikeyboard.ai.ToneEngine.ToneChange>,
    confidence: Float,
    theme: KeyboardTheme,
    onApply: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.keyBackgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Tone: $toneName",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = theme.keyTextColor
            )
            Text(
                text = suggestedText,
                style = MaterialTheme.typography.bodyMedium,
                color = theme.keyTextColor
            )
            if (changes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    changes.forEach { change ->
                        Text(
                            text = "• ${change.original} → ${change.transformed}",
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.keyTextColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Confidence ${(confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.keyTextColor.copy(alpha = 0.6f)
                )
                Button(onClick = { onApply(suggestedText) }) {
                    Text("Apply tone")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToneSelectionBar(
    selectedTone: String,
    theme: KeyboardTheme,
    onToneChange: (String) -> Unit
) {
    val tones = listOf(
        "professional", "casual", "polite", "friendly", "confident"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(tones) { tone ->
            FilterChip(
                onClick = { onToneChange(tone) },
                label = {
                    Text(
                        text = tone.replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp,
                        color = if (tone == selectedTone) Color.White else theme.keyTextColor
                    )
                },
                selected = tone == selectedTone,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = theme.accentColor,
                    containerColor = theme.keyBackgroundColor
                )
            )
        }
    }
}

@Composable
private fun KeyboardLayout(
    theme: KeyboardTheme,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onSpace: () -> Unit,
    onVoiceInput: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyboardRow(
            keys = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
            theme = theme,
            onKeyPress = onKeyPress
        )

        KeyboardRow(
            keys = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
            theme = theme,
            onKeyPress = onKeyPress,
            modifier = Modifier.padding(start = 20.dp)
        )

        KeyboardRow(
            keys = listOf("Z", "X", "C", "V", "B", "N", "M"),
            theme = theme,
            onKeyPress = onKeyPress,
            modifier = Modifier.padding(start = 40.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeyboardKey(
                text = "🎤",
                theme = theme,
                onKeyPress = { onVoiceInput() },
                modifier = Modifier.weight(1f)
            )
            KeyboardKey(
                text = "SPACE",
                theme = theme,
                onKeyPress = { onSpace() },
                modifier = Modifier.weight(4f)
            )
            KeyboardKey(
                text = "⌫",
                theme = theme,
                onKeyPress = { onBackspace() },
                modifier = Modifier.weight(1f)
            )
            KeyboardKey(
                text = "↵",
                theme = theme,
                onKeyPress = { onEnter() },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KeyboardRow(
    keys: List<String>,
    theme: KeyboardTheme,
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        keys.forEach { key ->
            KeyboardKey(
                text = key,
                theme = theme,
                onKeyPress = onKeyPress,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeyboardKey(
    text: String,
    theme: KeyboardTheme,
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }

    Card(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed) theme.keyPressedColor else theme.keyBackgroundColor
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(theme.borderColor),
            width = 1.dp
        ),
        onClick = {
            isPressed = true
            onKeyPress(text)
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = theme.keyTextColor
            )
        }
    }
}

private fun getCurrentTheme(userPreferences: UserPreferences?): KeyboardTheme {
    val themeId = userPreferences?.selectedTheme ?: "default"
    return DefaultThemes.ALL_THEMES
        .find { it.id == themeId } ?: DefaultThemes.DEFAULT
}

