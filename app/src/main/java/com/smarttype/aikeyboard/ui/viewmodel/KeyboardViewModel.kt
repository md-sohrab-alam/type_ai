package com.smarttype.aikeyboard.ui.viewmodel

import android.app.Application
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smarttype.aikeyboard.ai.GrammarEngine
import com.smarttype.aikeyboard.ai.SpellingChecker
import com.smarttype.aikeyboard.ai.ToneEngine
import com.smarttype.aikeyboard.data.model.UserPreferences
import com.smarttype.aikeyboard.data.repository.TextSuggestionRepository
import com.smarttype.aikeyboard.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class KeyboardViewModel @Inject constructor(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val textSuggestionRepository: TextSuggestionRepository,
    private val grammarEngine: GrammarEngine,
    private val spellingChecker: SpellingChecker,
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
    
    // Spelling check results
    private val _spellingResult = MutableStateFlow<SpellingChecker.SpellingResult?>(null)
    val spellingResult: StateFlow<SpellingChecker.SpellingResult?> = _spellingResult.asStateFlow()
    
    // Keyboard state
    private val _isCapsLock = MutableStateFlow(false)
    val isCapsLock: StateFlow<Boolean> = _isCapsLock.asStateFlow()
    
    private val _isShiftPressed = MutableStateFlow(false)
    val isShiftPressed: StateFlow<Boolean> = _isShiftPressed.asStateFlow()
    
    private val _showNumbers = MutableStateFlow(false)
    val showNumbers: StateFlow<Boolean> = _showNumbers.asStateFlow()
    
    private val _showSymbols = MutableStateFlow(false)
    val showSymbols: StateFlow<Boolean> = _showSymbols.asStateFlow()
    
    // AI menu state
    private val _showAiMenu = MutableStateFlow(false)
    val showAiMenu: StateFlow<Boolean> = _showAiMenu.asStateFlow()
    
    private val _showAiResult = MutableStateFlow(false)
    val showAiResult: StateFlow<Boolean> = _showAiResult.asStateFlow()
    
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
        val textToAdd = if (_isCapsLock.value || _isShiftPressed.value) {
            key.uppercase()
        } else {
            key.lowercase()
        }
        _currentText.value += textToAdd
        
        // Reset shift after one character (unless caps lock is on)
        if (_isShiftPressed.value && !_isCapsLock.value) {
            _isShiftPressed.value = false
        }
        
        // Don't update suggestions while typing to prevent flickering
        // Suggestions will be updated when user pauses or selects AI features
    }
    
    fun onTextInput(text: String) {
        _currentText.value += text
        // No suggestions update to prevent flickering
    }
    
    fun setFullText(text: String) {
        _currentText.value = text
        // No suggestions update to prevent flickering
    }
    
    fun onBackspace() {
        if (_currentText.value.isNotEmpty()) {
            _currentText.value = _currentText.value.dropLast(1)
            // No suggestions update to prevent flickering
        }
    }
    
    fun onEnter() {
        _currentText.value += "\n"
        // No suggestions update to prevent flickering
    }
    
    fun onSpace() {
        _currentText.value += " "
        // No suggestions update to prevent flickering
    }
    
    // Keyboard state management
    fun toggleShift() {
        if (_isCapsLock.value) {
            _isCapsLock.value = false
        } else {
            _isShiftPressed.value = !_isShiftPressed.value
        }
    }
    
    fun toggleCapsLock() {
        _isCapsLock.value = !_isCapsLock.value
        _isShiftPressed.value = false
    }
    
    fun toggleNumbers() {
        if (_showSymbols.value) {
            // If symbols are showing, switch to numbers/letters
            _showSymbols.value = false
            _showNumbers.value = false
        } else {
            // Toggle numbers row visibility
            _showNumbers.value = !_showNumbers.value
        }
    }
    
    fun toggleSymbols() {
        if (_showSymbols.value) {
            // Switch back to letters
            _showSymbols.value = false
            _showNumbers.value = false
        } else {
            // Switch to symbols keyboard
            _showSymbols.value = true
            _showNumbers.value = false
        }
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
        _spellingResult.value = null
        _showAiResult.value = false
    }
    
    /**
     * Opens the AI menu to select features.
     */
    fun openAiMenu() {
        _showAiMenu.value = true
    }
    
    /**
     * Closes the AI menu.
     */
    fun closeAiMenu() {
        _showAiMenu.value = false
    }
    
    /**
     * Closes the AI result screen.
     */
    fun closeAiResult() {
        _showAiResult.value = false
    }
    
    /**
     * On-demand spelling and grammar checking.
     * Called when user explicitly requests it from AI menu.
     */
    fun checkSpellingAndGrammar() {
        viewModelScope.launch {
            val text = _currentText.value
            if (text.isBlank()) {
                _error.value = "No text to check"
                return@launch
            }
            
            try {
                _isLoading.value = true
                _showAiMenu.value = false
                
                // Check spelling
                val spellingResult = spellingChecker.checkSpelling(text)
                _spellingResult.value = spellingResult
                
                // Check grammar
                val grammarResult = grammarEngine.checkGrammar(text)
                _grammarResult.value = grammarResult
                
                // Show result screen
                _showAiResult.value = true
                
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Applies a spelling correction.
     */
    fun applySpellingCorrection(correction: SpellingChecker.WordCorrection, suggestion: String) {
        val text = _currentText.value
        val correctedText = text.replaceRange(
            correction.startIndex,
            correction.endIndex,
            suggestion
        )
        _currentText.value = correctedText
        checkSpellingAndGrammar()
    }
    
    /**
     * Ignores a spelling correction (adds word to dictionary).
     */
    fun ignoreSpellingCorrection(word: String) {
        viewModelScope.launch {
            spellingChecker.addToDictionary(word)
            checkSpellingAndGrammar()
        }
    }
}
