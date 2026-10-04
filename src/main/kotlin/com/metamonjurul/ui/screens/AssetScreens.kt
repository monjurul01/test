package com.metamonjurul.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.drawRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metamonjurul.model.ProcessingStatus
import com.metamonjurul.model.StockAsset
import com.metamonjurul.ui.components.GlassCard
import com.metamonjurul.ui.components.GlassStatusBadge
import com.metamonjurul.ui.components.GlassTag
import org.jetbrains.skija.shaper

@Composable
fun AssetListScreen(
    assets: List<StockAsset>,
    onAssetClick: (StockAsset) -> Unit,
    onAssetDelete: (StockAsset) -> Unit,
    modifier: Modifier = Modifier,
    selectedAsset: StockAsset? = null,
    onSelectionChange: (StockAsset?) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    
    Box(modifier = modifier.fillMaxSize()) {
        if (assets.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(assets) { asset ->
                    AssetListItem(
                        asset = asset,
                        isSelected = selectedAsset?.id == asset.id,
                        onClick = { onAssetClick(asset); onSelectionChange(asset) },
                        onDelete = { onAssetDelete(asset) }
                    )
                }
            }
        }
    }
}

@Composable
fun AssetListItem(
    asset: StockAsset,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(vertical = 4.dp),
        elevation = if (isSelected) com.metamonjurul.ui.components.GlassElevation.High else com.metamonjurul.ui.components.GlassElevation.Medium
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preview thumbnail
            Box(
                modifier = Modifier
                    .size(100.dp, 100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(colors.surfaceVariant, colors.surface)
                        )
                    )
            ) {
                asset.previewImagePath?.let { path ->
                    androidx.compose.ui.res.painterResource("file://$path")?.let { painter ->
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = asset.fileName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            
            // Asset info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FileTypeIcon(fileType = asset.fileType, size = 20.dp, color = colors.primary)
                        Text(
                            text = asset.fileName,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    GlassStatusBadge(status = asset.status)
                }
                
                Text(
                    text = "${getFileTypeLabel(asset.fileType)} • ${formatFileSize(asset.filePath)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                
                if (asset.metadata.keywords.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        asset.metadata.keywords.take(3).forEach { keyword ->
                            GlassTag(text = keyword, color = colors.primary)
                        }
                        if (asset.metadata.keywords.size > 3) {
                            GlassTag(text = "+${asset.metadata.keywords.size - 3} more", color = colors.onSurfaceVariant)
                        }
                    }
                }
            }
            
            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = colors.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    val colors = MaterialTheme.colorScheme
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.CloudUpload,
                    contentDescription = "",
                    tint = colors.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(80.dp)
                )
            }
            
            Text(
                text = "No Assets Imported",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface
            )
            
            Text(
                text = "Drag and drop SVG, EPS, or image files to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.TextAlign.Center
            )
        }
    }
}

@Composable
fun AssetDetailScreen(
    asset: StockAsset,
    onMetadataUpdate: (StockAsset) -> Unit,
    onGenerateMetadata: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Preview section
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Preview", style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(colors.surfaceVariant, colors.surface)
                            )
                        )
                ) {
                    asset.previewImagePath?.let { path ->
                        androidx.compose.ui.res.painterResource("file://$path")?.let { painter ->
                            androidx.compose.foundation.Image(
                                painter = painter,
                                contentDescription = asset.fileName,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                
                Text(
                    text = asset.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.TextAlign.Center
                )
            }
        }
        
        // Metadata fields
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            MetadataEditor(
                asset = asset,
                onUpdate = onMetadataUpdate
            )
        }
        
        // Keywords
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            KeywordsEditor(
                keywords = asset.metadata.keywords,
                onUpdate = { newKeywords ->
                    onMetadataUpdate(asset.copy(metadata = asset.metadata.copy(keywords = newKeywords)))
                }
            )
        }
        
        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            com.metamonjurul.ui.components.GlassButton(
                onClick = onGenerateMetadata,
                modifier = Modifier.weight(1f)
            ) {
                Text("Generate with AI", style = MaterialTheme.typography.labelLarge, color = colors.onPrimary)
            }
            
            com.metamonjurul.ui.components.GlassButton(
                onClick = onExport,
                modifier = Modifier.weight(1f)
            ) {
                Text("Export CSV", style = MaterialTheme.typography.labelLarge, color = colors.onPrimary)
            }
        }
    }
}

@Composable
fun MetadataEditor(
    asset: StockAsset,
    onUpdate: (StockAsset) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var title by remember { mutableStateOf(asset.metadata.title) }
    var description by remember { mutableStateOf(asset.metadata.description) }
    var category by remember { mutableStateOf(asset.metadata.category) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Metadata", style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
        
        com.metamonjurul.ui.components.GlassTextField(
            value = title,
            onValueChange = { title = it; onUpdate(asset.copy(metadata = asset.metadata.copy(title = it))) },
            label = "Title",
            placeholder = "Enter a descriptive title..."
        )
        
        com.metamonjurul.ui.components.GlassTextField(
            value = description,
            onValueChange = { description = it; onUpdate(asset.copy(metadata = asset.metadata.copy(description = it))) },
            label = "Description",
            placeholder = "Enter a detailed description..."
        )
        
        com.metamonjurul.ui.components.GlassTextField(
            value = category,
            onValueChange = { category = it; onUpdate(asset.copy(metadata = asset.metadata.copy(category = it))) },
            label = "Category",
            placeholder = "e.g., Backgrounds, Abstract, Technology..."
        )
    }
}

@Composable
fun KeywordsEditor(
    keywords: List<String>,
    onUpdate: (List<String>) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var keywordInput by remember { mutableStateOf("") }
    var localKeywords by remember { mutableStateOf(keywords.toMutableList()) }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            com.metamonjurul.ui.components.GlassTextField(
                value = keywordInput,
                onValueChange = { keywordInput = it },
                modifier = Modifier.weight(1f),
                placeholder = "Add keyword..."
            )
            com.metamonjurul.ui.components.GlassButton(
                onClick = {
                    if (keywordInput.isNotBlank() && keywordInput !in localKeywords) {
                        localKeywords = localKeywords + keywordInput
                        onUpdate(localKeywords)
                        keywordInput = ""
                    }
                },
                modifier = Modifier.width(100.dp)
            ) {
                Text("Add", style = MaterialTheme.typography.labelMedium, color = colors.onPrimary)
            }
        }
        
        // Keywords list
        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            localKeywords.forEach { keyword ->
                com.metamonjurul.ui.components.GlassTag(
                    text = keyword,
                    color = colors.primary,
                    onClose = {
                        localKeywords = localKeywords - keyword
                        onUpdate(localKeywords)
                    }
                )
            }
        }
    }
}

private fun formatFileSize(filePath: String): String {
    val file = java.io.File(filePath)
    val bytes = file.length()
    return when {
        bytes < 1024 -> "${bytes} B"
        bytes < 1024 * 1024 -> "${(bytes / 1024).toString()} KB"
        else -> String.format("%.1f MB", bytes / (1024 * 1024).toDouble())
    }
}

@Composable
fun FileTypeIcon(
    fileType: com.metamonjurul.model.FileType,
    size: androidx.compose.ui.unit.Dp = 24.dp,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    val icon = when {
        fileType.isVector() -> androidx.compose.material.icons.Icons.Default.VectorSquare
        fileType.isImage() -> androidx.compose.material.icons.Icons.Default.Image
        fileType.isVideo() -> androidx.compose.material.icons.Icons.Default.Videocam
        fileType.isAudio() -> androidx.compose.material.icons.Icons.Default.Audiotrack
        else -> androidx.compose.material.icons.Icons.Default.InsertDriveFile
    }
    
    Icon(
        imageVector = icon,
        contentDescription = fileType.name,
        tint = color,
        modifier = Modifier.size(size)
    )
}

fun getFileTypeLabel(fileType: com.metamonjurul.model.FileType): String {
    return when {
        fileType.isVector() -> "Vector"
        fileType.isImage() -> "Image"
        fileType.isVideo() -> "Video"
        fileType.isAudio() -> "Audio"
        else -> fileType.name
    }
}