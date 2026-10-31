package com.ai.heymika.core.data.source.remote.response

import com.google.gson.annotations.SerializedName

/**
 * One generation result. [body] is the text the model produced; for a quiz it is
 * a JSON array of questions, which [questions] mirrors when the backend has
 * already parsed it.
 */
data class GenerationResponse(
    @field:SerializedName("id")
    val id: String?,
    @field:SerializedName("materialId")
    val materialId: String?,
    @field:SerializedName("kind")
    val kind: String?,
    @field:SerializedName("body")
    val body: String?,
    @field:SerializedName("createdAt")
    val createdAt: Long?,
    @field:SerializedName("questions")
    val questions: List<QuizQuestionResponse>? = null,
)
