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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metamonjurul.model.Marketplace
import com.metamonjurul.model.StockAsset
import com.metamonjurul.ui.components.GlassCard
import com.metamonjurul.ui.components.GlassButton
import com.metamonjurul.ui.components.GlassProgressIndicator
import com.metamonjurul.ui.components.GlassStatusBadge
import com.metamonjurul.ui.components.GlassTextField

@Composable
fun ExportScreen(
    assets: List<StockAsset>,
    onExport: (Set<Marketplace>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    var selectedMarketplaces by remember { mutableStateOf(Marketplace.values().toSet()) }
    var exportProgress by remember { mutableStateOf(0f) }
    var exportResults by remember { mutableStateOf<Map<Marketplace, String>>(emptyMap()) }
    var isExporting by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text("Export to Marketplaces", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
            
            Box(modifier = Modifier.width(48.dp)) // Balance
        }
        
        // Marketplace selection
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select Marketplaces", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Marketplace.values().forEach { marketplace ->
                        MarketplaceCheckbox(
                            marketplace = marketplace,
                            isSelected = marketplace in selectedMarketplaces,
                            onClick = {
                                if (marketplace in selectedMarketplaces) {
                                    selectedMarketplaces = selectedMarketplaces - marketplace
                                } else {
                                    selectedMarketplaces = selectedMarketplaces + marketplace
                                }
                            }
                        )
                    }
                }
            }
        }
        
        // Asset summary
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Export Summary", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Assets:", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    Text("${assets.size}", style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ready for Export:", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    Text("${assets.count { it.status == com.metamonjurul.model.ProcessingStatus.METADATA_READY || it.status == com.metamonjurul.model.ProcessingStatus.EXPORT_READY }}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF00FF88))
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Marketplaces:", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    Text("${selectedMarketplaces.size}", style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
                }
            }
        }
        
        // Progress
        if (isExporting) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Exporting...", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                    GlassProgressIndicator(
                        progress = exportProgress,
                        label = "Exporting to ${exportResults.size} marketplace(s)..."
                    )
                }
            }
        }
        
        // Results
        if (exportResults.isNotEmpty() && !isExporting) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Export Complete", style = MaterialTheme.typography.titleMedium, color = Color(0xFF00FF88))
                    
                    exportResults.forEach { (marketplace, path) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(marketplace.name.replace("_", " "), style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
                            Text("✓ Saved", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF00FF88))
                        }
                    }
                }
            }
        }
        
        // Export button
        GlassButton(
            onClick = {
                if (!isExporting && selectedMarketplaces.isNotEmpty()) {
                    onExport(selectedMarketplaces)
                }
            },
            enabled = !isExporting && selectedMarketplaces.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(
                text = if (isExporting) "Exporting..." else "Export CSV Files",
                style = MaterialTheme.typography.labelLarge,
                color = colors.onPrimary
            )
        }
    }
}

@Composable
fun MarketplaceCheckbox(
    marketplace: Marketplace,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val displayName = marketplace.name.replace("_", " ")
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = if (isSelected) {
                        listOf(colors.primary.glassSurfaceStrong, colors.secondary.glassSurfaceStrong)
                    } else {
                        listOf(colors.surface.glassSurface, colors.surface.glassSurfaceStrong)
                    }
                )
            )
            .border(
                width = 2.dp,
                color = if (isSelected) colors.primary else colors.outline.glassBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .padding(16.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(displayName, style = MaterialTheme.typography.bodyMedium, color = if (isSelected) colors.primary else colors.onSurface)
            Icon(
                imageVector = if (isSelected) {
                    androidx.compose.material.icons.Icons.Default.CheckCircle
                } else {
                    androidx.compose.material.icons.Icons.Default.RadioButtonUnchecked
                },
                contentDescription = "",
                tint = if (isSelected) colors.primary else colors.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SettingsScreen(
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    outputDirectory: String,
    onOutputDirectoryChange: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    var apiKeyInput by remember { mutableStateOf(apiKey) }
    var outputDirInput by remember { mutableStateOf(outputDirectory) }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text("Settings", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
            
            Box(modifier = Modifier.width(48.dp))
        }
        
        // API Key
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Gemini API Key", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Text("Get your API key from https://aistudio.google.com", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                
                com.metamonjurul.ui.components.GlassTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it; onApiKeyChange(it) },
                    label = "API Key",
                    placeholder = "Enter your Gemini API key..."
                )
            }
        }
        
        // Output Directory
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Export Directory", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                
                com.metamonjurul.ui.components.GlassTextField(
                    value = outputDirInput,
                    onValueChange = { outputDirInput = it; onOutputDirectoryChange(it) },
                    label = "Directory Path",
                    placeholder = "Select export directory..."
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    GlassButton(
                        onClick = {
                            // TODO: Open directory picker
                        },
                        modifier = Modifier.width(150.dp)
                    ) {
                        Text("Browse...", style = MaterialTheme.typography.labelMedium, color = colors.onPrimary)
                    }
                }
            }
        }
        
        // About
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Meta Monjurul", style = MaterialTheme.typography.titleLarge, color = colors.primary)
                Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                Text("Stock Marketplace Metadata Automation", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Text("Powered by Gemini AI", style = MaterialTheme.typography.bodySmall, color = colors.tertiary)
            }
        }
    }
}