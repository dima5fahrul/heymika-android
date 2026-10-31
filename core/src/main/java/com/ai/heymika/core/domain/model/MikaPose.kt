package com.ai.heymika.core.domain.model

/**
 * Every Mika illustration the app asks for. A plain enum, so non-UI mappers such as
 * ErrorStateCopies can choose a pose without depending on core/ui. The art behind each pose is
 * resolved in one place, rememberMikaPainter (core/ui), so the placeholder vectors can be swapped
 * for the real assets without touching any caller.
 *
 * The last six poses belong to the system states: [CONFUSED_MAP] (404), [PADLOCK] (403),
 * [YAWN_HOURGLASS] (session expired), [UNPLUGGED] (offline), [HARDHAT] (maintenance) and
 * [TROPHY] (quota reached).
 */
enum class MikaPose {
    WAVING,
    WAVING_PENCIL,
    ENVELOPE,
    HAPPY,
    WORRIED,
    THINKING,
    CONFUSED,
    SLEEPING,
    CONFUSED_MAP,
    PADLOCK,
    YAWN_HOURGLASS,
    UNPLUGGED,
    HARDHAT,
    TROPHY,
}
