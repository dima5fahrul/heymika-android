package com.ai.heymika.core.utils.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A daily time window such as quiet hours, at minute precision. When [end] is before [start] the
 * window crosses midnight: 22:00-07:00 runs from 22:00 today to 07:00 tomorrow. [start] is
 * inclusive and [end] exclusive.
 */
data class TimeRange(
    val start: LocalTime,
    val end: LocalTime,
) {
    val crossesMidnight: Boolean
        get() = TimeMath.minuteOfDay(end) < TimeMath.minuteOfDay(start)

    /** Length of the window in minutes, wrapping past midnight (22:00-07:00 is 540). */
    val durationMinutes: Int
        get() = TimeMath.minutesBetween(start, end)

    operator fun contains(time: LocalTime): Boolean = TimeMath.contains(this, time)
}

/** Why a [TimeRange] was rejected by [TimeMath.validate]. */
enum class TimeRangeError {
    /** Start and end are the same minute, so the window is empty. */
    SAME_TIME,

    /** The window crosses midnight but the field does not allow it. */
    CROSSES_MIDNIGHT,
}

/** Minute-of-day arithmetic for [TimeRange] and 24-hour formatting. */
object TimeMath {
    const val MINUTES_PER_HOUR = 60
    const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR

    private val clockFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)

    fun minuteOfDay(time: LocalTime): Int = time.hour * MINUTES_PER_HOUR + time.minute

    /** The time at [minuteOfDay], wrapped into one day (1440 is 00:00, -60 is 23:00). */
    fun timeAtMinute(minuteOfDay: Int): LocalTime {
        val minute = Math.floorMod(minuteOfDay, MINUTES_PER_DAY)
        return LocalTime.of(minute / MINUTES_PER_HOUR, minute % MINUTES_PER_HOUR)
    }

    /** Minutes from [start] forward to [end], wrapping past midnight. 0 when they are equal. */
    fun minutesBetween(
        start: LocalTime,
        end: LocalTime,
    ): Int = Math.floorMod(minuteOfDay(end) - minuteOfDay(start), MINUTES_PER_DAY)

    /** True when [time] falls inside [range] (start inclusive, end exclusive). An empty range has nothing. */
    fun contains(
        range: TimeRange,
        time: LocalTime,
    ): Boolean {
        val start = minuteOfDay(range.start)
        val end = minuteOfDay(range.end)
        val minute = minuteOfDay(time)
        return when {
            start == end -> false
            start < end -> minute in start until end
            else -> minute >= start || minute < end
        }
    }

    /** Null when [range] is usable; a range across midnight is valid unless [allowCrossMidnight] is false. */
    fun validate(
        range: TimeRange,
        allowCrossMidnight: Boolean = true,
    ): TimeRangeError? =
        when {
            minuteOfDay(range.start) == minuteOfDay(range.end) -> TimeRangeError.SAME_TIME
            !allowCrossMidnight && range.crossesMidnight -> TimeRangeError.CROSSES_MIDNIGHT
            else -> null
        }

    /** 24-hour "HH:mm", as used across the app ("07:00", "22:00"). */
    fun formatClock(time: LocalTime): String = time.format(clockFormatter)

    /** Whole hours and the remaining minutes of a duration: 510 gives (8, 30). Negative counts as 0. */
    fun splitHoursMinutes(totalMinutes: Int): Pair<Int, Int> {
        val minutes = totalMinutes.coerceAtLeast(0)
        return minutes / MINUTES_PER_HOUR to minutes % MINUTES_PER_HOUR
    }
}

/**
 * The time source of a countdown, in milliseconds. A deadline and its clock must share one
 * timebase: a monotonic clock for local cooldowns, the wall clock for server epoch deadlines.
 * Tests and previews pass a fixed or fake clock.
 */
fun interface CountdownClock {
    fun nowMillis(): Long
}

/** Countdown arithmetic for resend cooldowns, quiz timers and link expiry. */
object CountdownMath {
    private const val MILLIS_PER_SECOND = 1_000L
    private const val SECONDS_PER_MINUTE = 60

    fun deadlineAfter(
        nowMillis: Long,
        seconds: Int,
    ): Long = nowMillis + seconds.coerceAtLeast(0) * MILLIS_PER_SECOND

    /** Whole seconds left, rounded up (1 ms left still shows 00:01), never negative. */
    fun remainingSeconds(
        deadlineMillis: Long,
        nowMillis: Long,
    ): Int {
        val left = deadlineMillis - nowMillis
        if (left <= 0L) return 0
        val seconds = (left + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND
        return seconds.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    /** Delay until the displayed second next changes, between 1 and 1000 ms; 0 once expired. */
    fun millisUntilNextTick(
        deadlineMillis: Long,
        nowMillis: Long,
    ): Long {
        val left = deadlineMillis - nowMillis
        if (left <= 0L) return 0L
        return (left - 1) % MILLIS_PER_SECOND + 1
    }

    /** "mm:ss" with at least two minute digits: 0 gives "00:00", 868 gives "14:28", 6000 gives "100:00". */
    fun formatClock(totalSeconds: Int): String {
        val (minutes, seconds) = splitMinutesSeconds(totalSeconds)
        return "%02d:%02d".format(Locale.ROOT, minutes, seconds)
    }

    /** Whole minutes and the remaining seconds: 868 gives (14, 28). Negative counts as 0. */
    fun splitMinutesSeconds(totalSeconds: Int): Pair<Int, Int> {
        val seconds = totalSeconds.coerceAtLeast(0)
        return seconds / SECONDS_PER_MINUTE to seconds % SECONDS_PER_MINUTE
    }
}

/**
 * Calendar helpers for the date picker. Material3 date pickers work in UTC-midnight milliseconds;
 * these convert to and from [LocalDate] and format dates the Indonesian way ("14 Juli 2025").
 */
object DateMath {
    /** The app is Indonesian-only, so dates are formatted and laid out in `id-ID` whatever the device locale. */
    val locale: Locale = Locale.forLanguageTag("id-ID")
    private val longDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)

    fun formatLongDate(date: LocalDate): String = date.format(longDateFormatter)

    fun toUtcMillis(date: LocalDate): Long = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun fromUtcMillis(utcMillis: Long): LocalDate = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()

    /** True when [date] is within [min]..[max]; a null bound is open. */
    fun isWithin(
        date: LocalDate,
        min: LocalDate?,
        max: LocalDate?,
    ): Boolean = (min == null || !date.isBefore(min)) && (max == null || !date.isAfter(max))

    /** True when any day of [year] can be within [min]..[max]. */
    fun isYearWithin(
        year: Int,
        min: LocalDate?,
        max: LocalDate?,
    ): Boolean = (min == null || year >= min.year) && (max == null || year <= max.year)
}
