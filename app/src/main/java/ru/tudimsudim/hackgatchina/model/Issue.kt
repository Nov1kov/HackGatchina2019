package ru.tudimsudim.hackgatchina.model

data class Issue(
    var id: String = "",
    var title: String = "",
    var text: String = "",
    var images: MutableCollection<String> = mutableListOf(),
    var coordinate: List<Double> = emptyList(),
    var author: String = ""
){
    val longitude : Double
        get() = if (coordinate.count() == 2) coordinate[1] else 0.0
    val latitude : Double
        get() = if (coordinate.count() == 2) coordinate[0] else 0.0
}