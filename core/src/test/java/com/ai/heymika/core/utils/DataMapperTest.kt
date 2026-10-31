package com.ai.heymika.core.utils

import com.ai.heymika.core.data.source.local.entity.GeneratedContentEntity
import com.ai.heymika.core.data.source.local.entity.MaterialEntity
import com.ai.heymika.core.data.source.remote.response.GenerationResponse
import com.ai.heymika.core.data.source.remote.response.MaterialResponse
import com.ai.heymika.core.data.source.remote.response.QuizQuestionResponse
import com.ai.heymika.core.domain.model.GeneratedContent
import com.ai.heymika.core.domain.model.GenerationKind
import com.ai.heymika.core.domain.model.Material
import com.ai.heymika.core.domain.model.QuizQuestion
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class DataMapperTest {
    // region material

    @Test
    fun `material response with missing fields maps to an entity with defaults`() {
        val response = MaterialResponse("m-1", null, null, null, null, null, null)

        assertEquals(
            MaterialEntity(
                materialId = "m-1",
                title = "",
                subject = "",
                description = "",
                body = "",
                coverUrl = null,
                durationMinutes = 0,
                isFavorite = false,
            ),
            DataMapper.mapMaterialResponseToEntity(response),
        )
    }

    @Test
    fun `material response keeps every field and the favourite flag`() {
        assertEquals(entity(isFavorite = true), DataMapper.mapMaterialResponseToEntity(response(), isFavorite = true))
    }

    @Test
    fun `response list keeps the favourite flag of starred ids only`() {
        val responses = listOf(response(id = "a"), response(id = "b"), response(id = "c"))

        val entities = DataMapper.mapMaterialResponsesToEntities(responses, favoriteIds = setOf("b", "zzz"))

        assertEquals(listOf("a", "b", "c"), entities.map { it.materialId })
        assertEquals(listOf(false, true, false), entities.map { it.isFavorite })
    }

    @Test
    fun `response list without favourites marks nothing`() {
        val entities = DataMapper.mapMaterialResponsesToEntities(listOf(response(id = "a"), response(id = "b")))

        assertFalse(entities.any { it.isFavorite })
    }

    @Test
    fun `material entity maps to the domain model field by field`() {
        assertEquals(material(isFavorite = true), DataMapper.mapMaterialEntityToDomain(entity(isFavorite = true)))
    }

    @Test
    fun `material entity round-trips through the domain model`() {
        val entity = entity(isFavorite = true)

        assertEquals(entity, DataMapper.mapMaterialDomainToEntity(DataMapper.mapMaterialEntityToDomain(entity)))
    }

    @Test
    fun `material domain round-trips through the entity`() {
        val material = material(coverUrl = null)

        assertEquals(material, DataMapper.mapMaterialEntityToDomain(DataMapper.mapMaterialDomainToEntity(material)))
    }

    @Test
    fun `material entity list maps in order`() {
        val entities = listOf(entity(id = "x"), entity(id = "y"))

        assertEquals(listOf("x", "y"), DataMapper.mapMaterialEntitiesToDomain(entities).map { it.id })
    }

    // endregion

    // region generation

    @Test
    fun `quiz with structured questions stores the cleaned question list as json`() {
        val questions =
            listOf(
                QuizQuestionResponse("q-1", "Hasil netto glikolisis?", listOf("2 ATP", "4 ATP"), 0, "Investasi 2."),
                QuizQuestionResponse("q-2", " ", listOf("a"), 0, null),
                QuizQuestionResponse("q-3", "Tanpa opsi", emptyList(), 0, null),
                QuizQuestionResponse(null, "Indeks di luar rentang", listOf("x", "y"), 5, null),
            )

        val entity =
            DataMapper.mapGenerationResponseToEntity(
                generation(questions = questions),
                "m-1",
                GenerationKind.QUIZ,
            )
        val stored = Gson().fromJson(entity.body, Array<QuizQuestion>::class.java).toList()

        assertEquals(2, stored.size)
        assertEquals(
            QuizQuestion("q-1", "Hasil netto glikolisis?", listOf("2 ATP", "4 ATP"), 0, "Investasi 2."),
            stored[0],
        )
        assertEquals("Indeks di luar rentang", stored[1].prompt)
        assertEquals(0, stored[1].answerIndex)
        assertEquals("", stored[1].explanation)
        assertTrue(stored[1].id.isNotBlank())
    }

    @Test
    fun `quiz without structured questions keeps the raw body`() {
        val entity =
            DataMapper.mapGenerationResponseToEntity(
                generation(body = "[{\"prompt\":\"?\"}]"),
                "m-1",
                GenerationKind.QUIZ,
            )

        assertEquals("[{\"prompt\":\"?\"}]", entity.body)
    }

    @Test
    fun `non quiz kinds keep the body even when questions are present`() {
        val questions = listOf(QuizQuestionResponse("q-1", "?", listOf("a"), 0, null))

        val entity =
            DataMapper.mapGenerationResponseToEntity(
                generation(body = "Ringkasan", questions = questions),
                "m-1",
                GenerationKind.SUMMARY,
            )

        assertEquals("Ringkasan", entity.body)
    }

    @Test
    fun `null body becomes empty`() {
        val entity = DataMapper.mapGenerationResponseToEntity(generation(body = null), "m-1", GenerationKind.SUMMARY)

        assertEquals("", entity.body)
    }

    @Test
    fun `missing id, material, kind and timestamp are filled from the call`() {
        val before = System.currentTimeMillis()

        val entity =
            DataMapper.mapGenerationResponseToEntity(
                GenerationResponse(id = null, materialId = null, kind = null, body = "Isi", createdAt = null),
                "m-9",
                GenerationKind.SUMMARY,
            )

        val after = System.currentTimeMillis()
        UUID.fromString(entity.generatedContentId)
        assertEquals("m-9", entity.materialId)
        assertEquals(GenerationKind.SUMMARY.name, entity.kind)
        assertTrue(entity.createdAt in before..after)
    }

    @Test
    fun `response values win over the call arguments`() {
        val entity =
            DataMapper.mapGenerationResponseToEntity(
                GenerationResponse(id = "g-1", materialId = "m-2", kind = "QUIZ", body = "Isi", createdAt = 42L),
                "m-1",
                GenerationKind.SUMMARY,
            )

        assertEquals(GeneratedContentEntity("g-1", "m-2", "QUIZ", "Isi", 42L), entity)
    }

    @Test
    fun `every known kind round-trips from response to domain`() {
        GenerationKind.entries.forEach { kind ->
            val response =
                GenerationResponse(id = "g-$kind", materialId = "m-1", kind = null, body = "Isi", createdAt = 7L)

            val domain =
                DataMapper.mapGeneratedContentEntityToDomain(
                    DataMapper.mapGenerationResponseToEntity(response, "m-1", kind),
                )

            assertEquals(GeneratedContent("g-$kind", "m-1", kind, "Isi", 7L), domain)
        }
    }

    @Test
    fun `kind matching ignores case`() {
        val domain =
            DataMapper.mapGeneratedContentEntityToDomain(
                GeneratedContentEntity("g-1", "m-1", "quiz", "[]", 1L),
            )

        assertEquals(GenerationKind.QUIZ, domain.kind)
    }

    @Test
    fun `unknown kind from a newer backend falls back to summary`() {
        val domain =
            DataMapper.mapGeneratedContentEntityToDomain(
                GeneratedContentEntity("g-1", "m-1", "MINDMAP", "Isi", 1L),
            )

        assertEquals(GenerationKind.SUMMARY, domain.kind)
    }

    @Test
    fun `generated content list maps in order`() {
        val entities =
            listOf(
                GeneratedContentEntity("g-1", "m-1", "SUMMARY", "a", 1L),
                GeneratedContentEntity("g-2", "m-1", "QUIZ", "[]", 2L),
            )

        assertEquals(listOf("g-1", "g-2"), DataMapper.mapGeneratedContentEntitiesToDomain(entities).map { it.id })
    }

    // endregion

    private fun response(id: String = "m-1"): MaterialResponse =
        MaterialResponse(
            id = id,
            title = "Glikolisis",
            subject = "Biologi",
            description = "Bab 4",
            body = "Isi materi",
            coverUrl = "https://cdn.example.com/m.png",
            durationMinutes = 6,
        )

    private fun entity(
        id: String = "m-1",
        isFavorite: Boolean = false,
    ): MaterialEntity =
        MaterialEntity(
            materialId = id,
            title = "Glikolisis",
            subject = "Biologi",
            description = "Bab 4",
            body = "Isi materi",
            coverUrl = "https://cdn.example.com/m.png",
            durationMinutes = 6,
            isFavorite = isFavorite,
        )

    private fun material(
        isFavorite: Boolean = false,
        coverUrl: String? = "https://cdn.example.com/m.png",
    ): Material =
        Material(
            id = "m-1",
            title = "Glikolisis",
            subject = "Biologi",
            description = "Bab 4",
            body = "Isi materi",
            coverUrl = coverUrl,
            durationMinutes = 6,
            isFavorite = isFavorite,
        )

    private fun generation(
        body: String? = null,
        questions: List<QuizQuestionResponse>? = null,
    ): GenerationResponse =
        GenerationResponse(
            id = "g-1",
            materialId = "m-1",
            kind = null,
            body = body,
            createdAt = 1L,
            questions = questions,
        )
}
