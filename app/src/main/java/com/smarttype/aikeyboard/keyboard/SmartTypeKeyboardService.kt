package com.smarttype.aikeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SmartTypeKeyboardService : InputMethodService() {
    
    private var keyboardView: View? = null
    
    override fun onCreateInputView(): View {
        // Create a simple keyboard layout
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        
        // Add title
        val title = TextView(this).apply {
            text = "SmartType AI Keyboard"
            textSize = 18f
            setPadding(0, 0, 0, 16)
        }
        layout.addView(title)
        
        // Add first row of keys
        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P").forEach { key ->
            val button = Button(this).apply {
                text = key
                setOnClickListener { handleKeyPress(key) }
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            row1.addView(button)
        }
        layout.addView(row1)
        
        // Add second row of keys
        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L").forEach { key ->
            val button = Button(this).apply {
                text = key
                setOnClickListener { handleKeyPress(key) }
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            row2.addView(button)
        }
        layout.addView(row2)
        
        // Add third row of keys
        val row3 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        listOf("Z", "X", "C", "V", "B", "N", "M").forEach { key ->
            val button = Button(this).apply {
                text = key
                setOnClickListener { handleKeyPress(key) }
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            row3.addView(button)
        }
        layout.addView(row3)
        
        // Add bottom row with special keys
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        
        val backspaceButton = Button(this).apply {
            text = "⌫"
            setOnClickListener { handleBackspace() }
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        bottomRow.addView(backspaceButton)
        
        val spaceButton = Button(this).apply {
            text = "Space"
            setOnClickListener { handleSpace() }
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
        }
        bottomRow.addView(spaceButton)
        
        val enterButton = Button(this).apply {
            text = "Enter"
            setOnClickListener { handleEnter() }
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        bottomRow.addView(enterButton)
        
        layout.addView(bottomRow)
        
        keyboardView = layout
        return layout
    }
    
    private fun handleKeyPress(key: String) {
        currentInputConnection?.commitText(key, 1)
    }
    
    private fun handleBackspace() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }
    
    private fun handleEnter() {
        currentInputConnection?.commitText("\n", 1)
    }
    
    private fun handleSpace() {
        currentInputConnection?.commitText(" ", 1)
    }
}