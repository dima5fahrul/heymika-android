package com.ai.heymika.core.data.source.remote.network

import com.ai.heymika.core.data.source.remote.request.GenerationRequest
import com.ai.heymika.core.data.source.remote.response.GenerationResponse
import com.ai.heymika.core.data.source.remote.response.ListMaterialResponse
import com.ai.heymika.core.data.source.remote.response.MaterialResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Both halves of this service talk to OUR OWN backend. The `ai/generate`
 * endpoint is a thin proxy in front of Anthropic: the backend holds the
 * Anthropic API key, so no model-provider credential is ever present in the app.
 */
interface ApiService {
    @GET("materials")
    suspend fun getMaterials(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 50,
    ): ListMaterialResponse

    @GET("materials/{id}")
    suspend fun getMaterialById(
        @Path("id") id: String,
    ): MaterialResponse

    @POST("ai/generate")
    suspend fun generate(
        @Body request: GenerationRequest,
    ): GenerationResponse
}
