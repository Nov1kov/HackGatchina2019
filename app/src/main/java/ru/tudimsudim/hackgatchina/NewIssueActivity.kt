package ru.tudimsudim.hackgatchina

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity;

import kotlinx.android.synthetic.main.activity_new_issue.*
import kotlinx.coroutines.launch
import ru.tudimsudim.hackgatchina.model.Issue
import ru.tudimsudim.hackgatchina.presenter.HttpClient
import ru.tudimsudim.hackgatchina.presenter.HttpJavaUtils
import java.lang.Exception

class NewIssueActivity : AppCompatActivity() {

    private lateinit var issue : Issue
    private var imageUrl : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_issue)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            dispatchTakePictureIntent()
        }
        issue_image.setOnClickListener {
            postIssue()
        }
    }

    private fun postIssue(){
        var issueId = ""
        val isssue = Issue(
            "",
            issue_header.text.toString(),
            issue_description.text.toString(),
            imageUrl
            )
        launch {
            try {
                issueId = HttpClient.postIssue(isssue)
            }catch (ex: Exception){
                ex.printStackTrace()
            }
        }
        println(issueId)
    }

    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       // super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            val imageBitmap = data.extras.get("data") as Bitmap
            issue_image.setImageBitmap(imageBitmap)
            HttpJavaUtils.uploadBitmap(this, imageBitmap)
        }
    }
}
