package ru.tudimsudim.hackgatchina.presenter

import com.google.gson.Gson
import ru.tudimsudim.hackgatchina.model.Issue
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


import com.github.salomonbrys.kotson.*
import java.io.*


object HttpClient{

    //val address = "https://httpbin.davecheney.com/"
    val address = "https://g.lp-app.com"


    private val LINE_FEED = "\r\n"
    private val maxBufferSize = 1024 * 1024
    private val charset = "UTF-8"


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

/*
    fun postImage(bytes: ByteArray): String {
        val urlFull = "$address/images"


        val boundary: String = "===" + System.currentTimeMillis() + "==="

        return URL(urlFull)
            .openConnection()
            .let {
                it as HttpURLConnection
            }.apply {
                setRequestProperty("Accept-Charset", "UTF-8")
                setRequestProperty("Connection", "Keep-Alive")
                setRequestProperty("Cache-Control", "no-cache")
                setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary)
                requestMethod = "POST"
                doOutput = true
                doInput = true
                val writer = DataOutputStream(outputStream)
                writer.append("--").append(boundary).append(LINE_FEED)
                writer.append("Content-Disposition: file; name=\"").append("frgtr.jpeg")
                    .append("\"; filename=\"").append("frgtr.jpeg").append("\"").append(LINE_FEED)
                writer.append("Content-Type: ").append("").append(LINE_FEED)
                writer.append(LINE_FEED)

                writer.write(bytes)
                // send multipart form data necesssary after file data...
                writer.append(LINE_FEED);
                writer.flush()
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
    }*/
}