package com.smarttype.aikeyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.smarttype.aikeyboard.data.model.KeyboardTheme

/**
 * Alternate characters map for long-press keys
 */
object AlternateCharacters {
    val map = mapOf(
        "a" to listOf("à", "á", "â", "ã", "ä", "å", "æ"),
        "A" to listOf("À", "Á", "Â", "Ã", "Ä", "Å", "Æ"),
        "e" to listOf("è", "é", "ê", "ë", "ē", "ė", "ę"),
        "E" to listOf("È", "É", "Ê", "Ë", "Ē", "Ė", "Ę"),
        "i" to listOf("ì", "í", "î", "ï", "ī", "į", "ı"),
        "I" to listOf("Ì", "Í", "Î", "Ï", "Ī", "Į", "İ"),
        "o" to listOf("ò", "ó", "ô", "õ", "ö", "ø", "ō"),
        "O" to listOf("Ò", "Ó", "Ô", "Õ", "Ö", "Ø", "Ō"),
        "u" to listOf("ù", "ú", "û", "ü", "ū", "ų", "ů"),
        "U" to listOf("Ù", "Ú", "Û", "Ü", "Ū", "Ų", "Ů"),
        "c" to listOf("ç", "ć", "č", "ĉ", "ċ"),
        "C" to listOf("Ç", "Ć", "Č", "Ĉ", "Ċ"),
        "n" to listOf("ñ", "ń", "ň", "ņ", "ŋ"),
        "N" to listOf("Ñ", "Ń", "Ň", "Ņ", "Ŋ"),
        "s" to listOf("ś", "š", "ş", "ș", "ß"),
        "S" to listOf("Ś", "Š", "Ş", "Ș", "ẞ"),
        "y" to listOf("ý", "ÿ", "ŷ", "ỳ"),
        "Y" to listOf("Ý", "Ÿ", "Ŷ", "Ỳ"),
        "z" to listOf("ž", "ź", "ż", "ž"),
        "Z" to listOf("Ž", "Ź", "Ż", "Ž"),
        "1" to listOf("¹", "½", "¼", "⅓", "⅕", "⅙", "⅛"),
        "2" to listOf("²", "⅔", "⅖", "⅗", "⅜"),
        "3" to listOf("³", "¾", "⅗", "⅘", "⅝"),
        "4" to listOf("⁴", "⅘"),
        "5" to listOf("⁵", "⅚", "⅝", "⅞"),
        "0" to listOf("°", "º", "₀", "⁰"),
        "$" to listOf("€", "£", "¥", "₹", "₽", "₩", "₪", "₫", "₦", "₨", "₩", "₮", "₯", "₰", "₱", "₲", "₳", "₴", "₵", "₶", "₷", "₸", "₹", "₺", "₻", "₼", "₽", "₾", "₿"),
        "." to listOf("…", "•", "·", "‥"),
        "," to listOf("'", "‚", "„"),
        "?" to listOf("¿", "‽", "⁇", "⁈", "⁉"),
        "!" to listOf("¡", "‼", "‽", "⁉", "⁈"),
        "-" to listOf("–", "—", "―", "‒", "―"),
        "'" to listOf("'", "`", "´", "′", "″", "‴", "‵", "‶", "‷", "‹", "›"),
        "\"" to listOf("\"", "«", "»", "„", "‟", "‚", "‛", "‹", "›")
    )
    
    fun getAlternates(key: String): List<String> {
        return map[key] ?: emptyList()
    }
}

/**
 * Popup that shows alternate characters when a key is long-pressed
 */
@Composable
fun AlternateCharacterPopup(
    key: String,
    alternates: List<String>,
    theme: KeyboardTheme,
    onCharacterSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (alternates.isEmpty()) return
    
    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier
                .padding(8.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = theme.keyBackgroundColor
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .background(theme.keyBackgroundColor),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Show original key
                AlternateCharButton(
                    char = key,
                    theme = theme,
                    isOriginal = true,
                    onClick = {
                        onCharacterSelected(key)
                        onDismiss()
                    }
                )
                
                // Show alternate characters
                alternates.forEach { alt ->
                    AlternateCharButton(
                        char = alt,
                        theme = theme,
                        isOriginal = false,
                        onClick = {
                            onCharacterSelected(alt)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlternateCharButton(
    char: String,
    theme: KeyboardTheme,
    isOriginal: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOriginal) {
                theme.accentColor.copy(alpha = 0.3f)
            } else {
                theme.keyBackgroundColor
            }
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = char,
                fontSize = 20.sp,
                fontWeight = if (isOriginal) FontWeight.Bold else FontWeight.Normal,
                color = theme.keyTextColor
            )
        }
    }
}

