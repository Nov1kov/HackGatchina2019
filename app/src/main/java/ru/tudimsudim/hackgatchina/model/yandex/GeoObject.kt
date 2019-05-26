package ru.tudimsudim.hackgatchina.model.yandex

data class GeoObject(
    val Point: PointX,
    val boundedBy: BoundedBy,
    val metaDataProperty: MetaDataPropertyX,
    val name: String
)