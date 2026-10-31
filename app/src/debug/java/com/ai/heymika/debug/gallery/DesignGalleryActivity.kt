package com.ai.heymika.debug.gallery

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.ai.heymika.enableHeyMikaEdgeToEdge

/**
 * Debug-only design gallery ("HeyMika DS" launcher entry). Every page can be opened directly, so
 * screenshots are deterministic:
 *
 * `adb shell am start -n com.ai.heymika/com.ai.heymika.debug.gallery.DesignGalleryActivity --es section buttons`
 *
 * Extras: `section` (a [GallerySection.id]), `open` (an overlay or picker to show on arrival, e.g.
 * `dialog`, `sheet`, `snackbar`, `time`, `date`), `state` (the full-screen state on the `states`
 * page, e.g. `offline`) and `offline` (boolean, forces the OfflineBanner).
 */
class DesignGalleryActivity : ComponentActivity() {
    private var request by mutableStateOf(GalleryRequest())

    override fun onCreate(savedInstanceState: Bundle?) {
        enableHeyMikaEdgeToEdge()
        super.onCreate(savedInstanceState)
        request = GalleryRequest.from(intent)
        setContent {
            HeyMikaTheme {
                DesignGalleryApp(request = request, onExit = ::finish)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        request = GalleryRequest.from(intent)
    }
}

/** What the launching intent asked the gallery to show. */
data class GalleryRequest(
    val section: String? = null,
    val open: String? = null,
    val state: String? = null,
    val offline: Boolean = false,
    /** Makes two identical intents distinct, so each one resets the gallery. */
    val nonce: Long = 0L,
) {
    companion object {
        fun from(intent: Intent?): GalleryRequest =
            GalleryRequest(
                section = intent?.getStringExtra("section"),
                open = intent?.getStringExtra("open"),
                state = intent?.getStringExtra("state"),
                offline = intent?.getBooleanExtra("offline", false) ?: false,
                nonce = System.nanoTime(),
            )
    }
}
