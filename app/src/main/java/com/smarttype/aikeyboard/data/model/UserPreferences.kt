package com.smarttype.aikeyboard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey
    val id: Int = 1,
    val theme: String = "system", // system, light, dark
    val language: String = "en",
    val autoCorrect: Boolean = true,
    val smartPredictions: Boolean = true,
    val glideTyping: Boolean = true,
    val voiceInput: Boolean = false,
    val hapticFeedback: Boolean = true,
    val soundFeedback: Boolean = false,
    val showSuggestions: Boolean = true,
    val suggestionCount: Int = 3,
    val fontSize: Float = 16f,
    val keyHeight: Float = 48f,
    val keySpacing: Float = 4f,
    val selectedTheme: String = "default",
    val customTones: List<String> = emptyList(),
    val aiFeaturesEnabled: Boolean = true,
    val privacyMode: Boolean = false, // On-device processing only
    val syncEnabled: Boolean = false,
    val lastSyncTime: Long = 0L
)
