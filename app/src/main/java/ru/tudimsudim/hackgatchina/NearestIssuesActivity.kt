package ru.tudimsudim.hackgatchina

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main
import ru.tudimsudim.hackgatchina.model.Issue
import java.util.*
import ru.tudimsudim.hackgatchina.presenter.HttpClient
import java.lang.Exception

@SuppressLint("ByteOrderMark")
class NearestIssuesActivity : AppCompatActivity() {

    private lateinit var geo: GeoMaster
    private lateinit var adapter : IssuesAdapter

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        fab.setOnClickListener { view ->
            openNewIssue();
        }
        createNotificationChannel()
        geo = GatchinaApplication.geoMaster
        geo.init()

        val displayMetrics = DisplayMetrics()
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        recycler_view.layoutManager = LinearLayoutManager(this)
        adapter = IssuesAdapter(width)
        recycler_view.adapter = adapter

    }

    //RUNTIME PERMISSIONS

    private val RECORD_REQUEST_CODE = 101

    private fun setupPermissions() {
        if (GeoMasterHelper.shouldAskPermission(this))
        {
            Log.i("PermissionDemo", "Permission to record denied")
            makeRequest()
        }
    }


    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            RECORD_REQUEST_CODE
        )
    }
/*
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i("IPL", "Permission has been denied by user")
                } else {
                    Log.i("IPL", "Permission has been granted by user")
                }
            }
        }
    }﻿*/

    // === RUNTIME PERMISSION

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        GlobalScope.launch(Dispatchers.Main) {
            var issues = emptyList<Issue>()
            try {
                withContext(Dispatchers.IO) {
                    issues = HttpClient.getIssues()
                }
            }catch (ex: Exception){
                ex.printStackTrace()
            }

            adapter.update(issues)

            try {
                geo.updateGeofence(issues)
            }catch (ex: Exception){
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

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("main_id", "Main", importance).apply {
                description = "Main channel"
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
