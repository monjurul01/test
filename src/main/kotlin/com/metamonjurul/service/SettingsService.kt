package com.metamonjurul.service

import com.metamonjurul.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class SettingsService {
    private val settingsFile = File(System.getProperty("user.home"), ".metamonjurul/settings.json")
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    
    suspend fun loadSettings(): AppSettings = withContext(Dispatchers.IO) {
        if (settingsFile.exists()) {
            try {
                val content = settingsFile.readText()
                json.decodeFromString<AppSettings>(content)
            } catch (e: Exception) {
                AppSettings()
            }
        } else {
            AppSettings()
        }
    }
    
    suspend fun saveSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        settingsFile.parentFile?.mkdirs()
        val content = json.encodeToString(settings)
        settingsFile.writeText(content)
    }
    
    suspend fun updateApiKey(apiKey: String) = withContext(Dispatchers.IO) {
        val settings = loadSettings()
        saveSettings(settings.copy(geminiApiKey = apiKey))
    }
    
    suspend fun updateOutputDirectory(directory: String) = withContext(Dispatchers.IO) {
        val settings = loadSettings()
        saveSettings(settings.copy(outputDirectory = directory))
    }
}