package ru.tudimsudim.hackgatchina

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.coroutines.launch
import ru.tudimsudim.hackgatchina.model.Issue
import ru.tudimsudim.hackgatchina.presenter.HttpClient
import java.lang.Exception

class NearestIssuesActivity : AppCompatActivity() {

    var geo: GeoMaster = GeoMaster(this)
    private lateinit var adapter : IssuesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        fab.setOnClickListener { view ->
            openNewIssue();
        }
        geo.init()
        recycler_view.layoutManager = LinearLayoutManager(this)
        adapter = IssuesAdapter()
        recycler_view.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        var locations = geo.getUpdatedLocations()
        println(locations?.longitude)
        println(locations?.latitude)

        var issues = emptyList<Issue>()
        launch{
            try {
                issues = HttpClient.getIssues()
            }catch (ex: Exception){
                ex.printStackTrace()
            }
        }
        adapter.update(issues)
    }

    private fun openNewIssue() {
        val message = "stub"
        val intent = Intent(this, NewIssueActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
    }
}
