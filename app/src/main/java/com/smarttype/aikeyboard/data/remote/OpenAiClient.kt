package com.smarttype.aikeyboard.data.remote

import android.util.Log
import com.smarttype.aikeyboard.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAiClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private const val OPENAI_CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions"
        private const val DEFAULT_MODEL = "gpt-4o-mini"
        private const val TAG = "OpenAiClient"
    }

    suspend fun generateSuggestions(
        currentText: String,
        contextHint: String,
        limit: Int
    ): List<String> = withContext(ioDispatcher) {
        val apiKey = BuildConfig.OPENAI_API_KEY
        if (apiKey.isBlank()) {
            Log.w(TAG, "OpenAI API key is missing; skipping AI suggestions.")
            return@withContext emptyList<String>()
        }

        val requestJson = buildRequestPayload(currentText, contextHint, limit)
        val requestBody = requestJson.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(OPENAI_CHAT_COMPLETIONS_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        return@withContext try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "OpenAI request failed: ${response.code} ${response.message}")
                    return@withContext emptyList<String>()
                }

                val body = response.body?.string().orEmpty()
                if (body.isBlank()) {
                    return@withContext emptyList<String>()
                }

                parseResponse(body)
            }
        } catch (io: IOException) {
            Log.e(TAG, "OpenAI request error", io)
            emptyList()
        }
    }

    private fun buildRequestPayload(
        currentText: String,
        contextHint: String,
        limit: Int
    ): JSONObject {
        val systemMessage = "You are a helpful keyboard assistant that offers short phrase " +
            "suggestions. Respond ONLY with a JSON array of strings."

        val trimmedInput = currentText.takeLast(200)
        val prompt = buildString {
            append("Current context: ")
            append(contextHint)
            append(". ")
            append("User text: \"")
            append(trimmedInput.ifBlank { " " })
            append("\". ")
            append("Suggest up to $limit natural completions or next phrases. Keep each suggestion under 12 words.")
        }

        val messages = JSONArray()
            .put(
                JSONObject().apply {
                    put("role", "system")
                    put("content", systemMessage)
                }
            )
            .put(
                JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                }
            )

        return JSONObject().apply {
            put("model", DEFAULT_MODEL)
            put("messages", messages)
            put("temperature", 0.7)
            put("max_tokens", 120)
        }
    }

    private fun parseResponse(raw: String): List<String> {
        return runCatching {
            val root = JSONObject(raw)
            val choices = root.optJSONArray("choices") ?: return emptyList()
            if (choices.length() == 0) return emptyList()

            val firstChoice = choices.optJSONObject(0) ?: return emptyList()
            val message = firstChoice.optJSONObject("message") ?: return emptyList()
            var content = message.optString("content").orEmpty().trim()
            if (content.startsWith("```")) {
                content = content.removePrefix("```json")
                    .removePrefix("```JSON")
                    .removePrefix("```")
                    .trim()
            }

            val jsonArray = JSONArray(content)
            buildList {
                for (i in 0 until jsonArray.length()) {
                    val value = jsonArray.optString(i).trim()
                    if (value.isNotBlank()) add(value)
                }
            }
        }.getOrElse {
            Log.e(TAG, "Failed to parse OpenAI response", it)
            emptyList()
        }
    }
    
    /**
     * Corrects grammar and spelling using OpenAI.
     * Returns the corrected text with proper grammar, spelling, and punctuation.
     */
    suspend fun correctGrammarAndSpelling(
        text: String
    ): String = withContext(ioDispatcher) {
        val apiKey = BuildConfig.OPENAI_API_KEY.trim()
        Log.d(TAG, "OpenAI API key check: key length = ${apiKey.length}, isBlank = ${apiKey.isBlank()}, starts with = ${apiKey.take(7)}")
        if (apiKey.isBlank()) {
            Log.w(TAG, "OpenAI API key is missing; cannot correct grammar.")
            return@withContext text
        }
        Log.d(TAG, "Calling OpenAI API for grammar correction. Text: \"$text\"")

        val systemMessage = "You are a grammar and spelling correction assistant. " +
            "Correct the user's text for grammar, spelling, punctuation, and clarity. " +
            "Return ONLY the corrected text, nothing else. " +
            "Preserve the original meaning and tone. " +
            "Expand abbreviations appropriately (e.g., 'r' -> 'are', 'u' -> 'you')."

        val userMessage = "Correct this text: \"$text\""

        val messages = JSONArray()
            .put(
                JSONObject().apply {
                    put("role", "system")
                    put("content", systemMessage)
                }
            )
            .put(
                JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                }
            )

        val requestJson = JSONObject().apply {
            put("model", DEFAULT_MODEL)
            put("messages", messages)
            put("temperature", 0.3) // Lower temperature for more consistent corrections
            put("max_tokens", 500)
        }

        val requestBody = requestJson.toString()
            .toRequestBody("application/json".toMediaType())

        val trimmedKey = apiKey.trim()
        Log.d(TAG, "API Key length: ${trimmedKey.length}, starts with: ${trimmedKey.take(10)}...")
        
        val request = Request.Builder()
            .url(OPENAI_CHAT_COMPLETIONS_URL)
            .addHeader("Authorization", "Bearer $trimmedKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        return@withContext try {
            okHttpClient.newCall(request).execute().use { response ->
                Log.d(TAG, "OpenAI API response: code = ${response.code}, message = ${response.message}")
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No error body"
                    Log.e(TAG, "OpenAI grammar correction failed: ${response.code} ${response.message}")
                    Log.e(TAG, "Error response body: $errorBody")
                    
                    // 401 means unauthorized - API key issue
                    if (response.code == 401) {
                        Log.e(TAG, "401 Unauthorized - Check if API key is valid and properly configured in gradle.properties")
                        Log.e(TAG, "API Key in BuildConfig length: ${BuildConfig.OPENAI_API_KEY.length}")
                        Log.e(TAG, "API Key first 20 chars: ${BuildConfig.OPENAI_API_KEY.take(20)}")
                    }
                    return@withContext text
                }

                val body = response.body?.string().orEmpty()
                Log.d(TAG, "OpenAI API response body length: ${body.length}")
                if (body.isBlank()) {
                    Log.w(TAG, "OpenAI API returned empty response body")
                    return@withContext text
                }

                val corrected = parseGrammarCorrectionResponse(body)
                Log.d(TAG, "OpenAI correction result: \"$corrected\"")
                corrected ?: text
            }
        } catch (io: IOException) {
            Log.e(TAG, "OpenAI grammar correction IO error", io)
            text
        } catch (e: Exception) {
            Log.e(TAG, "OpenAI grammar correction error", e)
            text
        }
    }
    
    private fun parseGrammarCorrectionResponse(raw: String): String? {
        return runCatching {
            val root = JSONObject(raw)
            val choices = root.optJSONArray("choices") ?: return null
            if (choices.length() == 0) return null

            val firstChoice = choices.optJSONObject(0) ?: return null
            val message = firstChoice.optJSONObject("message") ?: return null
            val content = message.optString("content").orEmpty().trim()
            
            // Remove quotes if the response is wrapped in quotes
            content.removeSurrounding("\"").trim()
        }.getOrElse {
            Log.e(TAG, "Failed to parse OpenAI grammar correction response", it)
            null
        }
    }
}

