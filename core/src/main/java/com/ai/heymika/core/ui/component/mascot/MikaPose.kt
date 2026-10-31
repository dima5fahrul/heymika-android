package com.ai.heymika.core.ui.component.mascot

import androidx.annotation.StringRes
import com.ai.heymika.core.R
import com.ai.heymika.core.domain.model.MikaPose

/** TalkBack description of the pose, for a [MikaAvatar] that is not purely decorative. */
@StringRes
fun MikaPose.contentDescriptionRes(): Int =
    when (this) {
        MikaPose.WAVING -> R.string.mascot_pose_waving
        MikaPose.WAVING_PENCIL -> R.string.mascot_pose_waving_pencil
        MikaPose.ENVELOPE -> R.string.mascot_pose_envelope
        MikaPose.HAPPY -> R.string.mascot_pose_happy
        MikaPose.WORRIED -> R.string.mascot_pose_worried
        MikaPose.THINKING -> R.string.mascot_pose_thinking
        MikaPose.CONFUSED -> R.string.mascot_pose_confused
        MikaPose.SLEEPING -> R.string.mascot_pose_sleeping
        MikaPose.CONFUSED_MAP -> R.string.mascot_pose_confused_map
        MikaPose.PADLOCK -> R.string.mascot_pose_padlock
        MikaPose.YAWN_HOURGLASS -> R.string.mascot_pose_yawn_hourglass
        MikaPose.UNPLUGGED -> R.string.mascot_pose_unplugged
        MikaPose.HARDHAT -> R.string.mascot_pose_hardhat
        MikaPose.TROPHY -> R.string.mascot_pose_trophy
    }
