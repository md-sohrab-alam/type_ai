package com.smarttype.aikeyboard.ui.viewmodel

import android.app.Application
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smarttype.aikeyboard.ai.GrammarEngine
import com.smarttype.aikeyboard.ai.ToneEngine
import com.smarttype.aikeyboard.data.model.UserPreferences
import com.smarttype.aikeyboard.data.repository.TextSuggestionRepository
import com.smarttype.aikeyboard.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@HiltViewModel
class KeyboardViewModel @Inject constructor(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val textSuggestionRepository: TextSuggestionRepository,
    private val grammarEngine: GrammarEngine,
    private val toneEngine: ToneEngine
) : AndroidViewModel(application) {
    
    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)
    val userPreferences: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()
    
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()
    
    private val _currentText = MutableStateFlow("")
    val currentText: StateFlow<String> = _currentText.asStateFlow()
    
    private val _selectedTone = MutableStateFlow("")
    val selectedTone: StateFlow<String> = _selectedTone.asStateFlow()
    
    private val _isVoiceInputActive = MutableStateFlow(false)
    val isVoiceInputActive: StateFlow<Boolean> = _isVoiceInputActive.asStateFlow()
    
    private val _grammarResult = MutableStateFlow<GrammarEngine.GrammarResult?>(null)
    val grammarResult: StateFlow<GrammarEngine.GrammarResult?> = _grammarResult.asStateFlow()
    
    private val _toneResult = MutableStateFlow<ToneEngine.ToneResult?>(null)
    val toneResult: StateFlow<ToneEngine.ToneResult?> = _toneResult.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadUserPreferences()
    }
    
    fun loadUserPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.getUserPreferences()
                .catch { e -> _error.value = e.message }
                .collect { preferences ->
                    _userPreferences.value = preferences
                    if (preferences != null) {
                        _selectedTone.value = preferences.customTones.firstOrNull() ?: "professional"
                    }
                }
        }
    }
    
    fun updateInputContext(editorInfo: EditorInfo?) {
        viewModelScope.launch {
            // Update context based on input type
            val context = when (editorInfo?.inputType) {
                EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> "email"
                EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_URI -> "url"
                EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE -> "message"
                else -> "general"
            }
            
            // Load suggestions for this context
            loadSuggestions(context)
        }
    }
    
    fun onKeyPressed(key: String) {
        _currentText.value += key
        updateSuggestions()
    }
    
    fun onTextInput(text: String) {
        _currentText.value += text
        updateSuggestions()
    }
    
    fun setFullText(text: String) {
        _currentText.value = text
        updateSuggestions()
    }
    
    fun onBackspace() {
        if (_currentText.value.isNotEmpty()) {
            _currentText.value = _currentText.value.dropLast(1)
            updateSuggestions()
        }
    }
    
    fun onEnter() {
        _currentText.value += "\n"
        updateSuggestions()
    }
    
    fun onSpace() {
        _currentText.value += " "
        updateSuggestions()
    }
    
    fun onSuggestionSelected(suggestion: String) {
        _currentText.value = suggestion
        viewModelScope.launch {
            textSuggestionRepository.incrementFrequency(suggestion)
        }
        _grammarResult.value = null
        updateSuggestions()
    }
    
    fun changeTone(tone: String) {
        _selectedTone.value = tone
        applyToneTransformation()
    }
    
    fun startVoiceInput() {
        _isVoiceInputActive.value = true
        // Voice input implementation would go here
    }
    
    fun stopVoiceInput() {
        _isVoiceInputActive.value = false
    }
    
    fun updateSelection(selStart: Int, selEnd: Int) {
        // Handle text selection for context-aware suggestions
        updateSuggestions()
    }
    
    private fun updateSuggestions() {
        viewModelScope.launch {
            val context = getCurrentContext()
            loadSuggestions(context)
        }
    }
    
    private suspend fun loadSuggestions(context: String) {
        try {
            _isLoading.value = true

            val preferences = _userPreferences.value
            val limit = preferences?.suggestionCount ?: 3

            val storedSuggestions = runCatching {
                textSuggestionRepository.getSuggestionsByContext(context, limit).firstOrNull()
            }.getOrNull().orEmpty()

            val storedTexts = storedSuggestions.map { it.text }
            if (storedTexts.isNotEmpty()) {
                _suggestions.value = storedTexts
            }

            val current = _currentText.value
            if (storedTexts.size < limit && shouldFetchAiSuggestions(current)) {
                val aiRepoSuggestions = textSuggestionRepository.refreshAiSuggestions(current, context, limit)
                if (aiRepoSuggestions.isNotEmpty()) {
                    val merged = (storedTexts + aiRepoSuggestions).distinct().take(limit)
                    _suggestions.value = merged
                }
            }

            val aiSuggestions = grammarEngine.getSuggestions(current, context, limit)
            if (aiSuggestions.isNotEmpty()) {
                val merged = (_suggestions.value + aiSuggestions).distinct().take(limit)
                _suggestions.value = merged
            }

        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }
    
    private fun shouldFetchAiSuggestions(text: String): Boolean {
        if (text.isBlank() || text.length < 4) return false
        val trimmed = text.trimEnd()
        if (trimmed.isEmpty()) return false
        val lastChar = text.last()
        return lastChar == ' ' || lastChar == '.' || lastChar == '!' || lastChar == '?'
    }

    private fun getCurrentContext(): String {
        return when {
            _currentText.value.contains("@") -> "email"
            _currentText.value.contains("http") -> "url"
            _currentText.value.length < 50 -> "message"
            else -> "general"
        }
    }
    
    fun checkGrammar() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = grammarEngine.checkGrammar(_currentText.value)
                _grammarResult.value = result
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyGrammarCorrection(correctedText: String) {
        _currentText.value = correctedText
        _grammarResult.value = null
        updateSuggestions()
    }

    fun applyToneResult(transformedText: String) {
        _currentText.value = transformedText
        _toneResult.value = null
        updateSuggestions()
    }
    
    private fun applyToneTransformation() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val preferences = _userPreferences.value
                if (preferences != null && _currentText.value.isNotBlank()) {
                    val toneStyle = getToneStyle(_selectedTone.value)
                    val result = toneEngine.transformTone(_currentText.value, toneStyle)
                    _toneResult.value = result
                } else {
                    _toneResult.value = null
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun getToneStyle(toneId: String): com.smarttype.aikeyboard.data.model.ToneStyle {
        return when (toneId) {
            "professional" -> com.smarttype.aikeyboard.data.model.DefaultTones.PROFESSIONAL
            "casual" -> com.smarttype.aikeyboard.data.model.DefaultTones.CASUAL
            "polite" -> com.smarttype.aikeyboard.data.model.DefaultTones.POLITE
            "friendly" -> com.smarttype.aikeyboard.data.model.DefaultTones.FRIENDLY
            "confident" -> com.smarttype.aikeyboard.data.model.DefaultTones.CONFIDENT
            "empathetic" -> com.smarttype.aikeyboard.data.model.DefaultTones.EMPATHETIC
            "persuasive" -> com.smarttype.aikeyboard.data.model.DefaultTones.PERSUASIVE
            "concise" -> com.smarttype.aikeyboard.data.model.DefaultTones.CONCISE
            "detailed" -> com.smarttype.aikeyboard.data.model.DefaultTones.DETAILED
            "creative" -> com.smarttype.aikeyboard.data.model.DefaultTones.CREATIVE
            "technical" -> com.smarttype.aikeyboard.data.model.DefaultTones.TECHNICAL
            "romantic" -> com.smarttype.aikeyboard.data.model.DefaultTones.ROMANTIC
            "humorous" -> com.smarttype.aikeyboard.data.model.DefaultTones.HUMOROUS
            "sarcasm" -> com.smarttype.aikeyboard.data.model.DefaultTones.SARCASM
            "gen_z" -> com.smarttype.aikeyboard.data.model.DefaultTones.GEN_Z
            else -> com.smarttype.aikeyboard.data.model.DefaultTones.PROFESSIONAL
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearCurrentText() {
        _currentText.value = ""
        _suggestions.value = emptyList()
        _grammarResult.value = null
        _toneResult.value = null
    }
}
