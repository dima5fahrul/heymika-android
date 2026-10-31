package com.ai.heymika.core.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cached AI output. [kind] is persisted as the name of a
 * [com.ai.heymika.core.domain.model.GenerationKind] so the table needs no type
 * converter and tolerates unknown values from a newer backend.
 */
@Entity(
    tableName = "generated_content",
    indices = [Index(value = ["materialId"])],
)
data class GeneratedContentEntity(
    @PrimaryKey
    @ColumnInfo(name = "generatedContentId")
    val generatedContentId: String,
    @ColumnInfo(name = "materialId")
    val materialId: String,
    @ColumnInfo(name = "kind")
    val kind: String,
    @ColumnInfo(name = "body")
    val body: String,
    @ColumnInfo(name = "createdAt")
    val createdAt: Long,
)
