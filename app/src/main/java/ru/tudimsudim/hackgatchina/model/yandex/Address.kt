package ru.tudimsudim.hackgatchina.model.yandex

data class Address(
    val Components: List<Component>,
    val country_code: String,
    val formatted: String
)