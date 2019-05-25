package ru.tudimsudim.hackgatchina.model

data class Issue(
    var id: String = "",
    var title: String = "",
    var text: String = "",
    var images: MutableCollection<String> = mutableListOf()
)