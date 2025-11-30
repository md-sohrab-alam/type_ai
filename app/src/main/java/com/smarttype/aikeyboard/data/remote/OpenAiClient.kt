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
}

