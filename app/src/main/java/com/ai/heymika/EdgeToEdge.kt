package com.ai.heymika

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

/**
 * Draws behind the system bars with dark icons on transparent bars, whatever the system theme:
 * the app is light-only, so the auto style (light icons in system dark mode) would hide them.
 */
fun ComponentActivity.enableHeyMikaEdgeToEdge() {
    val lightBars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)
}
