package ru.tudimsudim.hackgatchina

import android.content.res.ColorStateList
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_issue.*
import kotlinx.android.synthetic.main.activity_new_issue.*
import ru.tudimsudim.hackgatchina.model.Issue
import ru.tudimsudim.hackgatchina.presenter.HttpClient

class IssueActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue)
        val issue_id = intent.getStringExtra(ID_ISSUE_KEY)

        val iss = GatchinaApplication.data.issue(issue_id)

        val issue : Issue = if (iss == null) Issue() else {
            iss
        }

        if (!canVote()){
            vote_fab.setImageResource(R.drawable.ic_ok)
            val color = ContextCompat.getColor(this, R.color.VoteColor)
            vote_fab.backgroundTintList = ColorStateList.valueOf(color)
        }

        vote_fab.setOnClickListener {
            if (canVote())
                HttpClient.vote(issue)
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

    private fun canVote(): Boolean {
        return true
    }

    companion object {
        const val ID_ISSUE_KEY = "ID_ISSUE_KEY"
    }
}
