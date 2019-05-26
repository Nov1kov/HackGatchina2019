package ru.tudimsudim.hackgatchina.model.yandex

data class GeocoderResponseMetaData(
    val Point: Point,
    val found: String,
    val request: String,
    val results: String
)