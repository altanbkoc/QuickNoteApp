package com.altankoc.quicknote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.altankoc.quicknote.ui.navigation.QuickNoteNavigation
import com.altankoc.quicknote.ui.screens.splash.SplashScreen
import com.altankoc.quicknote.ui.theme.QuickNoteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showSplash by remember { mutableStateOf(true) }

            QuickNoteTheme(dynamicColor = false) {
                if (showSplash) {
                    SplashScreen {
                        showSplash = false
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        QuickNoteNavigation()
                    }
                }
            }
        }
    }
}