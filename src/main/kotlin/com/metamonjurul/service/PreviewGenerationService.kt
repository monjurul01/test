package com.metamonjurul.service

import com.metamonjurul.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.extension

class PreviewGenerationService {
    
    suspend fun generatePreview(asset: StockAsset, settings: AppSettings): StockAsset = withContext(Dispatchers.IO) {
        return@withContext try {
            val previewDir = File(settings.outputDirectory, "previews")
            previewDir.mkdirs()
            
            val previewFileName = "${File(asset.filePath).nameWithoutExtension}_preview.png"
            val previewFile = File(previewDir, previewFileName)
            
            val previewImage = when (asset.fileType) {
                FileType.SVG -> renderSvgPreview(asset.filePath, settings.previewQuality)
                FileType.EPS -> renderEpsPreview(asset.filePath, settings.previewQuality)
                FileType.PDF, FileType.AI -> renderVectorPreview(asset.filePath, settings.previewQuality)
                FileType.PNG, FileType.JPG, FileType.JPEG, FileType.WEBP, FileType.TIFF, FileType.BMP -> renderRasterPreview(asset.filePath, settings.previewQuality)
                FileType.MP4, FileType.MOV, FileType.AVI, FileType.WEBM, FileType.MKV, FileType.M4V -> renderVideoPreview(asset.filePath, settings.previewQuality)
                FileType.WAV, FileType.MP3, FileType.AAC, FileType.FLAC, FileType.OGG, FileType.AIFF, FileType.M4A -> renderAudioPreview(asset.filePath, settings.previewQuality)
                else -> createPlaceholderPreview(asset.fileType, settings.previewQuality)
            }
            
            if (previewImage != null) {
                ImageIO.write(previewImage, "png", previewFile)
                asset.copy(previewImagePath = previewFile.absolutePath, status = ProcessingStatus.PREVIEW_READY)
            } else {
                asset.copy(status = ProcessingStatus.ERROR)
            }
        } catch (e: Exception) {
            asset.copy(status = ProcessingStatus.ERROR)
        }
    }
    
    private fun renderSvgPreview(filePath: String, quality: PreviewQuality): BufferedImage? {
        return try {
            val svgFile = File(filePath)
            val svgContent = svgFile.readText()
            
            // Use Batik for SVG rendering (would need batik dependency)
            // For now, create a high-quality placeholder that represents the SVG
            createVectorPlaceholder(svgContent, quality)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun renderEpsPreview(filePath: String, quality: PreviewQuality): BufferedImage? {
        // EPS rendering would require Ghostscript or similar
        // For now, create a placeholder
        createVectorPlaceholder("EPS File", quality)
    }
    
    private fun renderVectorPreview(filePath: String, quality: PreviewQuality): BufferedImage? {
        // For PDF/AI, create a vector placeholder
        createVectorPlaceholder("Vector File", quality)
    }
    
    private fun renderVideoPreview(filePath: String, quality: PreviewQuality): BufferedImage? {
        // Video preview would require FFmpeg or similar to extract a frame
        // For now, create a video placeholder with film strip icon
        createVideoPlaceholder(quality)
    }
    
    private fun renderAudioPreview(filePath: String, quality: PreviewQuality): BufferedImage? {
        // Audio preview would show waveform or spectrogram
        // For now, create an audio placeholder with waveform visualization
        createAudioPlaceholder(quality)
    }
    
    private fun renderRasterPreview(filePath: String, quality: PreviewQuality): BufferedImage? {
        return try {
            val originalImage = ImageIO.read(File(filePath))
            if (originalImage == null) return null
            
            val maxDim = quality.maxDimension
            val (newWidth, newHeight) = calculateScaledDimensions(
                originalImage.width, originalImage.height, maxDim
            )
            
            val scaledImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
            val g2d = scaledImage.createGraphics()
            g2d.renderingHints = mapOf(
                RenderingHints.KEY_INTERPOLATION to RenderingHints.VALUE_INTERPOLATION_BICUBIC,
                RenderingHints.KEY_RENDERING to RenderingHints.VALUE_RENDER_QUALITY,
                RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON
            )
            g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null)
            g2d.dispose()
            
            scaledImage
        } catch (e: Exception) {
            null
        }
    }
    
    private fun createVectorPlaceholder(content: String, quality: PreviewQuality): BufferedImage {
        val size = quality.maxDimension
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()
        
        g2d.renderingHints = mapOf(
            RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
            RenderingHints.KEY_RENDERING to RenderingHints.VALUE_RENDER_QUALITY,
            RenderingHints.KEY_INTERPOLATION to RenderingHints.VALUE_INTERPOLATION_BICUBIC
        )
        
        // Glassmorphism background
        val bgColor = Color(0x1A, 0x1A, 0x2E)
        g2d.color = bgColor
        g2d.fillRect(0, 0, size, size)
        
        // Abstract glassmorphism shapes
        drawGlassmorphismShapes(g2d, size)
        
        // File type indicator
        g2d.color = Color(0xFF, 0xD7, 0x00) // Gold
        g2d.font = java.awt.Font("Segoe UI", java.awt.Font.BOLD, size / 20)
        val metrics = g2d.fontMetrics
        val text = "VECTOR"
        val textWidth = metrics.stringWidth(text)
        g2d.drawString(text, (size - textWidth) / 2, size / 2 + metrics.ascent / 2)
        
        g2d.dispose()
        return image
    }
    
    private fun createVideoPlaceholder(quality: PreviewQuality): BufferedImage {
        val size = quality.maxDimension
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()
        
        g2d.renderingHints = mapOf(
            RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
            RenderingHints.KEY_RENDERING to RenderingHints.VALUE_RENDER_QUALITY
        )
        
        // Dark background
        val bgColor = Color(0x0A, 0x0A, 0x1A)
        g2d.color = bgColor
        g2d.fillRect(0, 0, size, size)
        
        // Film strip pattern
        g2d.color = Color(0xFF, 0xD7, 0x00, 60)
        val frameHeight = size / 5
        for (i in 0..4) {
            val y = i * frameHeight + frameHeight / 4
            val frameRect = java.awt.geom.RoundRectangle2D.Double(
                size * 0.15, y.toDouble(), size * 0.7, frameHeight * 0.5, 8, 8
            )
            g2d.fill(frameRect)
            
            // Sprocket holes
            g2d.color = Color(0xFF, 0xD7, 0x00, 40)
            val holeSize = frameHeight / 8
            g2d.fillOval((size * 0.08).toInt(), (y + frameHeight / 8).toInt(), holeSize, holeSize)
            g2d.fillOval((size * 0.85).toInt(), (y + frameHeight / 8).toInt(), holeSize, holeSize)
        }
        
        // "VIDEO" label
        g2d.color = Color(0xFF, 0xD7, 0x00)
        g2d.font = java.awt.Font("Segoe UI", java.awt.Font.BOLD, size / 18)
        val metrics = g2d.fontMetrics
        val text = "VIDEO"
        val textWidth = metrics.stringWidth(text)
        g2d.drawString(text, (size - textWidth) / 2, size / 2 + metrics.ascent / 2)
        
        // Play button icon
        val playSize = size / 6
        val playX = (size - playSize) / 2
        val playY = size * 3 / 4
        g2d.color = Color(0xFF, 0xD7, 0x00, 100)
        val playTriangle = intArrayOf(
            playX, playY + playSize / 2,
            playX + playSize, playY + playSize / 4,
            playX + playSize, playY + playSize * 3 / 4
        )
        g2d.fillPolygon(playTriangle, playTriangle.size / 2)
        
        g2d.dispose()
        return image
    }
    
    private fun createAudioPlaceholder(quality: PreviewQuality): BufferedImage {
        val size = quality.maxDimension
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()
        
        g2d.renderingHints = mapOf(
            RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
            RenderingHints.KEY_RENDERING to RenderingHints.VALUE_RENDER_QUALITY
        )
        
        // Dark background
        val bgColor = Color(0x0A, 0x0A, 0x1A)
        g2d.color = bgColor
        g2d.fillRect(0, 0, size, size)
        
        // Waveform visualization
        val centerY = size / 2
        val amplitude = size / 3
        val barWidth = maxOf(2, size / 80)
        val spacing = barWidth + 2
        
        g2d.color = Color(0xFF, 0xD7, 0x00)
        for (i in 0 until size / spacing) {
            val x = i * spacing
            // Pseudo-random but deterministic waveform
            val seed = (i * 17 + 42) % 100
            val height = (amplitude * (0.3 + 0.7 * Math.sin(seed * 0.1) * Math.cos(seed * 0.05))).toInt()
            val barHeight = maxOf(2, height)
            
            g2d.fillRoundRect(x, centerY - barHeight / 2, barWidth, barHeight, 2, 2)
        }
        
        // Center line
        g2d.color = Color(0xFF, 0xD7, 0x00, 50)
        g2d.drawLine(0, centerY, size, centerY)
        
        // "AUDIO" label
        g2d.color = Color(0xFF, 0xD7, 0x00)
        g2d.font = java.awt.Font("Segoe UI", java.awt.Font.BOLD, size / 18)
        val metrics = g2d.fontMetrics
        val text = "AUDIO"
        val textWidth = metrics.stringWidth(text)
        g2d.drawString(text, (size - textWidth) / 2, size / 2 + metrics.ascent / 2)
        
        // Speaker icon
        val speakerSize = size / 8
        val speakerX = size / 2 - speakerSize
        val speakerY = size * 3 / 4
        g2d.color = Color(0xFF, 0xD7, 0x00, 100)
        // Speaker body
        g2d.fillRoundRect(speakerX, speakerY, speakerSize / 2, speakerSize, 4, 4)
        // Speaker cone
        val cone = intArrayOf(
            speakerX + speakerSize / 2, speakerY,
            speakerX + speakerSize, speakerY + speakerSize / 4,
            speakerX + speakerSize, speakerY + speakerSize * 3 / 4,
            speakerX + speakerSize / 2, speakerY + speakerSize
        )
        g2d.fillPolygon(cone, cone.size / 2)
        // Sound waves
        g2d.stroke = java.awt.BasicStroke(2f)
        for (wave in 1..2) {
            val arcX = speakerX + speakerSize / 2 + wave * 8
            g2d.drawArc(arcX, speakerY + speakerSize / 4, 16, speakerSize / 2, -45, 90)
        }
        
        g2d.dispose()
        return image
    }
    
    private fun createPlaceholderPreview(fileType: FileType, quality: PreviewQuality): BufferedImage {
        val size = quality.maxDimension
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()
        
        g2d.renderingHints = mapOf(
            RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON
        )
        
        val bgColor = Color(0x1A, 0x1A, 0x2E)
        g2d.color = bgColor
        g2d.fillRect(0, 0, size, size)
        
        drawGlassmorphismShapes(g2d, size)
        
        g2d.color = Color(0xFF, 0xD7, 0x00)
        g2d.font = java.awt.Font("Segoe UI", java.awt.Font.BOLD, size / 20)
        val metrics = g2d.fontMetrics
        val text = fileType.name
        val textWidth = metrics.stringWidth(text)
        g2d.drawString(text, (size - textWidth) / 2, size / 2 + metrics.ascent / 2)
        
        g2d.dispose()
        return image
    }
    
    private fun drawGlassmorphismShapes(g2d: Graphics2D, size: Int) {
        // Draw translucent geometric shapes with golden gradients
        val shapes = listOf(
            GlassShape(size * 0.15, size * 0.2, size * 0.4, size * 0.35, 30.0),
            GlassShape(size * 0.6, size * 0.15, size * 0.35, size * 0.4, -15.0),
            GlassShape(size * 0.25, size * 0.6, size * 0.5, size * 0.25, 45.0),
            GlassShape(size * 0.55, size * 0.55, size * 0.3, size * 0.45, -30.0)
        )
        
        for (shape in shapes) {
            val gradient = java.awt.GradientPaint(
                shape.x.toFloat(), shape.y.toFloat(),
                java.awt.Color(0xFF, 0xD7, 0x00, 80), // Gold with transparency
                (shape.x + shape.width).toFloat(), (shape.y + shape.height).toFloat(),
                java.awt.Color(0xFF, 0x8C, 0x00, 40), // Darker gold
                true
            )
            g2d.paint = gradient
            
            val rect = java.awt.geom.RoundRectangle2D.Double(
                shape.x, shape.y, shape.width, shape.height, 
                shape.width * 0.3, shape.height * 0.3
            )
            
            val transform = java.awt.geom.AffineTransform()
            transform.rotate(Math.toRadians(shape.rotation), shape.x + shape.width/2, shape.y + shape.height/2)
            g2d.fill(transform.createTransformedShape(rect))
        }
        
        // Add subtle glow effects
        g2d.color = java.awt.Color(0xFF, 0xD7, 0x00, 30)
        for (i in 1..3) {
            val inset = i * 5
            g2d.drawRoundRect(inset, inset, size - 2*inset, size - 2*inset, 20, 20)
        }
    }
    
    private fun calculateScaledDimensions(width: Int, height: Int, maxDim: Int): Pair<Int, Int> {
        val ratio = width.toDouble() / height
        return if (width > height) {
            val newWidth = minOf(width, maxDim)
            val newHeight = (newWidth / ratio).toInt()
            newWidth to newHeight
        } else {
            val newHeight = minOf(height, maxDim)
            val newWidth = (newHeight * ratio).toInt()
            newWidth to newHeight
        }
    }
    
    private data class GlassShape(
        val x: Double, val y: Double,
        val width: Double, val height: Double,
        val rotation: Double
    )
}