package com.ai.heymika.core.data.source.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ai.heymika.core.data.source.local.entity.GeneratedContentEntity
import com.ai.heymika.core.data.source.local.entity.MaterialEntity

@Database(
    entities = [MaterialEntity::class, GeneratedContentEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AiLearnDatabase : RoomDatabase() {
    abstract fun materialDao(): MaterialDao

    abstract fun generatedContentDao(): GeneratedContentDao
}
