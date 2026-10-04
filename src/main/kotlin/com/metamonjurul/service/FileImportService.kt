package com.metamonjurul.service

import com.metamonjurul.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.extension

class FileImportService {
    
    companion object {
        fun detectFileType(file: File): FileType {
            val extension = file.extension?.lowercase() ?: return FileType.UNKNOWN
            return when (extension) {
                // Vector formats
                "svg" -> FileType.SVG
                "eps" -> FileType.EPS
                "pdf" -> FileType.PDF
                "ai" -> FileType.AI
                // Raster image formats
                "png" -> FileType.PNG
                "jpg", "jpeg" -> FileType.JPG
                "webp" -> FileType.WEBP
                "tiff", "tif" -> FileType.TIFF
                "bmp" -> FileType.BMP
                // Video formats
                "mp4" -> FileType.MP4
                "mov" -> FileType.MOV
                "avi" -> FileType.AVI
                "webm" -> FileType.WEBM
                "mkv" -> FileType.MKV
                "m4v" -> FileType.M4V
                // Audio formats
                "wav" -> FileType.WAV
                "mp3" -> FileType.MP3
                "aac" -> FileType.AAC
                "flac" -> FileType.FLAC
                "ogg" -> FileType.OGG
                "aiff", "aif" -> FileType.AIFF
                "m4a" -> FileType.M4A
                else -> FileType.UNKNOWN
            }
        }
    }
    
    suspend fun importFiles(files: List<File>, settings: AppSettings): List<StockAsset> = withContext(Dispatchers.IO) {
        files.mapNotNull { file ->
            try {
                val fileType = detectFileType(file)
                if (fileType == FileType.UNKNOWN) return@mapNotNull null
                
                val previewDir = File(settings.outputDirectory, "previews")
                previewDir.mkdirs()
                
                val previewFileName = "${file.nameWithoutExtension}_preview.png"
                val previewFile = File(previewDir, previewFileName)
                
                StockAsset(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    fileType = fileType,
                    previewImagePath = if (previewFile.exists()) previewFile.absolutePath else null
                )
            } catch (e: Exception) {
                null
            }
        }
    }
    
    fun copyToImportDirectory(sourceFile: File, targetDir: File): File {
        targetDir.mkdirs()
        val targetFile = File(targetDir, sourceFile.name)
        sourceFile.copyTo(targetFile, overwrite = true)
        return targetFile
    }
}