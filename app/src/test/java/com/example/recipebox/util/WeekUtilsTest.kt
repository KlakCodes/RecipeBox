package com.example.recipebox.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

class WeekUtilsTest {
    private val monday = LocalDate.of(2026, 11, 9)

    @Test
    fun `every day of the week maps to its Monday`() {
        for (offset in 0L..6L) {
            assertEquals(monday, startOfWeek(monday.plusDays(offset)))
        }
    }

    @Test
    fun `Sunday belongs to the week before, not the next one`() {
        assertEquals(monday, startOfWeek(LocalDate.of(2026, 11, 15)))
        assertEquals(LocalDate.of(2026, 11, 16), startOfWeek(LocalDate.of(2026, 11, 16)))
    }

    @Test
    fun `week crossing a month starts in the earlier month`() {
        assertEquals(LocalDate.of(2026, 10, 26), startOfWeek(LocalDate.of(2026, 11, 1)))
    }

    @Test
    fun `week crossing a year starts in the earlier year`() {
        assertEquals(LocalDate.of(2025, 12, 29), startOfWeek(LocalDate.of(2026, 1, 1)))
        assertEquals(LocalDate.of(2025, 12, 29), startOfWeek(LocalDate.of(2026, 1, 4)))
    }

    @Test
    fun `leap day is in the right week`() {
        assertEquals(LocalDate.of(2028, 2, 28), startOfWeek(LocalDate.of(2028, 2, 29)))
        assertEquals(LocalDate.of(2028, 2, 28), startOfWeek(LocalDate.of(2028, 3, 5)))
    }

    @Test
    fun `weekDays runs Monday to Sunday across a year end`() {
        val days = weekDays(LocalDate.of(2025, 12, 29))
        assertEquals(7, days.size)
        assertEquals(DayOfWeek.MONDAY, days.first().dayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, days.last().dayOfWeek)
        assertEquals(LocalDate.of(2026, 1, 4), days.last())
        assertEquals(days.sorted(), days)
    }

    @Test
    fun `range within one month shows the month once`() {
        assertEquals("9 – 15 Nov", formatWeekRange(monday, Locale.UK))
    }

    @Test
    fun `range across months shows both months`() {
        assertEquals("26 Oct – 1 Nov", formatWeekRange(LocalDate.of(2026, 10, 26), Locale.UK))
        assertEquals("30 Mar – 5 Apr", formatWeekRange(LocalDate.of(2026, 3, 30), Locale.UK))
    }

    @Test
    fun `range across years shows both years`() {
        assertEquals("29 Dec 2025 – 4 Jan 2026", formatWeekRange(LocalDate.of(2025, 12, 29), Locale.UK))
    }

    @Test
    fun `range across February in a leap year`() {
        assertEquals("28 Feb – 5 Mar", formatWeekRange(LocalDate.of(2028, 2, 28), Locale.UK))
    }

    @Test
    fun `previous and next week stay on Mondays across a year end`() {
        val start = LocalDate.of(2025, 12, 29)
        assertEquals(LocalDate.of(2026, 1, 5), start.plusWeeks(1))
        assertEquals(DayOfWeek.MONDAY, start.minusWeeks(1).dayOfWeek)
        assertEquals("5 – 11 Jan", formatWeekRange(start.plusWeeks(1), Locale.UK))
        assertEquals("22 – 28 Dec", formatWeekRange(start.minusWeeks(1), Locale.UK))
    }
}
