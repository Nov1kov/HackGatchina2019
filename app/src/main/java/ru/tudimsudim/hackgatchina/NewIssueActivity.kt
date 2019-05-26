package ru.tudimsudim.hackgatchina

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_new_issue.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main
import ru.tudimsudim.hackgatchina.model.Issue
import ru.tudimsudim.hackgatchina.presenter.HttpClient
import ru.tudimsudim.hackgatchina.presenter.HttpJavaUtils


class NewIssueActivity : AppCompatActivity() {

    private lateinit var issue: Issue
    private var isPositive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_issue)

        isPositive = intent.getBooleanExtra("POSITIVE_KEY", false)

        fab.setOnClickListener { view ->
            postIssue()
        }

        photo_card_view.setOnClickListener {
            dispatchTakePictureIntent()
        }

        issue = Issue()
        issue_description.setImeOptions(EditorInfo.IME_ACTION_DONE);
        issue_description.setRawInputType(InputType.TYPE_CLASS_TEXT);
    }

    private fun initAuthor() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        issue.authorUid = sharedPref.getString("uid", "")
        issue.author = sharedPref.getString("name", "")
        issue.authorEmail = sharedPref.getString("email", "")
    }

    private fun postIssue() {
        if (issue.images.isEmpty()) {
            val toastMessage = getString(R.string.you_should_add_photo)
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
            return
        }
        issue.title = issue_header.text.toString()
        issue.text = issue_description.text.toString()
        issue.coordinate = GatchinaApplication.geoMaster.getCoordinates()
        issue.isPositive = isPositive
        initAuthor()

        GlobalScope.launch(Dispatchers.Main) {
            try {
                var addr = ""
                withContext(Dispatchers.IO) {
                    HttpClient.postIssue(issue)
                    addr = HttpClient.getAddress(issue)
                }
                issue.address = addr
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

            val imageView = ImageView(this)
            val padding = resources.getDimensionPixelSize(R.dimen.photo_preview_padding)
            val size = resources.getDimensionPixelSize(R.dimen.preview_image_size) + 2 * padding
            imageView.setLayoutParams(
                LinearLayout.LayoutParams(
                    size,
                    size
                )
            )
            imageView.layoutParams.height = size
            imageView.layoutParams.width = size
            imageView.setPadding(padding, padding, padding, padding)

            Glide
                .with(this)
                .load(imageBitmap)
                .centerCrop()
                .into(imageView);

            photo_container.addView(imageView)
            HttpJavaUtils.uploadBitmap(this, imageBitmap, issue, {
                val imageUrl = String(it.data)
                issue.images.add(imageUrl)
                val resultMessage = this.getString(R.string.image_uploaded)
                Toast.makeText(this, resultMessage, Toast.LENGTH_SHORT).show()
            },
                {
                    val statusCode = if (it.networkResponse != null) it.networkResponse.statusCode else 0
                    Toast.makeText(this, it.message + " code: " + statusCode, Toast.LENGTH_SHORT).show()
                })
        }
    }
}
