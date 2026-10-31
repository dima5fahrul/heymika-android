package com.ai.heymika.core.domain.repository

import com.ai.heymika.core.data.Resource
import com.ai.heymika.core.domain.model.GeneratedContent
import com.ai.heymika.core.domain.model.GenerationKind
import kotlinx.coroutines.flow.Flow

interface IAiRepository {
    fun generate(
        materialId: String,
        kind: GenerationKind,
    ): Flow<Resource<GeneratedContent>>

    fun getHistory(materialId: String): Flow<List<GeneratedContent>>
}
