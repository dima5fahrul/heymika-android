package com.ai.heymika.core.data.source.remote

import android.util.Log
import com.ai.heymika.core.data.error.ErrorMapper
import com.ai.heymika.core.data.source.remote.network.ApiResponse
import com.ai.heymika.core.data.source.remote.network.ApiService
import com.ai.heymika.core.data.source.remote.request.GenerationRequest
import com.ai.heymika.core.data.source.remote.response.GenerationResponse
import com.ai.heymika.core.data.source.remote.response.MaterialResponse
import com.ai.heymika.core.domain.model.GenerationKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.CancellationException

/**
 * Wraps every [ApiService] call in an [ApiResponse]. A failure carries both the raw message (for
 * logs) and the typed [com.ai.heymika.core.domain.model.AppError] from [ErrorMapper] that the UI
 * branches on. Cancellation is rethrown, never reported as an error.
 */
class RemoteDataSource(
    private val apiService: ApiService,
) {
    fun getAllMaterials(): Flow<ApiResponse<List<MaterialResponse>>> =
        flow {
            try {
                val response = apiService.getMaterials()
                if (response.results.isNotEmpty()) {
                    emit(ApiResponse.Success(response.results))
                } else {
                    emit(ApiResponse.Empty)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                emit(ApiResponse.Error(errorMessage = e.toString(), error = ErrorMapper.map(e)))
                Log.e(TAG, "getAllMaterials failed", e)
            }
        }.flowOn(Dispatchers.IO)

    fun getMaterialById(id: String): Flow<ApiResponse<MaterialResponse>> =
        flow {
            try {
                emit(ApiResponse.Success(apiService.getMaterialById(id)))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                emit(ApiResponse.Error(errorMessage = e.toString(), error = ErrorMapper.map(e)))
                Log.e(TAG, "getMaterialById failed", e)
            }
        }.flowOn(Dispatchers.IO)

    fun generate(
        materialId: String,
        kind: GenerationKind,
    ): Flow<ApiResponse<GenerationResponse>> =
        flow {
            try {
                val response = apiService.generate(GenerationRequest(materialId, kind.name))
                if (response.body.isNullOrBlank() && response.questions.isNullOrEmpty()) {
                    emit(ApiResponse.Empty)
                } else {
                    emit(ApiResponse.Success(response))
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                emit(ApiResponse.Error(errorMessage = e.toString(), error = ErrorMapper.map(e)))
                Log.e(TAG, "generate failed", e)
            }
        }.flowOn(Dispatchers.IO)

    private companion object {
        const val TAG = "RemoteDataSource"
    }
}
