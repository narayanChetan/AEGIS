package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Moshi Request Schema ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val tools: List<Tool>? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Tool(
    val googleSearch: GoogleSearchObject? = null
)

@JsonClass(generateAdapter = true)
class GoogleSearchObject // empty brackets {} represented in Json as empty class

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val thinkingConfig: ThinkingConfig? = null
)

@JsonClass(generateAdapter = true)
data class ThinkingConfig(
    val thinkingLevel: String? = null // "high"
)

// --- Moshi Response Schema ---

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null,
    val usageMetadata: UsageMetadata? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null,
    val groundingMetadata: GroundingMetadata? = null
)

@JsonClass(generateAdapter = true)
data class GroundingMetadata(
    val webSearchQueries: List<String>? = null,
    val searchEntryPoint: SearchEntryPoint? = null
)

@JsonClass(generateAdapter = true)
data class SearchEntryPoint(
    val renderedContent: String? = null
)

@JsonClass(generateAdapter = true)
data class UsageMetadata(
    val promptTokenCount: Int? = null,
    val candidatesTokenCount: Int? = null,
    val totalTokenCount: Int? = null
)

// --- Retrofit Service Interface ---

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

/**
 * Centered Repository to coordinate the multi-intelligence dynamic routing
 * for Aegis. Direct endpoints communicate securely using Secrets-provided API keys.
 */
class GeminiRepository {
    private val apiKey: String = BuildConfig.GEMINI_API_KEY

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(logger)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Determines whether we use High Thinking Mode, Low-latency Mode, or Grounded Search Mode.
     * @param prompt User's question or instruction.
     * @param mode "THINKING", "LOW_LATENCY", or "SEARCH"
     * @param systemInstruction Custom instruction profile tailored to Aegis AI.
     */
    suspend fun askAegis(
        prompt: String,
        mode: String,
        systemInstruction: String = "You are Aegis, a highly advanced super-intelligence assistant similar to Jarvis. Keep voice output responses concise, direct, helpful, and professionally loyal, maintaining a slightly futuristic tone."
    ): AegisQueryResult = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext AegisQueryResult(
                text = "My core neural matrix has not yet been connected to a Google Gemini API Key. Please configure your key in the secure settings panel or your workspace environment.",
                sourcesHtml = null,
                usageInfo = "Security Status: API key unconfigured"
            )
        }

        val requestModel: String
        val config: GenerationConfig?
        val toolsList: List<Tool>?

        when (mode) {
            "THINKING" -> {
                // gemini-3.1-pro-preview with Thinking Level set to high
                requestModel = "gemini-3.1-pro-preview"
                config = GenerationConfig(
                    thinkingConfig = ThinkingConfig(thinkingLevel = "high")
                    // Do NOT set maxOutputTokens per instructions
                )
                toolsList = null
            }
            "SEARCH" -> {
                // gemini-3.5-flash with search grounding tool
                requestModel = "gemini-3.5-flash"
                config = GenerationConfig(temperature = 0.5f)
                toolsList = listOf(Tool(googleSearch = GoogleSearchObject()))
            }
            else -> {
                // Default: LOW_LATENCY with gemini-3.1-flash-lite-preview
                requestModel = "gemini-3.1-flash-lite-preview"
                config = GenerationConfig(temperature = 0.7f)
                toolsList = null
            }
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = config,
            tools = toolsList,
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )

        try {
            val response = apiService.generateContent(requestModel, apiKey, request)
            val candidate = response.candidates?.firstOrNull()
            val textValue = candidate?.content?.parts?.firstOrNull()?.text ?: "I encountered a void of responses."
            val modelLabel = when (mode) {
                "THINKING" -> "Gemini-3.1 Pro (Thinking Mode)"
                "SEARCH" -> "Gemini-3.5 Flash (Google Search Grounded)"
                else -> "Gemini-3.1 Flash Lite (Low-Latency Response)"
            }

            val tokenInfo = "Model: $modelLabel | Tokens: P:${response.usageMetadata?.promptTokenCount ?: 0}, C:${response.usageMetadata?.candidatesTokenCount ?: 0}"
            val searchGroundedSourcesHtml = candidate?.groundingMetadata?.searchEntryPoint?.renderedContent

            AegisQueryResult(
                text = textValue,
                sourcesHtml = searchGroundedSourcesHtml,
                usageInfo = tokenInfo
            )
        } catch (e: Exception) {
            e.printStackTrace()
            AegisQueryResult(
                text = "My vocal processors encountered a network sync failure. Direct error alert: ${e.localizedMessage ?: "Core signal disruption"}",
                sourcesHtml = null,
                usageInfo = "Status: Connection offline"
            )
        }
    }
}

data class AegisQueryResult(
    val text: String,
    val sourcesHtml: String? = null,
    val usageInfo: String
)
