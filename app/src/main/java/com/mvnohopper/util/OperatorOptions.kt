package com.mvnohopper.util

import android.content.Context
import com.mvnohopper.R

object OperatorOptions {
    val stringResIds: List<Int> = listOf(
        R.string.operator_option_kt,
        R.string.operator_option_sk,
        R.string.operator_option_lg
    )

    fun labels(context: Context): List<String> =
        stringResIds.map { context.getString(it) }
}
