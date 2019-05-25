package ru.tudimsudim.hackgatchina.model

object Data{
    var issues :List<Issue> = emptyList()

    fun issue(id: String):Issue?{
        val issu = issues.findLast { it.id == id }
        return issu
    }
}