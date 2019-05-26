package ru.tudimsudim.hackgatchina.presenter


import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import ru.tudimsudim.hackgatchina.model.Issue
import ru.tudimsudim.hackgatchina.model.yandex.ResponseX
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


object HttpClient {

    val address = "https://g.lp-app.com"
    private val yandex = "https://geocode-maps.yandex.ru/1.x"

    fun postIssue(issue: Issue): String {
        val urlFull = "$address/issues"
        val gson = Gson()
        val body = gson.toJson(issue)

        return URL(urlFull)
            .openConnection()
            .let {
                it as HttpURLConnection
            }.apply {
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                requestMethod = "POST"
                doOutput = true
                val outputWriter = OutputStreamWriter(outputStream)
                outputWriter.write(body)
                outputWriter.flush()
            }.let {
                if (it.responseCode == 200) it.inputStream else it.errorStream
            }.let { streamToRead ->
                BufferedReader(InputStreamReader(streamToRead)).use {
                    val response = StringBuffer()

                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    it.close()
                    response.toString()
                }
            }
    }

    fun getIssues(): List<Issue> {
        val urlFull = "$address/issues"

        return URL(urlFull)
            .openConnection()
            .let {
                it as HttpURLConnection
            }.apply {
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                requestMethod = "GET"
            }.let {
                if (it.responseCode == 200) it.inputStream else it.errorStream
            }.let { streamToRead ->
                BufferedReader(InputStreamReader(streamToRead)).use {
                    val response = StringBuffer()
                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    it.close()
                    val gson = Gson()
                    gson.fromJson(response.toString())
                }
            }
    }

    fun getIssueById(issueId:String): Issue {
        val urlFull = "$address/issues/${issueId}"

        return URL(urlFull)
            .openConnection()
            .let {
                it as HttpURLConnection
            }.apply {
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                requestMethod = "GET"
            }.let {
                if (it.responseCode == 200) it.inputStream else it.errorStream
            }.let { streamToRead ->
                BufferedReader(InputStreamReader(streamToRead)).use {
                    val response = StringBuffer()
                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    it.close()
                    val gson = Gson()
                    gson.fromJson(response.toString())
                }
            }
    }

    class Vote(val authorId : String)

    fun vote(issue: Issue, userUid: String) {
        val urlFull = "$address/issues/vote/${issue.id}"
        val gson = Gson()
        val body = gson.toJson(Vote(userUid))

        return URL(urlFull)
            .openConnection()
            .let {
                it as HttpURLConnection
            }.apply {
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                requestMethod = "POST"
                doOutput = true
                val outputWriter = OutputStreamWriter(outputStream)
                outputWriter.write(body)
                outputWriter.flush()
            }.let {
                if (it.responseCode == 200) it.inputStream else it.errorStream
            }.let { streamToRead ->
                BufferedReader(InputStreamReader(streamToRead)).use {
                    val response = StringBuffer()
                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    it.close()
                    response.toString()
                }
            }
    }

    fun getAddress(issue: Issue): String {
        val urlFull =
            "$yandex/?apikey=b2a258f0-8c3c-4a97-baf8-dad09cc004d0&geocode=${issue.longitude},${issue.latitude}&format=json"


        var let = URL(urlFull)
            .openConnection()
            .let {
                it as HttpURLConnection
            }.apply {
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                requestMethod = "GET"
            }.let {
                if (it.responseCode == 200) it.inputStream else it.errorStream
            }.let { streamToRead ->
                BufferedReader(InputStreamReader(streamToRead)).use {
                    val response = StringBuffer()
                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    it.close()
                    response.toString()
                   response.toString()
                }
            }

        var fromJson = Gson().fromJson(
            let, ResponseX::class.java
        )

        return fromJson.response.GeoObjectCollection.featureMember[0].GeoObject.metaDataProperty.GeocoderMetaData.text
    }
}