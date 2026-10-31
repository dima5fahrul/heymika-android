package com.ai.heymika.core.data.source.remote.response

import com.google.gson.annotations.SerializedName

data class MaterialResponse(
    @field:SerializedName("id")
    val id: String,
    @field:SerializedName("title")
    val title: String?,
    @field:SerializedName("subject")
    val subject: String?,
    @field:SerializedName("description")
    val description: String?,
    @field:SerializedName("body")
    val body: String?,
    @field:SerializedName("coverUrl")
    val coverUrl: String?,
    @field:SerializedName("durationMinutes")
    val durationMinutes: Int?,
)
