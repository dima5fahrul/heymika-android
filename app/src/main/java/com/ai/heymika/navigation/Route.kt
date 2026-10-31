package com.ai.heymika.navigation

import com.ai.heymika.core.domain.model.GenerationKind

/**
 * Every route string in the app graph, in one place.
 *
 * [HOME] and [DETAIL] are owned by `:app`. [AISTUDIO], [FAVORITE] and [SETTING] are owned by
 * the dynamic feature modules — they are listed here because `:app` has to be able to
 * *navigate* to them (and to decide which split to install) without seeing their code.
 *
 * Contract for `:aistudio`: register the pattern [AISTUDIO_WITH_KIND] so that the optional
 * `kind` query argument produced by [aiStudio] resolves. A destination registered as plain
 * [AISTUDIO] will not match a URI that carries the query string.
 */
object Route {
    const val HOME = "home"

    const val DETAIL = "detail/{materialId}"

    const val AISTUDIO = "aistudio/{materialId}"

    /** [AISTUDIO] plus the optional [GenerationKind] argument the detail screen passes. */
    const val AISTUDIO_WITH_KIND = "aistudio/{materialId}?kind={kind}"

    const val FAVORITE = "favorite"

    const val SETTING = "setting"

    const val ARG_MATERIAL_ID = "materialId"

    const val ARG_KIND = "kind"

    fun detail(materialId: String): String = "detail/$materialId"

    fun aiStudio(
        materialId: String,
        kind: GenerationKind,
    ): String = "aistudio/$materialId?$ARG_KIND=${kind.name}"
}
