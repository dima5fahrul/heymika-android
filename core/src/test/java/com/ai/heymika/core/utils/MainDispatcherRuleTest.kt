package com.ai.heymika.core.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainDispatcherRuleTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `work launched on Main runs on the test dispatcher`() =
        runTest(mainDispatcherRule.testDispatcher) {
            var ranOnMain = false

            launch(Dispatchers.Main) { ranOnMain = true }

            assertTrue(ranOnMain)
        }
}
