package com.ai.heymika.core.data.source.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ai.heymika.core.data.source.local.entity.GeneratedContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedContentDao {
    @Query("SELECT * FROM generated_content WHERE materialId = :materialId ORDER BY createdAt DESC")
    fun getHistory(materialId: String): Flow<List<GeneratedContentEntity>>

    @Query("SELECT * FROM generated_content ORDER BY createdAt DESC")
    fun getAllGeneratedContent(): Flow<List<GeneratedContentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneratedContent(content: GeneratedContentEntity)

    @Query("DELETE FROM generated_content WHERE materialId = :materialId")
    suspend fun clearHistory(materialId: String)
}
