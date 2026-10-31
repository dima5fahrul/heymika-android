package com.ai.heymika.core.utils.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class TimeMathTest {
    private val quietHours = TimeRange(LocalTime.of(22, 0), LocalTime.of(7, 0))

    @Test
    fun `range across midnight wraps its duration`() {
        assertTrue(quietHours.crossesMidnight)
        assertEquals(540, quietHours.durationMinutes)
    }

    @Test
    fun `range across midnight contains late and early times but not its end`() {
        assertTrue(LocalTime.of(23, 30) in quietHours)
        assertTrue(LocalTime.of(6, 59) in quietHours)
        assertTrue(LocalTime.of(22, 0) in quietHours)
        assertFalse(LocalTime.of(7, 0) in quietHours)
        assertFalse(LocalTime.of(12, 0) in quietHours)
    }

    @Test
    fun `same day range contains only its own window`() {
        val school = TimeRange(LocalTime.of(7, 0), LocalTime.of(14, 30))
        assertFalse(school.crossesMidnight)
        assertEquals(450, school.durationMinutes)
        assertTrue(LocalTime.of(10, 0) in school)
        assertFalse(LocalTime.of(14, 30) in school)
        assertFalse(LocalTime.of(6, 59) in school)
    }

    @Test
    fun `empty range contains nothing and is rejected`() {
        val empty = TimeRange(LocalTime.of(8, 0), LocalTime.of(8, 0))
        assertFalse(LocalTime.of(8, 0) in empty)
        assertEquals(0, empty.durationMinutes)
        assertEquals(TimeRangeError.SAME_TIME, TimeMath.validate(empty))
    }

    @Test
    fun `crossing midnight is rejected only when not allowed`() {
        assertNull(TimeMath.validate(quietHours))
        assertEquals(TimeRangeError.CROSSES_MIDNIGHT, TimeMath.validate(quietHours, allowCrossMidnight = false))
    }

    @Test
    fun `time at minute wraps into one day`() {
        assertEquals(LocalTime.MIDNIGHT, TimeMath.timeAtMinute(1_440))
        assertEquals(LocalTime.of(23, 0), TimeMath.timeAtMinute(-60))
        assertEquals(LocalTime.of(7, 30), TimeMath.timeAtMinute(450))
    }

    @Test
    fun `clock and duration formatting`() {
        assertEquals("07:05", TimeMath.formatClock(LocalTime.of(7, 5)))
        assertEquals(8 to 30, TimeMath.splitHoursMinutes(510))
        assertEquals(0 to 0, TimeMath.splitHoursMinutes(-5))
    }

    @Test
    fun `remaining seconds round up and never go negative`() {
        assertEquals(1, CountdownMath.remainingSeconds(deadlineMillis = 1_001, nowMillis = 1_000))
        assertEquals(60, CountdownMath.remainingSeconds(deadlineMillis = 60_000, nowMillis = 0))
        assertEquals(0, CountdownMath.remainingSeconds(deadlineMillis = 1_000, nowMillis = 2_000))
    }

    @Test
    fun `deadline after ignores negative seconds`() {
        assertEquals(61_000L, CountdownMath.deadlineAfter(nowMillis = 1_000, seconds = 60))
        assertEquals(1_000L, CountdownMath.deadlineAfter(nowMillis = 1_000, seconds = -5))
    }

    @Test
    fun `next tick lands exactly when the shown second changes`() {
        assertEquals(1_000L, CountdownMath.millisUntilNextTick(deadlineMillis = 5_000, nowMillis = 0))
        assertEquals(250L, CountdownMath.millisUntilNextTick(deadlineMillis = 5_000, nowMillis = 3_750))
        assertEquals(0L, CountdownMath.millisUntilNextTick(deadlineMillis = 5_000, nowMillis = 5_000))
    }

    @Test
    fun `countdown clock formatting`() {
        assertEquals("00:00", CountdownMath.formatClock(0))
        assertEquals("14:28", CountdownMath.formatClock(868))
        assertEquals("100:00", CountdownMath.formatClock(6_000))
        assertEquals(0 to 0, CountdownMath.splitMinutesSeconds(-1))
    }

    @Test
    fun `dates format the Indonesian way and round trip through utc millis`() {
        val date = LocalDate.of(2025, 7, 14)
        assertEquals("14 Juli 2025", DateMath.formatLongDate(date))
        assertEquals(date, DateMath.fromUtcMillis(DateMath.toUtcMillis(date)))
        assertEquals(0L, DateMath.toUtcMillis(LocalDate.of(1970, 1, 1)))
    }

    @Test
    fun `date bounds are inclusive and open when null`() {
        val min = LocalDate.of(2025, 1, 1)
        val max = LocalDate.of(2025, 12, 31)
        assertTrue(DateMath.isWithin(min, min, max))
        assertTrue(DateMath.isWithin(max, min, max))
        assertFalse(DateMath.isWithin(LocalDate.of(2026, 1, 1), min, max))
        assertTrue(DateMath.isWithin(LocalDate.of(1990, 1, 1), null, max))
        assertTrue(DateMath.isYearWithin(2025, min, max))
        assertFalse(DateMath.isYearWithin(2024, min, null))
    }
}
