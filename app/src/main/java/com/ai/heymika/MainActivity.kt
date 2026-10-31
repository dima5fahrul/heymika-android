package com.ai.heymika

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.google.android.play.core.splitcompat.SplitCompat

/**
 * The only Activity in the app. Everything else is a Compose destination, including the
 * screens contributed by the dynamic feature modules. v1 is light-only, so the stored darkMode
 * preference is not read here.
 */
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        // Required so an Activity/Composable from a just-installed split can resolve its
        // own resources and classes without restarting the process.
        SplitCompat.installActivity(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableHeyMikaEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            HeyMikaTheme {
                HeyMikaRoot()
            }
        }
    }
}
