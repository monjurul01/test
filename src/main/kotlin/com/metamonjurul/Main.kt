package com.metamonjurul

import androidx.compose.desktop.WindowSizeClass
import androidx.compose.desktop.windowSizeClass
import androidx.compose.desktop.windowSizeClasses
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.singleWindowApplication
import com.metamonjurul.ui.MainWindow
import com.metamonjurul.ui.MetaMonjurulTheme

fun main() = singleWindowApplication(
    title = "Meta Monjurul",
    width = 1400,
    height = 900,
    minWidth = 1000,
    minHeight = 700,
    position = null,
    visible = true,
    resizable = true,
    state = null,
    icon = null
) {
    val windowSizeClass = windowSizeClass()
    
    MetaMonjurulTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainWindow()
        }
    }
}