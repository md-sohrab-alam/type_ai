# Spelling & Grammar Check Implementation Plan

## 🎯 Goal
Build a production-ready spelling and grammar checking system that works seamlessly with the keyboard.

## 📊 Samsung AI Keyboard Features Analysis

Based on modern Samsung keyboards, they typically include:

### **Core Features:**
1. **Real-time Grammar Check** - Underlines errors as you type
2. **Spelling Suggestions** - Shows corrections above keyboard
3. **Auto-Correction** - Automatically fixes common mistakes
4. **Context-Aware Suggestions** - Smart word predictions
5. **Multi-language Support** - Works across languages
6. **Learning** - Adapts to user's writing style

### **UI Elements:**
- **Suggestion Bar** - Above keyboard with correction options
- **Underline Indicators** - Red for spelling, blue for grammar
- **Quick Actions** - One-tap to apply corrections
- **Confidence Indicators** - Shows how certain the correction is

## 🏗️ Architecture Design

### **Component Structure**

```
┌─────────────────────────────────────────────┐
│         Keyboard UI Layer                    │
│  (AnySoftKeyboard or Custom Compose)        │
└─────────────────┬───────────────────────────┘
                  │
                  ↓
┌─────────────────────────────────────────────┐
│      Text Input Handler                      │
│  - Captures all text input                   │
│  - Tracks cursor position                    │
│  - Monitors text changes                     │
└─────────────────┬───────────────────────────┘
                  │
                  ↓
┌─────────────────────────────────────────────┐
│      Grammar & Spelling Engine               │
│  ┌─────────────────────────────────────┐    │
│  │  GrammarEngine (Current)            │    │
│  │  - Heuristic rules                  │    │
│  │  - Basic corrections                │    │
│  └─────────────────────────────────────┘    │
│  ┌─────────────────────────────────────┐    │
│  │  ML Kit Proofreading (New)          │    │
│  │  - Advanced grammar checking        │    │
│  │  - On-device processing             │    │
│  └─────────────────────────────────────┘    │
│  ┌─────────────────────────────────────┐    │
│  │  LanguageTool Integration (New)     │    │
│  │  - Comprehensive grammar rules      │    │
│  │  - Multi-language support           │    │
│  └─────────────────────────────────────┘    │
└─────────────────┬───────────────────────────┘
                  │
                  ↓
┌─────────────────────────────────────────────┐
│      Suggestion UI Layer                     │
│  - Suggestion bar above keyboard             │
│  - Underline indicators                      │
│  - Quick action buttons                      │
└─────────────────────────────────────────────┘
```

## 🔧 Implementation Details

### **Phase 1: Enhance Current GrammarEngine**

**Current State:**
- Basic heuristic rules
- Simple corrections (capitalization, pronouns, spaces)
- Limited accuracy

**Enhancements Needed:**

```kotlin
class GrammarEngine @Inject constructor() {
    
    // Add ML Kit integration
    private val mlKitProofreader: Proofreader? = null
    
    // Add LanguageTool client
    private val languageToolClient: LanguageToolClient? = null
    
    /**
     * Enhanced grammar checking with multiple engines
     */
    suspend fun checkGrammar(
        text: String,
        language: String = "en"
    ): GrammarResult = withContext(Dispatchers.IO) {
        
        // 1. Quick heuristic check (fast, on-device)
        val heuristicResult = runHeuristicChecks(text, language)
        
        // 2. ML Kit check (accurate, on-device)
        val mlKitResult = checkWithMLKit(text, language)
        
        // 3. LanguageTool check (comprehensive, optional)
        val languageToolResult = checkWithLanguageTool(text, language)
        
        // 4. Merge results with confidence scores
        mergeResults(heuristicResult, mlKitResult, languageToolResult)
    }
    
    /**
     * Real-time checking for word-level corrections
     */
    suspend fun checkWord(
        word: String,
        context: String,
        position: Int
    ): WordCorrection? {
        // Check spelling
        // Check grammar in context
        // Return correction if found
    }
}
```

### **Phase 2: Add ML Kit Proofreading**

**Dependencies:**
```gradle
implementation("com.google.mlkit:genai-proofreading:1.0.0-beta1")
```

**Implementation:**
```kotlin
class MLKitGrammarChecker {
    private val proofreader: Proofreader = Proofreading.getClient(
        ProofreaderOptions.Builder()
            .setLanguageTag("en")
            .build()
    )
    
    suspend fun checkText(text: String): ProofreadingResult {
        return proofreader.proofread(text)
    }
}
```

**Features:**
- ✅ On-device processing (privacy)
- ✅ Fast response times
- ✅ Works offline
- ✅ Supports multiple languages

### **Phase 3: Add LanguageTool Integration**

**Dependencies:**
```gradle
implementation("org.languagetool:language-all:6.4")
```

**Features:**
- ✅ Comprehensive grammar rules
- ✅ 40+ languages
- ✅ Detailed explanations
- ⚠️ Requires internet (or large on-device model)

### **Phase 4: Real-time Suggestion UI**

**Components:**

1. **SuggestionBar** (Above keyboard)
```kotlin
@Composable
fun SuggestionBar(
    suggestions: List<Correction>,
    onApply: (Correction) -> Unit,
    onDismiss: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        items(suggestions) { correction ->
            SuggestionChip(
                text = correction.corrected,
                explanation = correction.explanation,
                confidence = correction.confidence,
                onClick = { onApply(correction) }
            )
        }
    }
}
```

2. **Text Underline Indicators**
```kotlin
@Composable
fun TextWithUnderlines(
    text: String,
    corrections: List<Correction>
) {
    // Render text with colored underlines
    // Red for spelling errors
    // Blue for grammar errors
}
```

3. **Quick Action Buttons**
```kotlin
@Composable
fun QuickActionBar(
    correction: Correction,
    onApply: () -> Unit,
    onIgnore: () -> Unit,
    onLearn: () -> Unit
) {
    Row {
        Button(onClick = onApply) { Text("Apply") }
        Button(onClick = onIgnore) { Text("Ignore") }
        Button(onClick = onLearn) { Text("Learn") }
    }
}
```

## 📋 Feature Implementation Checklist

### **Spelling Check**
- [ ] Word-level spelling detection
- [ ] Dictionary integration
- [ ] Custom word dictionary
- [ ] Auto-correction toggle
- [ ] Underline indicators
- [ ] Suggestion display

### **Grammar Check**
- [ ] Sentence-level grammar checking
- [ ] Context-aware corrections
- [ ] Punctuation fixes
- [ ] Capitalization rules
- [ ] Subject-verb agreement
- [ ] Tense consistency

### **UI/UX**
- [ ] Suggestion bar above keyboard
- [ ] Underline indicators in text
- [ ] One-tap correction
- [ ] Ignore option
- [ ] Learn from user corrections
- [ ] Confidence indicators

### **Performance**
- [ ] Real-time checking (< 100ms)
- [ ] Debouncing for efficiency
- [ ] Caching common corrections
- [ ] Background processing
- [ ] Battery optimization

## 🚀 Implementation Phases

### **Phase 1: Foundation (Week 1-2)**
1. Enhance GrammarEngine with better heuristics
2. Add word-level spelling checking
3. Implement basic suggestion bar UI
4. Add underline indicators

### **Phase 2: ML Integration (Week 3-4)**
1. Integrate ML Kit Proofreading
2. Add LanguageTool integration (optional)
3. Merge results from multiple engines
4. Improve accuracy

### **Phase 3: Polish (Week 5-6)**
1. Optimize performance
2. Add user preferences
3. Implement learning system
4. Add multi-language support

### **Phase 4: Testing & Refinement (Week 7-8)**
1. User testing
2. Performance optimization
3. Bug fixes
4. Feature refinement

## 🎨 UI Mockup

```
┌─────────────────────────────────────────┐
│  [Text Input Field]                     │
│  Hello, how are you doing?              │
│  ─── (blue underline for grammar)       │
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│  Suggestions:                           │
│  [Hello, how are you?] [Hello! How...] │
│  ─────────────────────────────────────  │
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│  Q W E R T Y U I O P                    │
│   A S D F G H J K L                     │
│    Z X C V B N M                        │
│  [🎤] [        SPACE        ] [⌫] [↵]  │
└─────────────────────────────────────────┘
```

## 📊 Success Metrics

1. **Accuracy**: > 90% correct suggestions
2. **Speed**: < 100ms response time
3. **User Satisfaction**: > 4.5/5 rating
4. **Adoption**: > 70% users enable feature
5. **Correction Rate**: > 60% corrections applied

## 🔒 Privacy Considerations

1. **On-Device Processing**: Prefer ML Kit (on-device)
2. **Optional Cloud**: LanguageTool only if user opts in
3. **No Data Collection**: Don't send user text to servers
4. **Local Dictionary**: Store custom words locally
5. **Transparent**: Clear privacy policy

---

**Next Steps:**
1. Review and approve this plan
2. Set up development environment
3. Start Phase 1 implementation
4. Regular progress reviews

