package com.metamonjurul.service

import com.metamonjurul.model.FileType
import com.metamonjurul.model.GeminiAnalysisResult
import com.metamonjurul.model.isAudio
import com.metamonjurul.model.isImage
import com.metamonjurul.model.isVector
import com.metamonjurul.model.isVideo
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LOGGER
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Base64

class GeminiService(private val apiKey: String) {

    companion object {
        // Never hardcode the key in git. Priority: Settings UI > env GEMINI_API_KEY.
        const val MODEL = "gemini-flash-latest"
        const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent"

        fun effectiveKey(configured: String?): String {
            if (!configured.isNullOrBlank()) return configured.trim()
            val env = System.getenv("GEMINI_API_KEY")?.trim().orEmpty()
            return env
        }
    }

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    @Serializable
    private data class TextPart(val text: String)

    @Serializable
    private data class InlineData(val mimeType: String, val data: String)

    @Serializable
    private data class InlineDataPart(val inlineData: InlineData)

    @Serializable
    private data class GeminiRequest(
        val contents: List<Content>,
        val generationConfig: GenerationConfig = GenerationConfig()
    )

    @Serializable
    private data class Content(val parts: List<kotlinx.serialization.json.JsonElement>)

    @Serializable
    private data class GenerationConfig(
        val temperature: Double = 0.7,
        val topK: Int = 40,
        val topP: Double = 0.95,
        val maxOutputTokens: Int = 2048,
        val responseMimeType: String = "application/json"
    )

    @Serializable
    private data class GeminiResponse(val candidates: List<Candidate> = emptyList())

    @Serializable
    private data class Candidate(val content: ContentResponse? = null)

    @Serializable
    private data class ContentResponse(val parts: List<PartResponse> = emptyList())

    @Serializable
    private data class PartResponse(val text: String = "")

    private fun key(): String = effectiveKey(apiKey)

    /** Simple text test — mirrors the curl: POST generateContent with X-goog-api-key header. */
    suspend fun testText(prompt: String = "Explain how AI works in a few words"): String =
        withContext(Dispatchers.IO) {
            require(key().isNotBlank()) { "Gemini API key missing. Set it in Settings or GEMINI_API_KEY env var." }
            val payload = buildJsonContent(listOf(TextPart(prompt)))
            val response = client.post(BASE_URL) {
                header("X-goog-api-key", key())
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            response.bodyAsText()
        }

    suspend fun analyzeImage(
        imagePath: String,
        context: String = "",
        fileType: FileType = FileType.UNKNOWN
    ): GeminiAnalysisResult = withContext(Dispatchers.IO) {
        require(key().isNotBlank()) { "Gemini API key missing. Set it in Settings or GEMINI_API_KEY env var." }
        val imageFile = File(imagePath)
        require(imageFile.exists()) { "Preview image not found: $imagePath" }
        val base64Image = Base64.getEncoder().encodeToString(imageFile.readBytes())
        val prompt = buildAnalysisPrompt(context, fileType)

        val payload = buildJsonContent(
            listOf(TextPart(prompt)),
            inlineImage = InlineData(mimeType = "image/png", data = base64Image)
        )

        val response: HttpResponse = client.post(BASE_URL) {
            header("X-goog-api-key", key())
            contentType(ContentType.Application.Json)
            setBody(payload)
        }

        if (response.status.value !in 200..299) {
            throw Exception("Gemini API error ${response.status.value}: ${response.bodyAsText().take(500)}")
        }
        parseResponse(response)
    }

    private fun buildJsonContent(
        textParts: List<TextPart>,
        inlineImage: InlineData? = null
    ): GeminiRequest {
        val parts = mutableListOf<kotlinx.serialization.json.JsonElement>()
        textParts.forEach { parts += json.encodeToJsonElement(TextPart.serializer(), it) }
        inlineImage?.let {
            parts += json.encodeToJsonElement(InlineDataPart.serializer(), InlineDataPart(it))
        }
        return GeminiRequest(contents = listOf(Content(parts)))
    }

    private fun buildAnalysisPrompt(context: String, fileType: FileType): String {
        val typeGuidelines = when {
            fileType.isVector() -> "- VECTOR GRAPHIC (SVG/EPS/AI/PDF). Keywords: vector, scalable, editable, paths, Illustrator, SVG, EPS. Uses: logo, branding, print, infographics."
            fileType.isImage() -> "- RASTER IMAGE (PNG/JPG/WEBP/TIFF). Keywords: high resolution, photo, texture, background. Uses: web, print, social, marketing."
            fileType.isVideo() -> "- VIDEO FOOTAGE (MP4/MOV/AVI). Keywords: footage, stock video, cinematic, 4K, HD. Uses: commercials, films, social."
            fileType.isAudio() -> "- AUDIO (WAV/MP3/FLAC). Keywords: sound effect, SFX, music, loop, royalty free. Uses: video, games, podcasts."
            else -> ""
        }
        return """
            You are an expert stock marketplace metadata specialist. Analyze this preview and generate commercially optimized metadata for Adobe Stock, Shutterstock, Pond5, Freepik.

            File Type: ${fileType.name}
            Context: $context
            $typeGuidelines

            Respond with JSON only, exactly these fields:
            {"title":"Commercial title max 200 chars","description":"Detailed description max 500 chars with use cases","keywords":["k1","k2"],"suggestedCategory":"category","confidence":0.95}

            Rules: title SEO-optimized; description commercial + applications; keywords 30-50, most important first, mix broad+specific; for gold/abstract work include glassmorphism, abstract, geometric, gradient, yellow, gold, translucent, blur, refractive, lighting.
        """.trimIndent()
    }

    private suspend fun parseResponse(response: HttpResponse): GeminiAnalysisResult {
        val body = response.bodyAsText()
        val parsed = try {
            json.decodeFromString(GeminiResponse.serializer(), body)
        } catch (e: Exception) {
            throw Exception("Invalid Gemini response: ${body.take(500)}")
        }
        val text = parsed.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from Gemini: ${body.take(500)}")
        val cleaned = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        return try {
            json.decodeFromString(GeminiAnalysisResult.serializer(), cleaned)
        } catch (e: Exception) {
            parseFallback(cleaned)
        }
    }

    private fun parseFallback(text: String): GeminiAnalysisResult {
        val title = extractField(text, "title") ?: "Abstract Glassmorphism Design"
        val description = extractField(text, "description")
            ?: "High-quality abstract glassmorphism composition with translucent geometric shapes and golden gradients."
        return GeminiAnalysisResult(
            title = title,
            description = description,
            keywords = extractKeywords(text),
            suggestedCategory = extractField(text, "suggestedCategory") ?: "Backgrounds/Textures",
            confidence = 0.7
        )
    }

    private fun extractField(text: String, field: String): String? {
        return "\"$field\"\\s*:\\s*\"([^\"]*)\"".toRegex().find(text)?.groupValues?.get(1)
    }

    private fun extractKeywords(text: String): List<String> {
        val m = "\"keywords\"\\s*:\\s*\\[([^\\]]*)\\]".toRegex().find(text)
        if (m != null) {
            return m.groupValues[1].split(",").map { it.trim().trim('"') }.filter { it.isNotBlank() }
        }
        return listOf("abstract", "glassmorphism", "geometric", "gradient", "golden", "yellow", "translucent", "blur", "refractive", "lighting", "commercial", "background", "design", "vector", "stock")
    }

    fun close() = client.close()
}
