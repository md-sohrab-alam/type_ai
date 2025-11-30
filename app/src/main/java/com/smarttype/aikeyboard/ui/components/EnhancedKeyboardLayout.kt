package com.smarttype.aikeyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttype.aikeyboard.data.model.KeyboardTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope

/**
 * Enhanced keyboard layout with support for:
 * - QWERTY letters
 * - Numbers row
 * - Symbols keyboard
 * - Caps lock / Shift toggle
 * - Long-press for alternate characters
 */
@Composable
fun EnhancedKeyboardLayout(
    theme: KeyboardTheme,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onSpace: () -> Unit,
    onVoiceInput: () -> Unit,
    onAiButtonClick: () -> Unit,
    onShiftToggle: () -> Unit,
    onNumbersToggle: () -> Unit,
    onSymbolsToggle: () -> Unit,
    isCapsLock: Boolean = false,
    isShiftPressed: Boolean = false,
    showNumbers: Boolean = false,
    showSymbols: Boolean = false
) {
    // Remember key lists to prevent recreation on every recomposition
    val topRowKeys = remember(isCapsLock, isShiftPressed) {
        if (isCapsLock || isShiftPressed) {
            listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
        } else {
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
        }
    }
    
    val middleRowKeys = remember(isCapsLock, isShiftPressed) {
        if (isCapsLock || isShiftPressed) {
            listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
        } else {
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
        }
    }
    
    val bottomRowKeys = remember(isCapsLock, isShiftPressed) {
        if (isCapsLock || isShiftPressed) {
            listOf("Z", "X", "C", "V", "B", "N", "M")
        } else {
            listOf("z", "x", "c", "v", "b", "n", "m")
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp) // Increased spacing
    ) {
        // Numbers row (always shown at top like standard keyboards)
        KeyboardRow(
            keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
            theme = theme,
            onKeyPress = onKeyPress
        )
        
        // QWERTY rows
        KeyboardRow(
            keys = topRowKeys,
            theme = theme,
            onKeyPress = onKeyPress
        )

        KeyboardRow(
            keys = middleRowKeys,
            theme = theme,
            onKeyPress = onKeyPress,
            modifier = Modifier.padding(start = 20.dp)
        )

        KeyboardRow(
            keys = bottomRowKeys,
            theme = theme,
            onKeyPress = onKeyPress,
            modifier = Modifier.padding(start = 40.dp)
        )

        // Bottom row with special keys
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Shift/Caps Lock button
            KeyboardKey(
                text = if (isCapsLock) "⇪" else "⇧",
                theme = theme,
                onKeyPress = { onShiftToggle() },
                modifier = Modifier.weight(1.2f),
                isPressed = isCapsLock || isShiftPressed
            )
            
            // Numbers/Symbols toggle - cycles: Letters -> Numbers -> Symbols -> Letters
            KeyboardKey(
                text = when {
                    showSymbols -> "ABC"
                    showNumbers -> "?123"
                    else -> "123"
                },
                theme = theme,
                onKeyPress = { 
                    if (showSymbols) {
                        // Currently showing symbols, go back to letters
                        onSymbolsToggle()
                    } else if (showNumbers) {
                        // Currently showing numbers row, switch to symbols
                        onSymbolsToggle()
                    } else {
                        // Currently showing letters, show numbers row
                        onNumbersToggle()
                    }
                },
                modifier = Modifier.weight(1f),
                isPressed = showNumbers || showSymbols
            )
            
            // AI button
            KeyboardKey(
                text = "AI",
                theme = theme,
                onKeyPress = { onAiButtonClick() },
                modifier = Modifier.weight(1f),
                isPressed = false
            )
            
            // Voice input
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
            
            // Backspace with long press support
            BackspaceKey(
                theme = theme,
                onBackspace = onBackspace,
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

/**
 * Symbols keyboard layout
 */
@Composable
fun SymbolsKeyboardLayout(
    theme: KeyboardTheme,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onSpace: () -> Unit,
    onNumbersToggle: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp) // Increased spacing
    ) {
        // First row of symbols
        KeyboardRow(
            keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
            theme = theme,
            onKeyPress = onKeyPress
        )
        
        // Second row
        KeyboardRow(
            keys = listOf("-", "/", ":", ";", "(", ")", "$", "&", "@", "\""),
            theme = theme,
            onKeyPress = onKeyPress
        )
        
        // Third row
        KeyboardRow(
            keys = listOf(".", ",", "?", "!", "'", "\"", "[", "]", "{", "}"),
            theme = theme,
            onKeyPress = onKeyPress,
            modifier = Modifier.padding(start = 20.dp)
        )
        
        // Fourth row
        KeyboardRow(
            keys = listOf("#", "%", "^", "*", "+", "=", "_", "\\", "|", "~"),
            theme = theme,
            onKeyPress = onKeyPress,
            modifier = Modifier.padding(start = 40.dp)
        )
        
        // Bottom row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeyboardKey(
                text = "123",
                theme = theme,
                onKeyPress = { onNumbersToggle() },
                modifier = Modifier.weight(1f)
            )
            
            KeyboardKey(
                text = "SPACE",
                theme = theme,
                onKeyPress = { onSpace() },
                modifier = Modifier.weight(4f)
            )
            
            BackspaceKey(
                theme = theme,
                onBackspace = onBackspace,
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

/**
 * Enhanced keyboard key with press state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardKey(
    text: String,
    theme: KeyboardTheme,
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier,
    isPressed: Boolean = false
) {
    var isCurrentlyPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isCurrentlyPressed) {
        if (isCurrentlyPressed) {
            kotlinx.coroutines.delay(100)
            isCurrentlyPressed = false
        }
    }

    val backgroundColor = when {
        isPressed -> theme.accentColor.copy(alpha = 0.7f)
        isCurrentlyPressed -> theme.keyPressedColor
        else -> theme.keyBackgroundColor
    }

    Card(
        modifier = modifier
            .height(56.dp) // Increased from 48dp to standard keyboard size
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        onClick = {
            isCurrentlyPressed = true
            onKeyPress(text)
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 18.sp, // Increased from 16sp for better visibility
                fontWeight = FontWeight.Medium,
                color = if (isPressed) Color.White else theme.keyTextColor
            )
        }
    }
}

/**
 * Backspace key with long press support for continuous deletion
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackspaceKey(
    theme: KeyboardTheme,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLongPressing by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    var shouldStartLongPress by remember { mutableStateOf(false) }

    // Handle long press start trigger
    LaunchedEffect(shouldStartLongPress) {
        if (shouldStartLongPress) {
            shouldStartLongPress = false
            delay(500) // Wait 500ms before starting continuous deletion
            if (isPressed) {
                isLongPressing = true
            }
        }
    }

    // Handle long press continuous deletion
    LaunchedEffect(isLongPressing) {
        if (isLongPressing) {
            // Delete continuously while long pressing
            while (isLongPressing && isActive) {
                onBackspace()
                delay(50) // Delete every 50ms for smooth continuous deletion
            }
        }
    }

    // Reset press state after release
    LaunchedEffect(isPressed) {
        if (isPressed && !isLongPressing) {
            delay(150)
            isPressed = false
        }
    }

    val backgroundColor = remember(isLongPressing, isPressed) {
        when {
            isLongPressing -> theme.accentColor.copy(alpha = 0.7f)
            isPressed -> theme.keyPressedColor
            else -> theme.keyBackgroundColor
        }
    }

    val textColor = remember(isLongPressing) {
        if (isLongPressing) Color.White else theme.keyTextColor
    }
    
    Card(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    isPressed = true
                    shouldStartLongPress = true
                    
                    // Wait for up or cancellation
                    val up = waitForUpOrCancellation()
                    
                    // Check if long press was triggered during the wait
                    val wasLongPress = isLongPressing
                    
                    if (up != null) {
                        // Normal release
                        if (!wasLongPress) {
                            onBackspace()
                        }
                    }
                    
                    isLongPressing = false
                    isPressed = false
                    shouldStartLongPress = false
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        onClick = {
            // Fallback for single tap (if pointerInput doesn't catch it)
            if (!isLongPressing && !isPressed) {
                onBackspace()
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⌫",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

/**
 * Keyboard row composable
 */
@Composable
fun KeyboardRow(
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
            key(key) { // Stable key to prevent recomposition
                KeyboardKey(
                    text = key,
                    theme = theme,
                    onKeyPress = onKeyPress,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

