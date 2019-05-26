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

@SuppressLint("ByteOrderMark")
class NearestIssuesActivity : AppCompatActivity(), IssueItemClick {

    private lateinit var geo: GeoMaster
    private lateinit var adapter: IssuesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        fab.setOnClickListener { view ->
            openNewIssue();
        }
        geo = GatchinaApplication.geoMaster
        geo.init()

        val displayMetrics = DisplayMetrics()
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
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
/*

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == RECORD_REQUEST_CODE) {

            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                Log.i("IPL", "Permission has been denied by user")
            } else {
                Log.i("IPL", "Permission has been granted by user")
            }
        }
    }﻿
*/

    // === RUNTIME PERMISSION

    override fun onResume() {
        super.onResume()
        val coors = GatchinaApplication.geoMaster.getCoordinates()
        GlobalScope.launch(Dispatchers.Main) {
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
        }
    }

    private fun openNewIssue() {
        val message = "stub"
        val intent = Intent(this, NewIssueActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
    }
}
