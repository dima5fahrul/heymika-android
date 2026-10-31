package com.ai.heymika.core.data.source.remote

import android.util.Log
import com.ai.heymika.core.data.source.remote.network.ApiResponse
import com.ai.heymika.core.data.source.remote.network.ApiService
import com.ai.heymika.core.data.source.remote.response.GenerationResponse
import com.ai.heymika.core.data.source.remote.response.ListMaterialResponse
import com.ai.heymika.core.data.source.remote.response.MaterialResponse
import com.ai.heymika.core.domain.model.AppError
import com.ai.heymika.core.domain.model.GenerationKind
import com.ai.heymika.core.testing.httpException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.UnknownHostException
import java.util.concurrent.CancellationException

class RemoteDataSourceTest {
    private val apiService = mockk<ApiService>()
    private val remoteDataSource = RemoteDataSource(apiService)

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `materials are a success when the list is not empty`() =
        runTest {
            val material = MaterialResponse("m-1", "Glikolisis", null, null, null, null, 6)
            coEvery { apiService.getMaterials(any(), any()) } returns ListMaterialResponse(results = listOf(material))

            assertEquals(listOf(ApiResponse.Success(listOf(material))), remoteDataSource.getAllMaterials().toList())
        }

    @Test
    fun `materials are empty when the list is empty`() =
        runTest {
            coEvery { apiService.getMaterials(any(), any()) } returns ListMaterialResponse()

            assertEquals(listOf(ApiResponse.Empty), remoteDataSource.getAllMaterials().toList())
        }

    @Test
    fun `network failure carries the raw message and the offline error`() =
        runTest {
            val failure = UnknownHostException("api.example.com")
            coEvery { apiService.getMaterials(any(), any()) } throws failure

            assertEquals(
                listOf(ApiResponse.Error(errorMessage = failure.toString(), error = AppError.Offline)),
                remoteDataSource.getAllMaterials().toList(),
            )
        }

    @Test
    fun `http failure on a detail carries the mapped error`() =
        runTest {
            coEvery { apiService.getMaterialById("m-404") } throws httpException(404)

            val error = remoteDataSource.getMaterialById("m-404").singleError()

            assertEquals(AppError.NotFound, error.error)
        }

    @Test
    fun `maintenance during generation carries the retry after estimate`() =
        runTest {
            coEvery { apiService.generate(any()) } throws httpException(503, retryAfter = "120")

            val error = remoteDataSource.generate("m-1", GenerationKind.QUIZ).singleError()

            assertEquals(AppError.Maintenance(2), error.error)
        }

    @Test
    fun `generation without body or questions is empty`() =
        runTest {
            coEvery { apiService.generate(any()) } returns GenerationResponse("g-1", "m-1", "SUMMARY", " ", 1L)

            assertEquals(listOf(ApiResponse.Empty), remoteDataSource.generate("m-1", GenerationKind.SUMMARY).toList())
        }

    @Test
    fun `cancellation is rethrown and never reported as an error`() =
        runTest {
            coEvery { apiService.getMaterials(any(), any()) } throws CancellationException("left the screen")
            val emitted = mutableListOf<ApiResponse<List<MaterialResponse>>>()

            val thrown =
                try {
                    remoteDataSource.getAllMaterials().collect { emitted += it }
                    null
                } catch (e: CancellationException) {
                    e
                }

            assertTrue(emitted.isEmpty())
            assertTrue(thrown is CancellationException)
        }

    /** Collects a flow that must emit exactly one [ApiResponse.Error]. */
    private suspend fun <T> Flow<ApiResponse<T>>.singleError(): ApiResponse.Error {
        val items = toList()
        assertEquals(1, items.size)
        return items.single() as ApiResponse.Error
    }
}
