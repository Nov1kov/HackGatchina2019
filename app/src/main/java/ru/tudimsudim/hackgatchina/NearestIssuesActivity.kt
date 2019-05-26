package ru.tudimsudim.hackgatchina

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.DisplayMetrics
import android.util.Log
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main
import ru.tudimsudim.hackgatchina.presenter.HttpClient
import android.content.DialogInterface
import android.support.v7.app.AlertDialog


@SuppressLint("ByteOrderMark")
class NearestIssuesActivity : AppCompatActivity(), IssueItemClick {

    private lateinit var geo: GeoMaster
    private lateinit var adapter: IssuesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        fab.setOnClickListener { view ->
            openDialog()
        }

        swipe_container.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent,
            R.color.VoteColor
        )

        swipe_container.setOnRefreshListener {
            updateIssues()
        }

        geo = GatchinaApplication.geoMaster

        val displayMetrics = DisplayMetrics()
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        recycler_view.layoutManager = LinearLayoutManager(this)
        adapter = IssuesAdapter(width, this)
        recycler_view.adapter = adapter

        setupPermissions()
    }

    override fun onClick(index: Int) {
        val issue_id = adapter.issues[index].id
        val intent = Intent(this, IssueActivity::class.java)
        intent.putExtra(IssueActivity.ID_ISSUE_KEY, issue_id)
        startActivity(intent)
    }

    //RUNTIME PERMISSIONS

    private val RECORD_REQUEST_CODE = 101

    private fun setupPermissions() {
        if (GeoMasterHelper.shouldAskPermission(this)) {
            Log.i("PermissionDemo", "Permission to record denied")
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            RECORD_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == RECORD_REQUEST_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.i("IPL", "Permission has been denied by user")
            } else {
                geo.init()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

// === RUNTIME PERMISSION

    override fun onResume() {
        super.onResume()
        updateIssues()
    }

    private fun updateIssues() {
        swipe_container.isRefreshing = true
        GlobalScope.launch(Dispatchers.Main) {
            delay(200)
            val coors = GatchinaApplication.geoMaster.getCoordinates()
            try {
                withContext(Dispatchers.IO) {
                    GatchinaApplication.data.issues = HttpClient.getIssues(coors)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            adapter.update(GatchinaApplication.data.issues)

            try {
                geo.updateGeofence(GatchinaApplication.data.issues)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            swipe_container.isRefreshing = false
        }
    }

    private fun openDialog(){
        val builder1 = AlertDialog.Builder(this)
        builder1.setTitle(getString(R.string.what_about_issue))
        val items = listOf(getString(R.string.about_good), getString(R.string.about_bad))
        builder1.setItems(items.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
            openNewIssue(which == 0)
        })
        builder1.show()
    }

    private fun openNewIssue(isPositive: Boolean) {
        val intent = Intent(this, NewIssueActivity::class.java).apply {
            putExtra("POSITIVE_KEY", isPositive)
        }
        startActivity(intent)
    }
}
