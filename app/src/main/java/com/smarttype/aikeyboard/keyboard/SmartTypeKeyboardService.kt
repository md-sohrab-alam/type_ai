package com.smarttype.aikeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.smarttype.aikeyboard.ui.components.KeyboardComposeView
import com.smarttype.aikeyboard.ui.viewmodel.KeyboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.smarttype.aikeyboard.data.repository.UserPreferencesRepository
import com.smarttype.aikeyboard.data.repository.TextSuggestionRepository
import com.smarttype.aikeyboard.ai.GrammarEngine
import com.smarttype.aikeyboard.ai.SpellingChecker
import com.smarttype.aikeyboard.ai.ToneEngine

/**
 * Main Input Method Service (IME) for SmartType AI Keyboard.
 * 
 * This service implements the Android InputMethodService interface and provides:
 * - Jetpack Compose-based keyboard UI
 * - Lifecycle-aware components for proper state management
 * - Integration with AI-powered grammar and tone engines
 * - ViewModel support for reactive state management
 * 
 * The service implements ViewModelStoreOwner, LifecycleOwner, and SavedStateRegistryOwner
 * to ensure proper lifecycle management and state persistence for Compose components.
 * 
 * @see InputMethodService
 * @see ViewModelStoreOwner
 * @see LifecycleOwner
 * @see SavedStateRegistryOwner
 */
@AndroidEntryPoint
class SmartTypeKeyboardService : InputMethodService(),
    ViewModelStoreOwner,
    LifecycleOwner,
    SavedStateRegistryOwner {

    // ViewModel store for managing ViewModels across configuration changes
    private val internalViewModelStore = ViewModelStore()
    
    // Lifecycle registry for managing component lifecycle events
    private val lifecycleRegistry = LifecycleRegistry(this)
    
    // Saved state registry controller for state persistence
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    // Dependency injection: Repositories and AI engines
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    @Inject lateinit var textSuggestionRepository: TextSuggestionRepository
    @Inject lateinit var grammarEngine: GrammarEngine
    @Inject lateinit var spellingChecker: SpellingChecker
    @Inject lateinit var toneEngine: ToneEngine

    /**
     * Factory for creating ViewModel instances.
     * Provides dependencies required by KeyboardViewModel.
     */
    private val viewModelFactory by lazy {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(KeyboardViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return KeyboardViewModel(
                        application = application,
                        userPreferencesRepository = userPreferencesRepository,
                        textSuggestionRepository = textSuggestionRepository,
                        grammarEngine = grammarEngine,
                        spellingChecker = spellingChecker,
                        toneEngine = toneEngine
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }

    /**
     * Main ViewModel for keyboard state management.
     * Handles text input, suggestions, grammar checking, and tone transformation.
     */
    private val keyboardViewModel: KeyboardViewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[KeyboardViewModel::class.java]
    }

    /**
     * Initializes the service and sets up lifecycle components.
     * Called when the IME service is first created.
     */
    override fun onCreate() {
        super.onCreate()
        // Attach and restore saved state registry
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        // Initialize lifecycle
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        // Install view tree owners for Compose integration
        installViewTreeOwners()
    }

    /**
     * Cleans up resources when the service is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        internalViewModelStore.clear()
    }

    /**
     * Called when input starts in a new field.
     * Resets the keyboard state for the new input context.
     */
    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        keyboardViewModel.clearCurrentText()
        keyboardViewModel.updateInputContext(attribute)
    }

    /**
     * Called when the keyboard view is shown.
     * Updates lifecycle state to STARTED and RESUMED.
     */
    override fun onStartInputView(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(attribute, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    /**
     * Called when input finishes (keyboard is hidden).
     * Updates lifecycle state to PAUSED and STOPPED.
     */
    override fun onFinishInput() {
        super.onFinishInput()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    /**
     * Creates the main keyboard view using Jetpack Compose.
     * Sets up lifecycle owners for proper Compose integration.
     * 
     * @return The ComposeView containing the keyboard UI
     */
    override fun onCreateInputView(): View {
        // Create ComposeView with lifecycle-aware disposal strategy
        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }

        // Install view tree owners for Compose to access lifecycle, ViewModel, and saved state
        setViewTreeLifecycleOwner(composeView)
        setViewTreeViewModelStoreOwner(composeView)
        setViewTreeSavedStateRegistryOwner(composeView)

        // Set the Compose content
        composeView.setContent {
            KeyboardComposeView(
                viewModel = keyboardViewModel,
                onKeyPress = { key -> handleKeyPress(key) },
                onTextInput = { text -> handleTextInput(text) },
                onBackspace = { handleBackspace() },
                onEnter = { handleEnter() },
                onSpace = { handleSpace() },
                onSuggestionSelect = { suggestion -> handleSuggestionSelection(suggestion) },
                onVoiceInput = { handleVoiceInput() },
                onApplyGrammar = { corrected -> applyGrammarCorrection(corrected) }
            )
        }

        return composeView
    }

    /**
     * Handles individual key press events.
     * Commits the character to the input field and updates ViewModel state.
     */
    private fun handleKeyPress(key: String) {
        currentInputConnection?.commitText(key, 1)
        keyboardViewModel.onKeyPressed(key)
    }

    /**
     * Handles text input (e.g., from suggestions or voice input).
     */
    private fun handleTextInput(text: String) {
        currentInputConnection?.commitText(text, 1)
        keyboardViewModel.onTextInput(text)
    }

    /**
     * Handles backspace key press.
     * Deletes one character before the cursor.
     */
    private fun handleBackspace() {
        currentInputConnection?.deleteSurroundingText(1, 0)
        keyboardViewModel.onBackspace()
    }

    /**
     * Handles enter key press.
     * Inserts a newline (no automatic grammar checking).
     */
    private fun handleEnter() {
        currentInputConnection?.commitText("\n", 1)
        keyboardViewModel.onEnter()
    }

    /**
     * Handles space key press.
     * Inserts a space (no automatic grammar checking).
     */
    private fun handleSpace() {
        currentInputConnection?.commitText(" ", 1)
        keyboardViewModel.onSpace()
    }

    /**
     * Handles selection of a text suggestion.
     * Replaces the current text with the selected suggestion.
     */
    private fun handleSuggestionSelection(suggestion: String) {
        replaceCurrentTextWith(suggestion)
        keyboardViewModel.onSuggestionSelected(suggestion)
    }

    /**
     * Applies a grammar correction to the input field.
     */
    private fun applyGrammarCorrection(correctedText: String) {
        replaceCurrentTextWith(correctedText)
        keyboardViewModel.applyGrammarCorrection(correctedText)
    }

    /**
     * Applies a tone transformation to the input field.
     */
    private fun applyToneTransformation(transformedText: String) {
        replaceCurrentTextWith(transformedText)
        keyboardViewModel.applyToneResult(transformedText)
    }

    /**
     * Replaces the current text in the input field with new text.
     * First deletes the existing text, then inserts the new text.
     */
    private fun replaceCurrentTextWith(newText: String) {
        val connection = currentInputConnection ?: return
        val currentText = keyboardViewModel.currentText.value
        if (currentText.isNotEmpty()) {
            connection.deleteSurroundingText(currentText.length, 0)
        }
        connection.commitText(newText, 1)
    }

    /**
     * Placeholder for future voice input integration.
     */
    private fun handleVoiceInput() {
        // TODO: Implement voice input using SpeechRecognizer API
    }

    // Implementation of ViewModelStoreOwner interface
    override val viewModelStore: ViewModelStore
        get() = internalViewModelStore

    // Implementation of LifecycleOwner interface
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    // Implementation of SavedStateRegistryOwner interface
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    /**
     * Installs view tree owners on the window decor view.
     * This allows Compose components to access lifecycle, ViewModel, and saved state
     * through the view hierarchy, which is required for proper Compose integration
     * in InputMethodService.
     */
    private fun installViewTreeOwners() {
        val decorView = window?.window?.decorView ?: return
        setViewTreeLifecycleOwner(decorView)
        setViewTreeViewModelStoreOwner(decorView)
        setViewTreeSavedStateRegistryOwner(decorView)
    }

    /**
     * Sets the lifecycle owner in the view tree using reflection.
     * This is necessary because InputMethodService doesn't have direct access
     * to ViewTreeLifecycleOwner in some Android versions.
     * 
     * @param view The view to attach the lifecycle owner to
     */
    private fun setViewTreeLifecycleOwner(view: View) {
        try {
            val clazz = Class.forName("androidx.lifecycle.ViewTreeLifecycleOwner")
            val method = clazz.getMethod("set", View::class.java, LifecycleOwner::class.java)
            method.invoke(null, view, this)
        } catch (_: Throwable) {
            // No-op if LifecycleOwner helpers are missing (graceful degradation)
        }
    }

    /**
     * Sets the ViewModel store owner in the view tree using reflection.
     * 
     * @param view The view to attach the ViewModel store owner to
     */
    private fun setViewTreeViewModelStoreOwner(view: View) {
        try {
            val clazz = Class.forName("androidx.lifecycle.ViewTreeViewModelStoreOwner")
            val method = clazz.getMethod("set", View::class.java, ViewModelStoreOwner::class.java)
            method.invoke(null, view, this)
        } catch (_: Throwable) {
            // No-op if Lifecycle ViewModel helpers are missing (graceful degradation)
        }
    }

    /**
     * Sets the saved state registry owner in the view tree using reflection.
     * 
     * @param view The view to attach the saved state registry owner to
     */
    private fun setViewTreeSavedStateRegistryOwner(view: View) {
        try {
            val clazz = Class.forName("androidx.savedstate.ViewTreeSavedStateRegistryOwner")
            val method = clazz.getMethod("set", View::class.java, SavedStateRegistryOwner::class.java)
            method.invoke(null, view, this)
        } catch (_: Throwable) {
            // No-op if SavedState helpers are missing (graceful degradation)
        }
    }

}