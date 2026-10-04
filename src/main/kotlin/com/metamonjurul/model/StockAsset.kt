package com.metamonjurul.model

import kotlinx.serialization.Serializable

@Serializable
data class StockAsset(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fileName: String,
    val filePath: String,
    val fileType: FileType,
    val previewImagePath: String? = null,
    val metadata: AssetMetadata = AssetMetadata(),
    val marketplaceData: Map<Marketplace, MarketplaceMetadata> = emptyMap(),
    val status: ProcessingStatus = ProcessingStatus.PENDING,
    val importedAt: Long = System.currentTimeMillis()
)

enum class FileType {
    // Vector formats
    SVG, EPS, PDF, AI,
    // Raster image formats
    PNG, JPG, JPEG, WEBP, TIFF, BMP,
    // Video formats
    MP4, MOV, AVI, WEBM, MKV, M4V,
    // Audio formats
    WAV, MP3, AAC, FLAC, OGG, AIFF, M4A,
    UNKNOWN
}

enum class Marketplace {
    ADOBE_STOCK, POND5, SHUTTERSTOCK, FREEPIK
}

enum class ProcessingStatus {
    PENDING, PREVIEW_GENERATING, PREVIEW_READY, METADATA_GENERATING, METADATA_READY, EXPORT_READY, ERROR
}

@Serializable
data class AssetMetadata(
    val title: String = "",
    val description: String = "",
    val keywords: List<String> = emptyList(),
    val category: String = "",
    val isEditorial: Boolean = false,
    val isMature: Boolean = false,
    val isIllustration: Boolean = false,
    // Pond5 specific fields
    val copyright: String? = null,
    val price: Double? = null,
    val city: String? = null,
    val region: String? = null,
    val country: String? = null,
    val frameRendering: String? = null,
    val aspectRatio: String? = null,
    val publisher: String? = null,
    val composer: String? = null,
    val soundType: String? = null, // "sfx" or "music"
    val isPro: Boolean = false
) {
    fun copyWithPond5Fields(
        copyright: String? = this.copyright,
        price: Double? = this.price,
        city: String? = this.city,
        region: String? = this.region,
        country: String? = this.country,
        frameRendering: String? = this.frameRendering,
        aspectRatio: String? = this.aspectRatio,
        publisher: String? = this.publisher,
        composer: String? = this.composer,
        soundType: String? = this.soundType,
        isPro: Boolean = this.isPro
    ): AssetMetadata = copy(
        copyright = copyright,
        price = price,
        city = city,
        region = region,
        country = country,
        frameRendering = frameRendering,
        aspectRatio = aspectRatio,
        publisher = publisher,
        composer = composer,
        soundType = soundType,
        isPro = isPro
    )
}

fun FileType.isVector(): Boolean = this in setOf(SVG, EPS, PDF, AI)
fun FileType.isImage(): Boolean = this in setOf(PNG, JPG, JPEG, WEBP, TIFF, BMP)
fun FileType.isVideo(): Boolean = this in setOf(MP4, MOV, AVI, WEBM, MKV, M4V)
fun FileType.isAudio(): Boolean = this in setOf(WAV, MP3, AAC, FLAC, OGG, AIFF, M4A)

fun FileType.isImageOrVector(): Boolean = isImage() || isVector()
fun FileType.isVideoOrFootage(): Boolean = isVideo()
fun FileType.isAudio(): Boolean = isAudio()

fun FileType.defaultPond5Price(): Double = when {
    isAudio() -> 3.0
    isVideo() -> 49.0
    isVector() -> 49.0
    isImage() -> 49.0
    else -> 49.0
}

fun FileType.defaultPond5SoundType(): String = when {
    isAudio() -> "sfx"
    else -> ""
}

@Serializable
data class MarketplaceMetadata(
    val marketplace: Marketplace,
    val csvRow: Map<String, String> = emptyMap(),
    val isValid: Boolean = true,
    val validationErrors: List<String> = emptyList()
)

@Serializable
data class GeminiAnalysisResult(
    val title: String,
    val description: String,
    val keywords: List<String>,
    val suggestedCategory: String,
    val confidence: Double
)

@Serializable
data class AppSettings(
    val geminiApiKey: String = "",
    val defaultMarketplaces: Set<Marketplace> = setOf(Marketplace.ADOBE_STOCK, Marketplace.POND5, Marketplace.SHUTTERSTOCK, Marketplace.FREEPIK),
    val maxKeywords: Int = 50,
    val defaultLanguage: String = "en",
    val outputDirectory: String = System.getProperty("user.home") + "/MetaMonjurul/Exports",
    val previewQuality: PreviewQuality = PreviewQuality.HIGH
)

enum class PreviewQuality {
    LOW(512), MEDIUM(1024), HIGH(2048), ULTRA(4096)
    
    val maxDimension: Int
    constructor(maxDimension: Int) { this.maxDimension = maxDimension }
}