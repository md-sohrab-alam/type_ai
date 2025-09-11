package com.smarttype.aikeyboard.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarttype.aikeyboard.ui.components.FeatureCard
import com.smarttype.aikeyboard.ui.settings.SettingsActivity
import android.provider.Settings as AndroidSettings
import com.smarttype.aikeyboard.ui.theme.SmartTypeAIKeyboardTheme
import com.smarttype.aikeyboard.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SmartTypeAIKeyboardTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val isKeyboardEnabled by viewModel.isKeyboardEnabled.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SmartType AI Keyboard",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            context.startActivity(Intent(context, SettingsActivity::class.java))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Welcome Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Welcome to SmartType AI Keyboard",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "An AI-powered keyboard that helps you communicate better with perfect grammar, appropriate tone, and intelligent suggestions.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            item {
                // Setup Instructions
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Setup Instructions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (!isKeyboardEnabled) {
                            Text(
                                text = "1. Enable SmartType AI Keyboard in your device settings",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = {
                                    val intent = Intent(AndroidSettings.ACTION_INPUT_METHOD_SETTINGS)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Open Keyboard Settings")
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "2. Select SmartType AI Keyboard as your default keyboard",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text(
                                text = "✅ SmartType AI Keyboard is enabled and ready to use!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            item {
                Text(
                    text = "Features",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Feature Cards
            val features = listOf(
                FeatureInfo(
                    title = "Smart Grammar Correction",
                    description = "Real-time grammar, spelling, and punctuation correction in 50+ languages",
                    icon = "✍️"
                ),
                FeatureInfo(
                    title = "Intelligent Tone Adjustment",
                    description = "Transform your text with 15+ tone options including professional, casual, and creative",
                    icon = "🎭"
                ),
                FeatureInfo(
                    title = "AI-Powered Suggestions",
                    description = "Context-aware word and phrase suggestions that learn from your writing style",
                    icon = "🧠"
                ),
                FeatureInfo(
                    title = "Enhanced Glide Typing",
                    description = "Smooth and accurate glide typing with customizable sensitivity",
                    icon = "👆"
                ),
                FeatureInfo(
                    title = "Voice Input Integration",
                    description = "High-accuracy speech-to-text with multilingual support",
                    icon = "🎤"
                ),
                FeatureInfo(
                    title = "Privacy-First Design",
                    description = "On-device AI processing ensures your data stays private",
                    icon = "🔒"
                ),
                FeatureInfo(
                    title = "Customizable Themes",
                    description = "50+ beautiful themes with dark mode and accessibility options",
                    icon = "🎨"
                ),
                FeatureInfo(
                    title = "Cross-Platform Sync",
                    description = "Sync your preferences and writing style across all devices",
                    icon = "☁️"
                )
            )
            
            items(features) { feature ->
                FeatureCard(
                    title = feature.title,
                    description = feature.description,
                    icon = feature.icon
                )
            }
            
            item {
                // Premium Features
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🚀 Premium Features",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Unlock advanced AI features, unlimited usage, and priority support",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { /* Handle premium upgrade */ }
                        ) {
                            Text("Upgrade to Premium")
                        }
                    }
                }
            }
        }
    }
}

data class FeatureInfo(
    val title: String,
    val description: String,
    val icon: String
)
