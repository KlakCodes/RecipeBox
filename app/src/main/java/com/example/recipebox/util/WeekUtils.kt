package com.example.recipebox.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/** The Monday on or before [date]. Weeks run Monday to Sunday. */
fun startOfWeek(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

/** The seven days of the week starting at [weekStart]. */
fun weekDays(weekStart: LocalDate): List<LocalDate> = (0L..6L).map { weekStart.plusDays(it) }

/**
 * "10 – 16 Nov", "27 Oct – 2 Nov", or with years when the week spans two of them:
 * "29 Dec 2025 – 4 Jan 2026".
 */
fun formatWeekRange(weekStart: LocalDate, locale: Locale = Locale.getDefault()): String {
    val end = weekStart.plusDays(6)
    val dayMonth = DateTimeFormatter.ofPattern("d MMM", locale)
    return when {
        weekStart.year != end.year -> {
            val full = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
            "${weekStart.format(full)} – ${end.format(full)}"
        }
        weekStart.month != end.month -> "${weekStart.format(dayMonth)} – ${end.format(dayMonth)}"
        else -> "${weekStart.dayOfMonth} – ${end.format(dayMonth)}"
    }
}
