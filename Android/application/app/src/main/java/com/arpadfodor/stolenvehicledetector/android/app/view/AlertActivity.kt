package com.arpadfodor.stolenvehicledetector.android.app.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.AlertListAdapter
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_alert.*

class AlertActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var adapter: AlertListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)

        val toolbar = findViewById<Toolbar>(R.id.custom_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val drawerToggle = ActionBarDrawerToggle(this, alertActivityDrawerLayout, toolbar, R.string.menu_open, R.string.menu_close)
        alertActivityDrawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        alert_navigation.setNavigationItemSelectedListener(this)
        val navigationMenuView = findViewById<NavigationView>(R.id.alert_navigation)
        val header = navigationMenuView?.getHeaderView(0)

        alert_navigation.bringToFront()
        alert_navigation.parent.requestLayout()

        val suspiciousIds = intent.getSerializableExtra("suspicious_ids") as Array<String>
        val date = intent.getStringExtra("date") ?: ""
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        val locationString = "lat=${latitude} long=${longitude}"

        adapter = AlertListAdapter(this, suspiciousIds, date, locationString)
        alert_recycleview.adapter = adapter
        alert_recycleview.layoutManager = LinearLayoutManager(this)

    }

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item
     **/
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.navigation_live -> {
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)
            }
            R.id.navigation_load -> {
                val intent = Intent(this, LoadActivity::class.java)
                startActivity(intent)
            }
            R.id.navigation_gallery -> {
            }
            R.id.navigation_reports -> {
            }
            R.id.navigation_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            else ->{
                return false
            }
        }

        if(alertActivityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            alertActivityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        return true

    }

    override fun onBackPressed() {

        if(alertActivityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            alertActivityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            this.finish()
        }

    }

}
