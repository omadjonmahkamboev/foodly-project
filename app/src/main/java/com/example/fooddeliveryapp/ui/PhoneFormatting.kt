package com.example.fooddeliveryapp.ui

const val UzbekistanPhonePrefix = "+998"
const val UzbekistanPhoneMask = "90 123 45 67"

private const val UzbekistanCountryCode = "998"
private const val UzbekistanLocalDigits = 9

fun String.uzbekPhoneDigitsKey(): String {
    val digits = filter(Char::isDigit)
    return when {
        digits.startsWith(UzbekistanCountryCode) -> digits.drop(UzbekistanCountryCode.length).take(UzbekistanLocalDigits)
        digits.isBlank() -> ""
        else -> digits.take(UzbekistanLocalDigits)
    }
}

fun String.formatUzbekPhoneInput(): String {
    val localDigits = uzbekPhoneDigitsKey()
    if (localDigits.isEmpty()) return ""

    return buildString {
        append(localDigits.take(2))
        if (localDigits.length > 2) {
            append(" ")
            append(localDigits.drop(2).take(3))
        }
        if (localDigits.length > 5) {
            append(" ")
            append(localDigits.drop(5).take(2))
        }
        if (localDigits.length > 7) {
            append(" ")
            append(localDigits.drop(7).take(2))
        }
    }
}

fun String.isCompleteUzbekPhone(): Boolean =
    uzbekPhoneDigitsKey().length == UzbekistanLocalDigits

fun String.hasUzbekPhoneDigits(): Boolean =
    uzbekPhoneDigitsKey().isNotEmpty()

fun String.toStoredUzbekPhone(): String =
    uzbekPhoneDigitsKey()
        .takeIf { it.length == UzbekistanLocalDigits }
        ?.let {
            "$UzbekistanPhonePrefix (${it.take(2)}) ${it.drop(2).take(3)} ${it.drop(5).take(2)} ${it.drop(7).take(2)}"
        }
        .orEmpty()
