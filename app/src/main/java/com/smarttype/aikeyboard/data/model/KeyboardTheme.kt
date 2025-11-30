package com.smarttype.aikeyboard.data.model

import androidx.compose.ui.graphics.Color

data class KeyboardTheme(
    val id: String,
    val name: String,
    val description: String,
    val isPremium: Boolean = false,
    val backgroundColor: Color,
    val keyBackgroundColor: Color,
    val keyTextColor: Color,
    val keyPressedColor: Color,
    val suggestionBackgroundColor: Color,
    val suggestionTextColor: Color,
    val accentColor: Color,
    val borderColor: Color,
    val isDark: Boolean = false
)

object DefaultThemes {
    val DEFAULT = KeyboardTheme(
        id = "default",
        name = "Default",
        description = "Clean and minimal design",
        backgroundColor = Color(0xFFF5F5F5),
        keyBackgroundColor = Color(0xFFFFFFFF),
        keyTextColor = Color(0xFF000000),
        keyPressedColor = Color(0xFFE0E0E0),
        suggestionBackgroundColor = Color(0xFFFFFFFF),
        suggestionTextColor = Color(0xFF000000),
        accentColor = Color(0xFF2196F3),
        borderColor = Color(0xFFE0E0E0)
    )
    
    val DARK = KeyboardTheme(
        id = "dark",
        name = "Dark",
        description = "Easy on the eyes",
        isDark = true,
        backgroundColor = Color(0xFF121212),
        keyBackgroundColor = Color(0xFF1E1E1E),
        keyTextColor = Color(0xFFFFFFFF),
        keyPressedColor = Color(0xFF333333),
        suggestionBackgroundColor = Color(0xFF1E1E1E),
        suggestionTextColor = Color(0xFFFFFFFF),
        accentColor = Color(0xFFBB86FC),
        borderColor = Color(0xFF333333)
    )
    
    val BLUE = KeyboardTheme(
        id = "blue",
        name = "Ocean Blue",
        description = "Calming blue tones",
        backgroundColor = Color(0xFFE3F2FD),
        keyBackgroundColor = Color(0xFF2196F3),
        keyTextColor = Color(0xFFFFFFFF),
        keyPressedColor = Color(0xFF1976D2),
        suggestionBackgroundColor = Color(0xFFFFFFFF),
        suggestionTextColor = Color(0xFF000000),
        accentColor = Color(0xFF03DAC6),
        borderColor = Color(0xFFBBDEFB)
    )
    
    val GREEN = KeyboardTheme(
        id = "green",
        name = "Nature Green",
        description = "Fresh green colors",
        backgroundColor = Color(0xFFE8F5E8),
        keyBackgroundColor = Color(0xFF4CAF50),
        keyTextColor = Color(0xFFFFFFFF),
        keyPressedColor = Color(0xFF388E3C),
        suggestionBackgroundColor = Color(0xFFFFFFFF),
        suggestionTextColor = Color(0xFF000000),
        accentColor = Color(0xFF8BC34A),
        borderColor = Color(0xFFC8E6C9)
    )
    
    val PURPLE = KeyboardTheme(
        id = "purple",
        name = "Royal Purple",
        description = "Elegant purple design",
        backgroundColor = Color(0xFFF3E5F5),
        keyBackgroundColor = Color(0xFF9C27B0),
        keyTextColor = Color(0xFFFFFFFF),
        keyPressedColor = Color(0xFF7B1FA2),
        suggestionBackgroundColor = Color(0xFFFFFFFF),
        suggestionTextColor = Color(0xFF000000),
        accentColor = Color(0xFFE1BEE7),
        borderColor = Color(0xFFE1BEE7)
    )
    
    val ORANGE = KeyboardTheme(
        id = "orange",
        name = "Sunset Orange",
        description = "Warm orange vibes",
        backgroundColor = Color(0xFFFFF3E0),
        keyBackgroundColor = Color(0xFFFF9800),
        keyTextColor = Color(0xFFFFFFFF),
        keyPressedColor = Color(0xFFF57C00),
        suggestionBackgroundColor = Color(0xFFFFFFFF),
        suggestionTextColor = Color(0xFF000000),
        accentColor = Color(0xFFFFB74D),
        borderColor = Color(0xFFFFCC80)
    )
    
    val ALL_THEMES = listOf(DEFAULT, DARK, BLUE, GREEN, PURPLE, ORANGE)
}
