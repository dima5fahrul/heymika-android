package com.ai.heymika.core.domain.usecase

import com.ai.heymika.core.domain.model.GenerationKind
import com.ai.heymika.core.domain.repository.IAiRepository

class AiInteractor(
    private val aiRepository: IAiRepository,
) : AiUseCase {
    override fun generate(
        materialId: String,
        kind: GenerationKind,
    ) = aiRepository.generate(materialId, kind)

    override fun getHistory(materialId: String) = aiRepository.getHistory(materialId)
}
