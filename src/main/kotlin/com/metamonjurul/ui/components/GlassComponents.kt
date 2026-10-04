package com.metamonjurul.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.drawRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.measure
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.px
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    elevation: GlassElevation = GlassElevation.Medium,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    
    Box(
        modifier = modifier
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        colors.surface.glassSurface,
                        colors.surface.glassSurfaceStrong
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(0f, 1f)
                )
            )
            .border(
                width = 1.dp,
                color = colors.primary.glassBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Glass highlight overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            colors.primary.glassHighlight,
                            Color.Transparent
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(0f, 0.3f)
                    )
                )
                .clip(RoundedCornerShape(16.dp))
        )
        
        content()
    }
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = if (enabled) {
                        listOf(colors.primary, colors.primaryContainer)
                    } else {
                        listOf(colors.surfaceVariant, colors.surfaceVariant)
                    },
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1f, 1f)
                )
            )
            .clip(RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        content()
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    isError: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    
    Column(modifier = modifier.fillMaxWidth()) {
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            colors.surface.glassSurface,
                            colors.surface.glassSurfaceStrong
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (isError) colors.error.glassBorder else colors.outline.glassBorder,
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
        ) {
            androidx.compose.material3.TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                singleLine = true,
                colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = colors.primary,
                    textColor = colors.onSurface,
                    placeholderColor = colors.onSurfaceVariant,
                    labelColor = colors.onSurfaceVariant
                ),
                label = if (label == null) { 
                    { Text(text = placeholder, color = colors.onSurfaceVariant) } 
                } else null
            )
        }
    }
}

@Composable
fun GlassProgressIndicator(
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier.size(200.dp, 8.dp),
    showLabel: Boolean = true,
    label: String = ""
) {
    val colors = MaterialTheme.colorScheme
    
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(colors.surface.glassSurface, colors.surface.glassSurfaceStrong)
                    )
                )
                .clip(RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(progress * 100)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(colors.primary, colors.secondary)
                        )
                    )
                    .clip(RoundedCornerShape(4.dp))
                    .animateContentSize()
            )
        }
        
        if (showLabel && label.isNotBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun GlassDropZone(
    modifier: Modifier = Modifier.fillMaxSize(),
    isDragOver: Boolean,
    onDrop: (List<java.io.File>) -> Unit,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    
    Box(
        modifier = modifier
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = if (isDragOver) {
                        listOf(
                            colors.primary.glassSurfaceStrong,
                            colors.secondary.glassSurfaceStrong
                        )
                    } else {
                        listOf(
                            colors.surface.glassSurface,
                            colors.surface.glassSurfaceStrong
                        )
                    }
                )
                .border(
                    width = 2.dp,
                    color = if (isDragOver) colors.primary else colors.outline.glassBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
                .padding(24.dp)
    ) {
        content()
    }
}

enum class GlassElevation {
    Low, Medium, High
}

@Composable
fun GlassTag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    onClose: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(color.glassSurfaceStrong, color.glassSurface)
                )
            )
            .border(1.dp, color.glassBorder, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
        onClose?.let {
            androidx.compose.material3.IconButton(onClick = it) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun GlassStatusBadge(
    status: com.metamonjurul.model.ProcessingStatus,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val (label, color) = when (status) {
        com.metamonjurul.model.ProcessingStatus.PENDING -> "Pending" to colors.onSurfaceVariant
        com.metamonjurul.model.ProcessingStatus.PREVIEW_GENERATING -> "Generating Preview" to colors.tertiary
        com.metamonjurul.model.ProcessingStatus.PREVIEW_READY -> "Preview Ready" to colors.primary
        com.metamonjurul.model.ProcessingStatus.METADATA_GENERATING -> "Generating Metadata" to colors.secondary
        com.metamonjurul.model.ProcessingStatus.METADATA_READY -> "Metadata Ready" to Color(0xFF00FF88)
        com.metamonjurul.model.ProcessingStatus.EXPORT_READY -> "Ready to Export" to Color(0xFF00FF88)
        com.metamonjurul.model.ProcessingStatus.ERROR -> "Error" to colors.error
    }
    
    Box(
        modifier = modifier
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(color.glassSurfaceStrong, color.glassSurface)
                )
            )
            .border(1.dp, color.glassBorder, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}