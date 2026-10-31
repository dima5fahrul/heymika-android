package com.ai.heymika.core.utils.text

/**
 * Applies an edit from [old] to [new] under [maxLength], like HTML `maxlength`: an edit that fits,
 * or that does not grow the text, passes unchanged; otherwise only the part of the inserted text
 * that still fits is kept, so a long paste is cut instead of ignored. The edit is found as the
 * span between the common prefix and the common suffix of [old] and [new].
 */
fun clampEditToMaxLength(
    old: String,
    new: String,
    maxLength: Int,
): String {
    if (new.length <= maxLength || new.length <= old.length) return new
    val shorter = minOf(old.length, new.length)
    var prefix = 0
    while (prefix < shorter && old[prefix] == new[prefix]) prefix++
    var suffix = 0
    while (suffix < shorter - prefix && old[old.length - 1 - suffix] == new[new.length - 1 - suffix]) suffix++
    val kept = prefix + suffix
    val room = (maxLength - kept).coerceAtLeast(0)
    val inserted = new.substring(prefix, new.length - suffix)
    return new.substring(0, prefix) + inserted.take(room) + new.substring(new.length - suffix)
}
