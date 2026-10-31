package com.ai.heymika.core.utils.text

import java.util.Locale

private const val MAX_INITIALS = 2

/**
 * Up to two uppercase initials for an avatar: the first letter or digit of the first and of the
 * last word ("Budi Santoso Putra" gives "BP", "mika" gives "M"). Words without a letter or digit
 * are skipped, and a blank name gives "".
 */
fun initialsOf(name: String): String {
    val firsts =
        name
            .trim()
            .split(Regex("\\s+"))
            .mapNotNull { word ->
                val index = word.indexOfFirst(Char::isLetterOrDigit)
                if (index < 0) null else firstCodePointAt(word, index)
            }
    val picked = if (firsts.size > MAX_INITIALS) listOf(firsts.first(), firsts.last()) else firsts
    return picked.joinToString(separator = "").uppercase(Locale.ROOT)
}

private fun firstCodePointAt(
    word: String,
    index: Int,
): String = String(Character.toChars(word.codePointAt(index)))
