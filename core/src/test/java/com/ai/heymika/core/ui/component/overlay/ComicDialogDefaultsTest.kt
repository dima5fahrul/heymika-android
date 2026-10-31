package com.ai.heymika.core.ui.component.overlay

import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Test

class ComicDialogDefaultsTest {
    @Test
    fun `properties are untouched while not loading`() {
        val properties = ComicDialogDefaults.properties
        assertSame(properties, ComicDialogDefaults.lockedWhileLoading(properties, loading = false))
    }

    @Test
    fun `loading blocks back press and outside taps but keeps the window settings`() {
        val properties =
            DialogProperties(
                securePolicy = SecureFlagPolicy.SecureOn,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            )
        val locked = ComicDialogDefaults.lockedWhileLoading(properties, loading = true)
        assertFalse(locked.dismissOnBackPress)
        assertFalse(locked.dismissOnClickOutside)
        assertEquals(SecureFlagPolicy.SecureOn, locked.securePolicy)
        assertFalse(locked.usePlatformDefaultWidth)
        assertFalse(locked.decorFitsSystemWindows)
    }
}
