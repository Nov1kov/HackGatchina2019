package ru.tudimsudim.hackgatchina

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_new_issue.*
import kotlinx.coroutines.launch
import ru.tudimsudim.hackgatchina.model.Issue
import ru.tudimsudim.hackgatchina.presenter.HttpClient
import ru.tudimsudim.hackgatchina.presenter.HttpJavaUtils

class NewIssueActivity : AppCompatActivity() {

    private lateinit var issue: Issue
    private var imageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_issue)

        fab.setOnClickListener { view ->
            postIssue()
        }
        issue_image.setOnClickListener {
            dispatchTakePictureIntent()
        }

        issue = Issue()
    }

    private fun initAuthor() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        issue.authorUid = sharedPref.getString("uid", "")
        issue.author = sharedPref.getString("name", "")
        issue.authorEmail = sharedPref.getString("email", "")
    }

    private fun postIssue() {
        issue.title = issue_header.text.toString()
        issue.text = issue_description.text.toString()
        issue.coordinate = GatchinaApplication.geoMaster.getCoordinates()
        initAuthor()

        launch {
            try {
                val issueId = HttpClient.postIssue(issue)
                //issue.id = issueId
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        Toast.makeText(this, "Отправлена проблема", Toast.LENGTH_LONG).show()
        finish()
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

            Glide
                .with(this)
                .load(imageBitmap)
                .centerCrop()
                .into(issue_image);

            photo_hint.visibility = View.GONE
            issue_image.visibility = View.VISIBLE
            HttpJavaUtils.uploadBitmap(this, imageBitmap, issue, {
                val imageUrl = String(it.data)
                issue.images.add(imageUrl)
                val resultMessage = this.getString(R.string.image_uploaded)
                Toast.makeText(this, imageUrl, Toast.LENGTH_SHORT).show()
            },
                {
                    val statusCode = if (it.networkResponse != null) it.networkResponse.statusCode else 0
                    Toast.makeText(this, it.message + " code: " + statusCode, Toast.LENGTH_SHORT).show()
                })
        }
    }
}
