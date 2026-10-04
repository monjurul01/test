package com.metamonjurul.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.awaitPointerEventScope
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metamonjurul.model.AppSettings
import com.metamonjurul.model.Marketplace
import com.metamonjurul.model.ProcessingStatus
import com.metamonjurul.model.StockAsset
import com.metamonjurul.service.CsvExportService
import com.metamonjurul.service.FileImportService
import com.metamonjurul.service.GeminiService
import com.metamonjurul.service.PreviewGenerationService
import com.metamonjurul.service.SettingsService
import com.metamonjurul.ui.components.GlassButton
import com.metamonjurul.ui.components.GlassCard
import com.metamonjurul.ui.components.GlassDropZone
import com.metamonjurul.ui.components.GlassProgressIndicator
import com.metamonjurul.ui.screens.AssetDetailScreen
import com.metamonjurul.ui.screens.AssetListScreen
import com.metamonjurul.ui.screens.EmptyState
import com.metamonjurul.ui.screens.ExportScreen
import com.metamonjurul.ui.screens.SettingsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

enum class Screen {
    MAIN, EXPORT, SETTINGS
}

@Composable
fun MainWindow(
    fileImportService: FileImportService = remember { FileImportService() },
    previewService: PreviewGenerationService = remember { PreviewGenerationService() },
    geminiService: GeminiService = remember { GeminiService("") },
    csvExportService: CsvExportService = remember { CsvExportService() },
    settingsService: SettingsService = remember { SettingsService() }
) {
    val context = LocalContext.current
    var assets by remember { mutableStateOf<List<StockAsset>>(emptyList()) }
    var selectedAsset by remember { mutableStateOf<StockAsset?>(null) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.MAIN) }
    var settings by remember { mutableStateOf<AppSettings>(AppSettings()) }
    var isDragOver by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }
    var outputDirInput by remember { mutableStateOf(settings.outputDirectory) }
    var isProcessing by remember { mutableStateOf(false) }
    var processingMessage by remember { mutableStateOf("") }
    
    // Load settings on start
    androidx.compose.runtime.LaunchedEffect(Unit) {
        settings = settingsService.loadSettings().await()
        apiKeyInput = settings.geminiApiKey
        outputDirInput = settings.outputDirectory
    }
    
    // Update Gemini service when API key changes
    val gemini = remember(apiKeyInput) {
        GeminiService(apiKeyInput)
    }
    
    // Handle file drops
    val dropZoneModifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            awaitPointerEventScope {
                // This is a simplified version - in real app use proper drag-drop handling
            }
        }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0F0F1A),
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E)
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1f, 1f)
                    )
                )
        )
        
        when (currentScreen) {
            Screen.MAIN -> MainScreen(
                assets = assets,
                selectedAsset = selectedAsset,
                isDragOver = isDragOver,
                isProcessing = isProcessing,
                processingMessage = processingMessage,
                onAssetClick = { asset ->
                    selectedAsset = asset
                },
                onAssetDelete = { asset ->
                    assets = assets - asset
                    if (selectedAsset?.id == asset.id) selectedAsset = null
                },
                onSelectionChange = { asset ->
                    selectedAsset = asset
                },
                onFilesDropped = { files ->
                    importFiles(files)
                },
                onGenerateMetadata = { asset ->
                    generateMetadata(asset)
                },
                onExportClick = {
                    currentScreen = Screen.EXPORT
                },
                onSettingsClick = {
                    currentScreen = Screen.SETTINGS
                },
                onDragOverChange = { over ->
                    isDragOver = over
                }
            )
            
            Screen.EXPORT -> ExportScreen(
                assets = assets,
                onExport = { marketplaces ->
                    exportToMarketplaces(marketplaces)
                },
                onBack = { currentScreen = Screen.MAIN }
            )
            
            Screen.SETTINGS -> SettingsScreen(
                apiKey = apiKeyInput,
                onApiKeyChange = { key ->
                    apiKeyInput = key
                    settingsService.updateApiKey(key).launch(Dispatchers.IO) {}
                },
                outputDirectory = outputDirInput,
                onOutputDirectoryChange = { dir ->
                    outputDirInput = dir
                    settingsService.updateOutputDirectory(dir).launch(Dispatchers.IO) {}
                    settings = settings.copy(outputDirectory = dir)
                },
                onBack = { currentScreen = Screen.MAIN }
            )
        }
    }
    
    fun importFiles(files: List<File>) {
        isProcessing = true
        processingMessage = "Importing ${files.size} file(s)..."
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newAssets = fileImportService.importFiles(files, settings).await()
                
                // Generate previews for new assets
                for (asset in newAssets) {
                    processingMessage = "Generating preview for ${asset.fileName}..."
                    val assetWithPreview = previewService.generatePreview(asset, settings).await()
                    assets = assets + assetWithPreview
                }
                
                processingMessage = "Import complete!"
                isProcessing = false
            } catch (e: Exception) {
                processingMessage = "Error: ${e.message}"
                isProcessing = false
            }
        }
    }
    
    fun generateMetadata(asset: StockAsset) {
        if (GeminiService.effectiveKey(apiKeyInput).isBlank()) {
            processingMessage = "Please set Gemini API key in Settings (or GEMINI_API_KEY env var)"
            return
        }
        
        val assetIndex = assets.indexOfFirst { it.id == asset.id }
        if (assetIndex == -1) return
        
        isProcessing = true
        processingMessage = "Analyzing with Gemini AI..."
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val analysis = gemini.analyzeImage(
                    asset.previewImagePath ?: asset.filePath,
                    "Stock marketplace asset: ${asset.fileName}",
                    asset.fileType
                ).await()
                
                val updatedAsset = assets[assetIndex].copy(
                    metadata = assets[assetIndex].metadata.copy(
                        title = analysis.title,
                        description = analysis.description,
                        keywords = analysis.keywords,
                        category = analysis.suggestedCategory
                    ),
                    status = ProcessingStatus.METADATA_READY
                )
                
                assets = assets.toMutableList().apply { this[assetIndex] = updatedAsset }.toList()
                if (selectedAsset?.id == asset.id) selectedAsset = updatedAsset
                
                processingMessage = "Metadata generated successfully!"
                isProcessing = false
            } catch (e: Exception) {
                processingMessage = "Error: ${e.message}"
                isProcessing = false
            }
        }
    }
    
    fun exportToMarketplaces(marketplaces: Set<Marketplace>) {
        isProcessing = true
        processingMessage = "Exporting to ${marketplaces.size} marketplace(s)..."
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val results = csvExportService.exportAllMarketplaces(
                    assets.filter { it.status == ProcessingStatus.METADATA_READY || it.status == ProcessingStatus.EXPORT_READY },
                    File(settings.outputDirectory),
                    settings
                ).await()
                
                // Update UI with results
                // This would need to be handled via state
                processingMessage = "Export complete! Check ${settings.outputDirectory}"
                isProcessing = false
            } catch (e: Exception) {
                processingMessage = "Export error: ${e.message}"
                isProcessing = false
            }
        }
    }
}

@Composable
fun MainScreen(
    assets: List<StockAsset>,
    selectedAsset: StockAsset?,
    isDragOver: Boolean,
    isProcessing: Boolean,
    processingMessage: String,
    onAssetClick: (StockAsset) -> Unit,
    onAssetDelete: (StockAsset) -> Unit,
    onSelectionChange: (StockAsset?) -> Unit,
    onFilesDropped: (List<File>) -> Unit,
    onGenerateMetadata: (StockAsset) -> Unit,
    onExportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onDragOverChange: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Top Bar
        TopBar(
            onExportClick = onExportClick,
            onSettingsClick = onSettingsClick,
            assetCount = assets.size,
            readyCount = assets.count { it.status == ProcessingStatus.METADATA_READY || it.status == ProcessingStatus.EXPORT_READY }
        )
        
        // Main Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Panel - Asset List
            Box(
                modifier = Modifier
                    .width(380.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Drop Zone Header
                        GlassDropZone(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            isDragOver = isDragOver,
                            onDrop = onFilesDropped
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.CloudUpload,
                                    contentDescription = "",
                                    tint = if (isDragOver) colors.primary else colors.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = if (isDragOver) "Drop files here" else "Drag & Drop Files",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isDragOver) colors.primary else colors.onSurfaceVariant
                                )
                                Text(
                                    text = "SVG, EPS, PNG, JPG, WEBP",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Asset List
                        if (assets.isNotEmpty()) {
                            com.metamonjurul.ui.screens.AssetListScreen(
                                assets = assets,
                                onAssetClick = onAssetClick,
                                onAssetDelete = onAssetDelete,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 16.dp),
                                selectedAsset = selectedAsset,
                                onSelectionChange = onSelectionChange
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                EmptyState()
                            }
                        }
                    }
                }
            }
            
            // Right Panel - Detail View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                if (selectedAsset != null) {
                    GlassCard(modifier = Modifier.fillMaxSize()) {
                        AssetDetailScreen(
                            asset = selectedAsset,
                            onMetadataUpdate = { updated ->
                                // Update handled in AssetDetailScreen
                            },
                            onGenerateMetadata = { onGenerateMetadata(selectedAsset!!) },
                            onExport = onExportClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    GlassCard(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.EditNote,
                                    contentDescription = "",
                                    tint = colors.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = "Select an Asset",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = colors.onSurfaceVariant
                                )
                                Text(
                                    text = "Choose a file from the list to edit metadata and generate AI-powered descriptions",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Processing indicator
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = processingMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(
    onExportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    assetCount: Int,
    readyCount: Int
) {
    val colors = MaterialTheme.colorScheme
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E).copy(alpha = 0.95f),
                        Color(0xFF16213E).copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = colors.outline.glassBorder,
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.AutoAwesome,
                        contentDescription = "",
                        tint = colors.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text("Meta Monjurul", style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                    Text("$assetCount assets • $readyCount ready", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            
            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassButton(
                    onClick = onExportClick,
                    enabled = readyCount > 0,
                    modifier = Modifier.width(160.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Download,
                            contentDescription = "",
                            tint = colors.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Export CSV", style = MaterialTheme.typography.labelMedium, color = colors.onPrimary)
                    }
                }
                
                GlassButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.width(48.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = colors.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}