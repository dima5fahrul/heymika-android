package com.ai.heymika.core.data.source.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Body of a generation call. Only the material id and the requested kind are
 * sent; the backend owns the prompt and the model credentials.
 */
data class GenerationRequest(
    @field:SerializedName("materialId")
    val materialId: String,
    @field:SerializedName("kind")
    val kind: String,
)
