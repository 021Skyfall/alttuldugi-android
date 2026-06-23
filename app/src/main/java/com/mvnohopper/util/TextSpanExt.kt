package com.mvnohopper.util

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import com.mvnohopper.R

fun String.toBoldLabeledValueSpan(context: Context, labelSeparator: String = ": "): SpannableString {
    val value = substringAfter(labelSeparator).ifBlank { this }
    val start = indexOf(value)
    val end = start + value.length
    return SpannableString(this).apply {
        if (start < 0) return@apply
        setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_primary)),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}

fun String.toBoldMarkedSpan(marker: String): SpannableString {
    val start = indexOf(marker)
    if (start < 0) return SpannableString(this)

    val valueStart = start + marker.length
    val valueEnd = indexOf("일", valueStart).let { dayIndex ->
        if (dayIndex >= 0) dayIndex + 1 else length
    }
    return SpannableString(this).apply {
        setSpan(StyleSpan(Typeface.BOLD), valueStart, valueEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}
