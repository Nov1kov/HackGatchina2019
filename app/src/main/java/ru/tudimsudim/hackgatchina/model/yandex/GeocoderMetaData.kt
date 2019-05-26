package ru.tudimsudim.hackgatchina.model.yandex

import ru.tudimsudim.hackgatchina.model.yandex.Address
import ru.tudimsudim.hackgatchina.model.yandex.AddressDetails

data class GeocoderMetaData(
    val Address: Address,
    val AddressDetails: AddressDetails,
    val kind: String,
    val precision: String,
    val text: String
)