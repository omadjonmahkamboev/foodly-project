package com.example.fooddeliveryapp.ui

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private const val SomsPerPriceUnit = 90

private val groupedNumberFormat = DecimalFormat(
    "#,###",
    DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ' '
    },
)

fun formatCurrency(amount: Int, language: AppLanguage): String =
    "${groupedNumberFormat.format(amount.toLong() * SomsPerPriceUnit)} сум"
