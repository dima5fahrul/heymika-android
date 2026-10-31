package com.ai.heymika.core.domain.usecase

import com.ai.heymika.core.domain.model.Material
import com.ai.heymika.core.domain.repository.IMaterialRepository

class MaterialInteractor(
    private val materialRepository: IMaterialRepository,
) : MaterialUseCase {
    override fun getAllMaterials() = materialRepository.getAllMaterials()

    override fun getMaterialById(id: String) = materialRepository.getMaterialById(id)

    override fun getFavoriteMaterials() = materialRepository.getFavoriteMaterials()

    override suspend fun setFavorite(
        material: Material,
        state: Boolean,
    ) = materialRepository.setFavorite(material, state)
}
