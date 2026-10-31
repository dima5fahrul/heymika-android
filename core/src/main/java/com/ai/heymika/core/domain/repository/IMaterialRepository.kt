package com.ai.heymika.core.domain.repository

import com.ai.heymika.core.data.Resource
import com.ai.heymika.core.domain.model.Material
import kotlinx.coroutines.flow.Flow

interface IMaterialRepository {
    fun getAllMaterials(): Flow<Resource<List<Material>>>

    fun getMaterialById(id: String): Flow<Material?>

    fun getFavoriteMaterials(): Flow<List<Material>>

    suspend fun setFavorite(
        material: Material,
        state: Boolean,
    )
}
