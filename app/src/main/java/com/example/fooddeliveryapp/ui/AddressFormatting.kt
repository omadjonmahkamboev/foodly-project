package com.example.fooddeliveryapp.ui

import com.example.fooddeliveryapp.ui.data.DeliveryAddress
import java.util.Locale

const val AddressLabelHome = "HOME"
const val AddressLabelOffice = "OFFICE"
const val AddressLabelOther = "OTHER"

fun canonicalAddressLabel(rawLabel: String): String {
    val normalized = rawLabel.normalizedAddressLabel()
    return when (normalized) {
        "HOME", "HOUSE", "DOM", "ДОМ", "UY" -> AddressLabelHome
        "WORK", "OFFICE", "ОФИС", "OFIS", "ISH" -> AddressLabelOffice
        "OTHER", "ДРУГОЕ", "BOSHQA" -> AddressLabelOther
        else -> rawLabel.trim()
    }
}

fun normalizeAddressLabelForStorage(rawLabel: String): String {
    val canonical = canonicalAddressLabel(rawLabel)
    return when (canonical) {
        AddressLabelHome, AddressLabelOffice, AddressLabelOther -> canonical
        else -> canonical.ifBlank { AddressLabelHome }
    }
}

fun isOfficeAddressLabel(rawLabel: String): Boolean =
    canonicalAddressLabel(rawLabel) == AddressLabelOffice

fun AppStrings.localizedAddressLabel(rawLabel: String): String =
    when (canonicalAddressLabel(rawLabel)) {
        AddressLabelHome -> homeAddress
        AddressLabelOffice -> officeAddress
        AddressLabelOther -> otherAddress
        else -> rawLabel.trim().ifBlank { homeAddress }
    }

fun DeliveryAddress.displayLabel(strings: AppStrings): String =
    strings.localizedAddressLabel(label)

fun DeliveryAddress.displayTitle(strings: AppStrings): String =
    title.takeUnless { strings.isGenericAddressTitle(it) }
        .orEmpty()
        .localizedKnownAddressTitle(strings.currentLanguage)
        .ifBlank { strings.selectedMapPoint }

fun AppStrings.isGenericAddressTitle(title: String): Boolean {
    val normalized = title.trim().lowercase(Locale.ROOT)
    if (normalized.isBlank()) return true

    return genericAddressTitles.any { value ->
        value.trim().lowercase(Locale.ROOT) == normalized
    }
}

fun AppLanguage.yandexGeocoderLanguage(): String =
    when (this) {
        AppLanguage.English -> "en_US"
        AppLanguage.Russian -> "ru_RU"
        AppLanguage.Uzbek -> "uz_UZ"
    }

private fun String.normalizedAddressLabel(): String =
    trim()
        .replace(Regex("\\s+"), " ")
        .uppercase(Locale.ROOT)

private fun String.localizedKnownAddressTitle(language: AppLanguage): String {
    val normalized = trim().lowercase(Locale.ROOT)
    val knownAddress = knownAddressTitles.firstOrNull { titles ->
        titles.any { it.lowercase(Locale.ROOT) == normalized }
    } ?: return this

    return when (language) {
        AppLanguage.English -> knownAddress.english
        AppLanguage.Russian -> knownAddress.russian
        AppLanguage.Uzbek -> knownAddress.uzbek
    }
}

private data class KnownAddressTitle(
    val russian: String,
    val english: String,
    val uzbek: String,
) {
    fun any(predicate: (String) -> Boolean): Boolean =
        predicate(russian) || predicate(english) || predicate(uzbek)
}

private val knownAddressTitles = listOf(
    KnownAddressTitle("Улица Ислама Каримова", "Islam Karimov Street", "Islom Karimov ko'chasi"),
    KnownAddressTitle("Улица Амира Темура", "Amir Temur Street", "Amir Temur ko'chasi"),
    KnownAddressTitle("Улица Шахрисабз", "Shakhrisabz Street", "Shahrisabz ko'chasi"),
    KnownAddressTitle("Улица Бунёдкор", "Bunyodkor Street", "Bunyodkor ko'chasi"),
    KnownAddressTitle("Улица Махтумкули", "Makhtumkuli Street", "Maxtumquli ko'chasi"),
)

private val genericAddressTitles by lazy {
    buildSet {
        listOf(AppLanguage.English, AppLanguage.Russian, AppLanguage.Uzbek).forEach { language ->
            val strings = AppStrings(language)
            add(strings.locationTitle)
            add(strings.selectedMapPoint)
        }
    }
}
