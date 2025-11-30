package com.smarttype.aikeyboard.ui.viewmodel

import android.app.Application
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smarttype.aikeyboard.data.model.UserPreferences
import com.smarttype.aikeyboard.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {
    
    private val _isKeyboardEnabled = MutableStateFlow(false)
    val isKeyboardEnabled: StateFlow<Boolean> = _isKeyboardEnabled.asStateFlow()
    
    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)
    val userPreferences: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()
    
    init {
        checkKeyboardStatus()
        loadUserPreferences()
    }
    
    private fun checkKeyboardStatus() {
        val context = getApplication<Application>()
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val packageName = context.packageName

        val isEnabled = runCatching {
            inputMethodManager.enabledInputMethodList.any { info ->
                info.id.startsWith(packageName) || info.serviceInfo.packageName == packageName
            }
        }.getOrDefault(false)

        _isKeyboardEnabled.value = isEnabled
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
    
    fun refreshKeyboardStatus() {
        checkKeyboardStatus()
    }
}
