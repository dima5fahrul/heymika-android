package com.ai.heymika.navigation

import androidx.navigation.NavHostController
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureLauncherTest {
    private val navController = mockk<NavHostController>(relaxed = true)
    private val installer =
        mockk<DynamicFeatureInstaller> {
            every { install(any()) } just runs
        }
    private val launcher = FeatureLauncher(navController, installer)

    @Test
    fun `installed module navigates right away`() {
        every { installer.isInstalled(DynamicFeature.SETTING) } returns true

        launcher.open(DynamicFeature.SETTING, Route.SETTING)

        verify { navController.navigate(Route.SETTING) }
        verify(exactly = 0) { installer.install(any()) }
        assertNull(launcher.pending)
    }

    @Test
    fun `missing module starts an install and waits`() {
        every { installer.isInstalled(DynamicFeature.FAVORITE) } returns false

        launcher.open(DynamicFeature.FAVORITE, Route.FAVORITE)

        verify { installer.install(DynamicFeature.FAVORITE) }
        verify(exactly = 0) { navController.navigate(any<String>()) }
        assertEquals(PendingFeature(DynamicFeature.FAVORITE, Route.FAVORITE), launcher.pending)
    }

    @Test
    fun `pending request completes once the module is installed`() {
        every { installer.isInstalled(DynamicFeature.FAVORITE) } returns false
        launcher.open(DynamicFeature.FAVORITE, Route.FAVORITE)
        assertFalse(launcher.completePending())

        every { installer.isInstalled(DynamicFeature.FAVORITE) } returns true

        assertTrue(launcher.completePending())
        verify { navController.navigate(Route.FAVORITE) }
        assertNull(launcher.pending)
    }

    @Test
    fun `pending request survives a graph that does not know the route yet`() {
        every { installer.isInstalled(DynamicFeature.AI_STUDIO) } returns false
        launcher.open(DynamicFeature.AI_STUDIO, "aistudio/42")
        every { installer.isInstalled(DynamicFeature.AI_STUDIO) } returns true
        every { navController.navigate("aistudio/42") } throws IllegalArgumentException("no destination")

        assertFalse(launcher.completePending())
        assertEquals(PendingFeature(DynamicFeature.AI_STUDIO, "aistudio/42"), launcher.pending)
    }

    @Test
    fun `dismiss drops the pending request`() {
        every { installer.isInstalled(DynamicFeature.SETTING) } returns false
        launcher.open(DynamicFeature.SETTING, Route.SETTING)

        launcher.dismiss()

        assertNull(launcher.pending)
        assertFalse(launcher.completePending())
    }
}
