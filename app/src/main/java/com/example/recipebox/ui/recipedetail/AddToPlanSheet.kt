package com.example.recipebox.ui.recipedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recipebox.util.plural
import com.example.recipebox.util.startOfWeek
import com.example.recipebox.util.weekDays
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Two rows of day chips, this week and next, for adding a recipe to the plan at [servings]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlanSheet(
    today: LocalDate,
    servings: Int,
    onPick: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val scope = rememberCoroutineScope()
    val thisWeek = startOfWeek(today)

    fun pick(day: LocalDate) {
        onPick(day)
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Add to plan", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp))
            Text(
                "At ${plural(servings, "serving")}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            WeekRow("This week", thisWeek, today, ::pick)
            WeekRow("Next week", thisWeek.plusWeeks(1), today, ::pick)
        }
    }
}

private val ShortDayFormat = DateTimeFormatter.ofPattern("EEE")

@Composable
private fun WeekRow(label: String, weekStart: LocalDate, today: LocalDate, onPick: (LocalDate) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            weekDays(weekStart).forEach { day ->
                DayChip(
                    day = day,
                    isToday = day == today,
                    isPast = day < today,
                    onClick = { onPick(day) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Short day name over the date. Past days stay pickable but are dimmed. */
@Composable
private fun DayChip(day: LocalDate, isToday: Boolean, isPast: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(64.dp)
            .alpha(if (isPast) 0.5f else 1f),
        shape = RoundedCornerShape(16.dp),
        color = if (isToday) colors.primaryContainer else colors.surfaceContainerHigh,
        contentColor = if (isToday) colors.onPrimaryContainer else colors.onSurface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(day.format(ShortDayFormat), style = MaterialTheme.typography.labelMedium)
            Text("${day.dayOfMonth}", style = MaterialTheme.typography.titleLarge)
        }
    }
}
