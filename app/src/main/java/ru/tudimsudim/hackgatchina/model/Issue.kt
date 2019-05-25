package ru.tudimsudim.hackgatchina.model

data class Issue(
    var id: String = "",
    var title: String = "",
    var text: String = "",
    var images: MutableCollection<String> = mutableListOf(),
    var longitude : Double = 30.3,
    var latitude : Double = 59.9,
    var author: String = ""
)