package com.ai.heymika.core.data

import com.ai.heymika.core.data.source.remote.network.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Single source of truth helper: Room is what the UI observes, and the network
 * is only ever used to top that cache up (remote -> Room -> emit).
 */
abstract class NetworkBoundResource<ResultType, RequestType> {
    private val result: Flow<Resource<ResultType>> =
        flow {
            emit(Resource.Loading())
            val dbSource = loadFromDB().first()
            if (shouldFetch(dbSource)) {
                emit(Resource.Loading(dbSource))
                when (val apiResponse = createCall().first()) {
                    is ApiResponse.Success -> {
                        saveCallResult(apiResponse.data)
                        emitAll(loadFromDB().map { Resource.Success(it) })
                    }

                    is ApiResponse.Empty -> {
                        emitAll(loadFromDB().map { Resource.Success(it) })
                    }

                    is ApiResponse.Error -> {
                        onFetchFailed()
                        emit(Resource.Error(apiResponse.errorMessage, dbSource, apiResponse.error))
                    }
                }
            } else {
                emitAll(loadFromDB().map { Resource.Success(it) })
            }
        }.flowOn(Dispatchers.IO)

    protected open fun onFetchFailed() = Unit

    protected abstract fun loadFromDB(): Flow<ResultType>

    protected abstract fun shouldFetch(data: ResultType?): Boolean

    protected abstract suspend fun createCall(): Flow<ApiResponse<RequestType>>

    protected abstract suspend fun saveCallResult(data: RequestType)

    fun asFlow(): Flow<Resource<ResultType>> = result
}
