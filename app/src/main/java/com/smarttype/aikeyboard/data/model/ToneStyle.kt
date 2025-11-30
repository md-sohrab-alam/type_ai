package com.smarttype.aikeyboard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tone_styles")
data class ToneStyle(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val isCustom: Boolean = false,
    val isEnabled: Boolean = true,
    val usageCount: Int = 0,
    val lastUsed: Long = 0L
)

object DefaultTones {
    val PROFESSIONAL = ToneStyle(
        id = "professional",
        name = "Professional",
        description = "Formal, business-appropriate tone"
    )
    
    val CASUAL = ToneStyle(
        id = "casual",
        name = "Casual",
        description = "Relaxed, friendly tone"
    )
    
    val POLITE = ToneStyle(
        id = "polite",
        name = "Polite",
        description = "Courteous and respectful"
    )
    
    val FRIENDLY = ToneStyle(
        id = "friendly",
        name = "Friendly",
        description = "Warm and approachable"
    )
    
    val CONFIDENT = ToneStyle(
        id = "confident",
        name = "Confident",
        description = "Assured and decisive"
    )
    
    val EMPATHETIC = ToneStyle(
        id = "empathetic",
        name = "Empathetic",
        description = "Understanding and compassionate"
    )
    
    val PERSUASIVE = ToneStyle(
        id = "persuasive",
        name = "Persuasive",
        description = "Convincing and compelling"
    )
    
    val CONCISE = ToneStyle(
        id = "concise",
        name = "Concise",
        description = "Brief and to the point"
    )
    
    val DETAILED = ToneStyle(
        id = "detailed",
        name = "Detailed",
        description = "Comprehensive and thorough"
    )
    
    val CREATIVE = ToneStyle(
        id = "creative",
        name = "Creative",
        description = "Imaginative and original"
    )
    
    val TECHNICAL = ToneStyle(
        id = "technical",
        name = "Technical",
        description = "Precise and specialized"
    )
    
    val ROMANTIC = ToneStyle(
        id = "romantic",
        name = "Romantic",
        description = "Loving and affectionate"
    )
    
    val HUMOROUS = ToneStyle(
        id = "humorous",
        name = "Humorous",
        description = "Funny and entertaining"
    )
    
    val SARCASM = ToneStyle(
        id = "sarcasm",
        name = "Sarcastic",
        description = "Ironically mocking"
    )
    
    val GEN_Z = ToneStyle(
        id = "gen_z",
        name = "Gen Z",
        description = "Modern, trendy language"
    )
    
    val ALL_TONES = listOf(
        PROFESSIONAL, CASUAL, POLITE, FRIENDLY, CONFIDENT,
        EMPATHETIC, PERSUASIVE, CONCISE, DETAILED, CREATIVE,
        TECHNICAL, ROMANTIC, HUMOROUS, SARCASM, GEN_Z
    )
}
