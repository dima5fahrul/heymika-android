package com.ai.heymika.core.data.source.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ai.heymika.core.data.source.local.entity.MaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Query("SELECT * FROM material ORDER BY title ASC")
    fun getAllMaterials(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM material WHERE materialId = :id LIMIT 1")
    fun getMaterialById(id: String): Flow<MaterialEntity?>

    @Query("SELECT * FROM material WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteMaterials(): Flow<List<MaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<MaterialEntity>)

    @Update
    suspend fun updateMaterial(material: MaterialEntity)
}
