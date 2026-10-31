package com.ai.heymika.core.data.repository

import com.ai.heymika.core.data.Resource
import com.ai.heymika.core.data.source.local.LocalDataSource
import com.ai.heymika.core.data.source.remote.RemoteDataSource
import com.ai.heymika.core.data.source.remote.network.ApiResponse
import com.ai.heymika.core.domain.model.GeneratedContent
import com.ai.heymika.core.domain.model.GenerationKind
import com.ai.heymika.core.domain.repository.IAiRepository
import com.ai.heymika.core.utils.DataMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Talks to our own backend, which proxies Anthropic. Every result is cached in
 * Room so a generated quiz, summary or story survives a process death and can be
 * re-read offline from [getHistory].
 */
class AiRepository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
) : IAiRepository {
    override fun generate(
        materialId: String,
        kind: GenerationKind,
    ): Flow<Resource<GeneratedContent>> =
        flow {
            emit(Resource.Loading())
            when (val apiResponse = remoteDataSource.generate(materialId, kind).first()) {
                is ApiResponse.Success -> {
                    val entity = DataMapper.mapGenerationResponseToEntity(apiResponse.data, materialId, kind)
                    localDataSource.insertGeneratedContent(entity)
                    emit(Resource.Success(DataMapper.mapGeneratedContentEntityToDomain(entity)))
                }

                is ApiResponse.Empty -> {
                    emit(Resource.Error("The assistant returned nothing for this material. Try again."))
                }

                is ApiResponse.Error -> {
                    emit(Resource.Error(apiResponse.errorMessage, error = apiResponse.error))
                }
            }
        }.flowOn(Dispatchers.IO)

    override fun getHistory(materialId: String): Flow<List<GeneratedContent>> =
        localDataSource.getHistory(materialId).map { DataMapper.mapGeneratedContentEntitiesToDomain(it) }
}
