package com.ai.heymika.core.utils

import com.ai.heymika.core.data.source.local.entity.GeneratedContentEntity
import com.ai.heymika.core.data.source.local.entity.MaterialEntity
import com.ai.heymika.core.data.source.remote.response.GenerationResponse
import com.ai.heymika.core.data.source.remote.response.MaterialResponse
import com.ai.heymika.core.domain.model.GeneratedContent
import com.ai.heymika.core.domain.model.GenerationKind
import com.ai.heymika.core.domain.model.Material
import java.util.UUID

object DataMapper {
    /**
     * [favoriteIds] carries the ids the user has already starred, because a
     * remote refresh REPLACEs rows and would otherwise drop the favourite flag.
     */
    fun mapMaterialResponsesToEntities(
        input: List<MaterialResponse>,
        favoriteIds: Set<String> = emptySet(),
    ): List<MaterialEntity> = input.map { mapMaterialResponseToEntity(it, it.id in favoriteIds) }

    fun mapMaterialResponseToEntity(
        input: MaterialResponse,
        isFavorite: Boolean = false,
    ): MaterialEntity =
        MaterialEntity(
            materialId = input.id,
            title = input.title.orEmpty(),
            subject = input.subject.orEmpty(),
            description = input.description.orEmpty(),
            body = input.body.orEmpty(),
            coverUrl = input.coverUrl,
            durationMinutes = input.durationMinutes ?: 0,
            isFavorite = isFavorite,
        )

    fun mapMaterialEntitiesToDomain(input: List<MaterialEntity>): List<Material> =
        input.map { mapMaterialEntityToDomain(it) }

    fun mapMaterialEntityToDomain(input: MaterialEntity): Material =
        Material(
            id = input.materialId,
            title = input.title,
            subject = input.subject,
            description = input.description,
            body = input.body,
            coverUrl = input.coverUrl,
            durationMinutes = input.durationMinutes,
            isFavorite = input.isFavorite,
        )

    fun mapMaterialDomainToEntity(input: Material): MaterialEntity =
        MaterialEntity(
            materialId = input.id,
            title = input.title,
            subject = input.subject,
            description = input.description,
            body = input.body,
            coverUrl = input.coverUrl,
            durationMinutes = input.durationMinutes,
            isFavorite = input.isFavorite,
        )

    /**
     * The backend may hand back either a prose [GenerationResponse.body] or a
     * parsed [GenerationResponse.questions] list; a quiz is always stored as the
     * JSON array the aistudio feature reads back.
     */
    fun mapGenerationResponseToEntity(
        input: GenerationResponse,
        materialId: String,
        kind: GenerationKind,
    ): GeneratedContentEntity {
        val questions = input.questions
        val body =
            if (kind == GenerationKind.QUIZ && !questions.isNullOrEmpty()) {
                QuizParser.encode(QuizParser.fromResponses(questions))
            } else {
                input.body.orEmpty()
            }
        return GeneratedContentEntity(
            generatedContentId = input.id ?: UUID.randomUUID().toString(),
            materialId = input.materialId ?: materialId,
            kind = input.kind ?: kind.name,
            body = body,
            createdAt = input.createdAt ?: System.currentTimeMillis(),
        )
    }

    fun mapGeneratedContentEntitiesToDomain(input: List<GeneratedContentEntity>): List<GeneratedContent> =
        input.map { mapGeneratedContentEntityToDomain(it) }

    fun mapGeneratedContentEntityToDomain(input: GeneratedContentEntity): GeneratedContent =
        GeneratedContent(
            id = input.generatedContentId,
            materialId = input.materialId,
            kind = toGenerationKind(input.kind),
            body = input.body,
            createdAt = input.createdAt,
        )

    /** Falls back to [GenerationKind.SUMMARY] for a kind a newer backend invented. */
    private fun toGenerationKind(raw: String): GenerationKind =
        GenerationKind.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: GenerationKind.SUMMARY
}
