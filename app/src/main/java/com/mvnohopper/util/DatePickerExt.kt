package com.mvnohopper.util

import android.app.DatePickerDialog
import android.content.Context
import java.time.LocalDate

fun Context.showDatePicker(
    initialDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit
) {
    DatePickerDialog(
        this,
        { _, year, month, dayOfMonth ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth
    ).show()
}

fun Context.parseIsoDateOrToday(value: String): LocalDate =
    runCatching { LocalDate.parse(value, DateFormats.ISO_DATE) }.getOrElse { LocalDate.now() }
