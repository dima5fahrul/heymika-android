package com.ai.heymika.core.data.repository

import com.ai.heymika.core.data.NetworkBoundResource
import com.ai.heymika.core.data.Resource
import com.ai.heymika.core.data.source.local.LocalDataSource
import com.ai.heymika.core.data.source.remote.RemoteDataSource
import com.ai.heymika.core.data.source.remote.network.ApiResponse
import com.ai.heymika.core.data.source.remote.response.MaterialResponse
import com.ai.heymika.core.domain.model.Material
import com.ai.heymika.core.domain.repository.IMaterialRepository
import com.ai.heymika.core.utils.DataMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class MaterialRepository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
) : IMaterialRepository {
    override fun getAllMaterials(): Flow<Resource<List<Material>>> =
        object : NetworkBoundResource<List<Material>, List<MaterialResponse>>() {
            override fun loadFromDB(): Flow<List<Material>> =
                localDataSource.getAllMaterials().map { DataMapper.mapMaterialEntitiesToDomain(it) }

            override fun shouldFetch(data: List<Material>?): Boolean = data.isNullOrEmpty()

            override suspend fun createCall(): Flow<ApiResponse<List<MaterialResponse>>> =
                remoteDataSource.getAllMaterials()

            override suspend fun saveCallResult(data: List<MaterialResponse>) {
                // REPLACE would otherwise clear the favourite flag on refresh.
                val favoriteIds =
                    localDataSource
                        .getFavoriteMaterials()
                        .first()
                        .map { it.materialId }
                        .toSet()
                localDataSource.insertMaterials(DataMapper.mapMaterialResponsesToEntities(data, favoriteIds))
            }
        }.asFlow()

    override fun getMaterialById(id: String): Flow<Material?> =
        localDataSource
            .getMaterialById(id)
            .onStart { fetchMaterialIfMissing(id) }
            .map { entity -> entity?.let { DataMapper.mapMaterialEntityToDomain(it) } }
            .flowOn(Dispatchers.IO)

    override fun getFavoriteMaterials(): Flow<List<Material>> =
        localDataSource.getFavoriteMaterials().map { DataMapper.mapMaterialEntitiesToDomain(it) }

    override suspend fun setFavorite(
        material: Material,
        state: Boolean,
    ) {
        localDataSource.setFavorite(DataMapper.mapMaterialDomainToEntity(material), state)
    }

    /** The list endpoint may omit the full body, so a detail view tops the row up once. */
    private suspend fun fetchMaterialIfMissing(id: String) {
        if (localDataSource.getMaterialById(id).first() != null) return
        val apiResponse = remoteDataSource.getMaterialById(id).first()
        if (apiResponse is ApiResponse.Success) {
            localDataSource.insertMaterials(listOf(DataMapper.mapMaterialResponseToEntity(apiResponse.data)))
        }
    }
}
