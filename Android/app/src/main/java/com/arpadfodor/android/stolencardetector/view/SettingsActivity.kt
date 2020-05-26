package com.arpadfodor.android.stolencardetector.view

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.arpadfodor.android.stolencardetector.R
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.custom_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val drawerToggle = ActionBarDrawerToggle(this, settingsActivityDrawerLayout, toolbar, R.string.menu_open, R.string.menu_close)
        settingsActivityDrawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        settings_navigation.setNavigationItemSelectedListener(this)
        val navigationSettingsView = findViewById<NavigationView>(R.id.settings_navigation)
        val header = navigationSettingsView?.getHeaderView(0)

        settings_navigation.bringToFront()
        settings_navigation.parent.requestLayout()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()

    }

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item
     */
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
            }
            else ->{
                return false
            }
        }

        if(settingsActivityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            settingsActivityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        return true

    }

    override fun onBackPressed() {
        if(settingsActivityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            settingsActivityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            this.finish()
        }
    }

}
