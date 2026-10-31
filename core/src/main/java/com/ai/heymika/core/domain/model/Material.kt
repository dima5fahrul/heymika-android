package com.ai.heymika.core.domain.model

/** A piece of study material the user reads. */
data class Material(
    val id: String,
    val title: String,
    val subject: String,
    val description: String,
    val body: String,
    val coverUrl: String?,
    val durationMinutes: Int,
    val isFavorite: Boolean,
)
