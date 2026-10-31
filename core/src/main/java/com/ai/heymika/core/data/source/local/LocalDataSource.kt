package com.ai.heymika.core.data.source.local

import com.ai.heymika.core.data.source.local.entity.GeneratedContentEntity
import com.ai.heymika.core.data.source.local.entity.MaterialEntity
import com.ai.heymika.core.data.source.local.room.GeneratedContentDao
import com.ai.heymika.core.data.source.local.room.MaterialDao
import kotlinx.coroutines.flow.Flow

class LocalDataSource(
    private val materialDao: MaterialDao,
    private val generatedContentDao: GeneratedContentDao,
) {
    fun getAllMaterials(): Flow<List<MaterialEntity>> = materialDao.getAllMaterials()

    fun getMaterialById(id: String): Flow<MaterialEntity?> = materialDao.getMaterialById(id)

    fun getFavoriteMaterials(): Flow<List<MaterialEntity>> = materialDao.getFavoriteMaterials()

    suspend fun insertMaterials(materials: List<MaterialEntity>) = materialDao.insertMaterials(materials)

    suspend fun setFavorite(
        material: MaterialEntity,
        newState: Boolean,
    ) = materialDao.updateMaterial(material.copy(isFavorite = newState))

    fun getHistory(materialId: String): Flow<List<GeneratedContentEntity>> = generatedContentDao.getHistory(materialId)

    suspend fun insertGeneratedContent(content: GeneratedContentEntity) =
        generatedContentDao.insertGeneratedContent(content)
}
