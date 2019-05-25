package ru.tudimsudim.hackgatchina

import android.content.res.ColorStateList
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_issue.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main
import ru.tudimsudim.hackgatchina.model.Issue
import ru.tudimsudim.hackgatchina.presenter.HttpClient
import java.lang.Exception

class IssueActivity : AppCompatActivity() {

    private lateinit var issue: Issue
    private lateinit var userUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue)
        val issue_id = intent.getStringExtra(ID_ISSUE_KEY)

        val iss = GatchinaApplication.data.issue(issue_id)

        issue = if (iss == null) Issue() else {
            iss
        }

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        userUid = sharedPref.getString("uid", "")

        if (!canVote()){
            vote_fab.setImageResource(R.drawable.ic_ok)
            val color = ContextCompat.getColor(this, R.color.VoteColor)
            vote_fab.backgroundTintList = ColorStateList.valueOf(color)
        }

        vote_fab.setOnClickListener {
            if (canVote())
                this.vote()
            else
                this.finish()
        }
        if (issue.images.count() > 0){
            val imageUrl = HttpClient.address + "/images/" + issue.images.elementAt(0)
            Glide
                .with(this)
                .load(imageUrl)
                .centerCrop()
                .into(exists_issue_image);
        }

        exists_issue_header.text = issue.title
        exists_issue_description.text = issue.text
    }

    private fun vote(){
        GlobalScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    HttpClient.vote(issue, userUid)
                }
                Toast.makeText(this@IssueActivity, getString(R.string.vote_success), Toast.LENGTH_SHORT).show()
            }catch (ex: Exception){
                ex.printStackTrace()
            }
        }
    }

    private fun canVote(): Boolean {
        return !issue.users_like.contains(userUid)
    }

    companion object {
        const val ID_ISSUE_KEY = "ID_ISSUE_KEY"
    }
}
