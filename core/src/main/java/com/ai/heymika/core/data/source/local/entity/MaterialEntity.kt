package com.ai.heymika.core.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "material")
data class MaterialEntity(
    @PrimaryKey
    @ColumnInfo(name = "materialId")
    val materialId: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "subject")
    val subject: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "body")
    val body: String,
    @ColumnInfo(name = "coverUrl")
    val coverUrl: String?,
    @ColumnInfo(name = "durationMinutes")
    val durationMinutes: Int,
    @ColumnInfo(name = "isFavorite")
    val isFavorite: Boolean = false,
)
