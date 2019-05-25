package ru.tudimsudim.hackgatchina.presenter

import com.google.gson.Gson
import ru.tudimsudim.hackgatchina.model.Issue
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


import com.github.salomonbrys.kotson.*


object HttpClient{

    //val address = "https://httpbin.davecheney.com/"
    val address = "https://g.lp-app.com"

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
}