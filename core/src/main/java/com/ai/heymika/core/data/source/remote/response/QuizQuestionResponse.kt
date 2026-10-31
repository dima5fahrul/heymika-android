package com.ai.heymika.core.data.source.remote.response

import com.google.gson.annotations.SerializedName

data class QuizQuestionResponse(
    @field:SerializedName("id")
    val id: String?,
    @field:SerializedName("prompt")
    val prompt: String?,
    @field:SerializedName("options")
    val options: List<String>?,
    @field:SerializedName("answerIndex")
    val answerIndex: Int?,
    @field:SerializedName("explanation")
    val explanation: String?,
)
