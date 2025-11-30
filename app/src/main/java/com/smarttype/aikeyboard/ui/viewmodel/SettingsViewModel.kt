package com.smarttype.aikeyboard.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smarttype.aikeyboard.data.model.UserPreferences
import com.smarttype.aikeyboard.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {
    
    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)
    val userPreferences: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()
    
    init {
        loadUserPreferences()
    }
    
    private fun loadUserPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.getUserPreferences()
                .catch { /* Handle error */ }
                .collect { preferences ->
                    _userPreferences.value = preferences
                }
        }
    }
    
    fun updateSelectedTheme(theme: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateSelectedTheme(theme)
        }
    }
    
    fun updateLanguage(language: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateLanguage(language)
        }
    }
    
    fun updateAutoCorrect(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateAutoCorrect(enabled)
        }
    }
    
    fun updateSmartPredictions(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateSmartPredictions(enabled)
        }
    }
    
    fun updateGlideTyping(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateGlideTyping(enabled)
        }
    }
    
    fun updateVoiceInput(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateVoiceInput(enabled)
        }
    }
    
    fun updateHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateHapticFeedback(enabled)
        }
    }
    
    fun updateSoundFeedback(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateSoundFeedback(enabled)
        }
    }
    
    fun updateAIFeatures(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateAIFeatures(enabled)
        }
    }
    
    fun updatePrivacyMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updatePrivacyMode(enabled)
        }
    }
    
    fun updateSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateSyncEnabled(enabled)
        }
    }
}
