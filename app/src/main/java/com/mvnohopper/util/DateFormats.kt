package com.mvnohopper.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateFormats {
    val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val ISO_DATE_TIME: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun nowIsoDateTime(): String = LocalDateTime.now().format(ISO_DATE_TIME)

    fun formatDate(date: LocalDate): String = date.format(ISO_DATE)
}
