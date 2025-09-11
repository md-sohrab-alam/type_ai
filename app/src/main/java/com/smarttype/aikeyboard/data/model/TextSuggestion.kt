package com.smarttype.aikeyboard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "text_suggestions")
data class TextSuggestion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val context: String,
    val frequency: Int = 1,
    val lastUsed: Long = System.currentTimeMillis(),
    val isCustom: Boolean = false,
    val category: SuggestionCategory = SuggestionCategory.GENERAL
)

enum class SuggestionCategory {
    GENERAL,
    EMAIL,
    SOCIAL,
    PROFESSIONAL,
    CASUAL,
    FORMAL,
    TECHNICAL,
    MEDICAL,
    LEGAL
}
