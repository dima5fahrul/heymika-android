package com.ai.heymika.core.data.source.remote.response

import com.google.gson.annotations.SerializedName

data class ListMaterialResponse(
    @field:SerializedName("page")
    val page: Int = 1,
    @field:SerializedName("results")
    val results: List<MaterialResponse> = emptyList(),
    @field:SerializedName("totalPages")
    val totalPages: Int = 1,
    @field:SerializedName("totalResults")
    val totalResults: Int = 0,
)
