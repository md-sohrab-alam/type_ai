package com.smarttype.aikeyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttype.aikeyboard.data.model.KeyboardTheme
import com.smarttype.aikeyboard.ui.viewmodel.KeyboardViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    onVoiceInput: () -> Unit
) {
    val userPreferences by viewModel.userPreferences.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val selectedTone by viewModel.selectedTone.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val theme = getCurrentTheme(userPreferences)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.backgroundColor)
            .padding(4.dp)
    ) {
        // Suggestions Bar
        if (suggestions.isNotEmpty()) {
            SuggestionBar(
                suggestions = suggestions,
                theme = theme,
                onSuggestionSelect = onSuggestionSelect
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // Tone Selection Bar
        ToneSelectionBar(
            selectedTone = selectedTone,
            theme = theme,
            onToneChange = onToneChange
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        // Main Keyboard
        KeyboardLayout(
            theme = theme,
            onKeyPress = onKeyPress,
            onBackspace = onBackspace,
            onEnter = onEnter,
            onSpace = onSpace,
            onVoiceInput = onVoiceInput
        )
        
        // Loading indicator
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
        
        // Error display
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
                border = SuggestionChipDefaults.suggestionChipBorder(
                    borderColor = theme.borderColor
                )
            )
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
        // First row: Q W E R T Y U I O P
        KeyboardRow(
            keys = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
            theme = theme,
            onKeyPress = onKeyPress
        )
        
        // Second row: A S D F G H J K L
        KeyboardRow(
            keys = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
            theme = theme,
            onKeyPress = onKeyPress,
            modifier = Modifier.padding(start = 20.dp)
        )
        
        // Third row: Z X C V B N M
        KeyboardRow(
            keys = listOf("Z", "X", "C", "V", "B", "N", "M"),
            theme = theme,
            onKeyPress = onKeyPress,
            modifier = Modifier.padding(start = 40.dp)
        )
        
        // Fourth row: Special keys
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Voice input button
            KeyboardKey(
                text = "🎤",
                theme = theme,
                onKeyPress = { onVoiceInput() },
                modifier = Modifier.weight(1f)
            )
            
            // Space bar
            KeyboardKey(
                text = "SPACE",
                theme = theme,
                onKeyPress = { onSpace() },
                modifier = Modifier.weight(4f)
            )
            
            // Backspace
            KeyboardKey(
                text = "⌫",
                theme = theme,
                onKeyPress = { onBackspace() },
                modifier = Modifier.weight(1f)
            )
            
            // Enter
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
    
    // Reset pressed state after a short delay
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

private fun getCurrentTheme(userPreferences: com.smarttype.aikeyboard.data.model.UserPreferences?): KeyboardTheme {
    val themeId = userPreferences?.selectedTheme ?: "default"
    return com.smarttype.aikeyboard.data.model.DefaultThemes.ALL_THEMES
        .find { it.id == themeId } ?: com.smarttype.aikeyboard.data.model.DefaultThemes.DEFAULT
}
