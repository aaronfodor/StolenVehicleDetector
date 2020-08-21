package com.arpadfodor.stolenvehicledetector.android.app.view

import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppActivity
import com.google.android.material.navigation.NavigationView

class SettingsActivity : AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        val drawer = findViewById<DrawerLayout>(R.id.settingsActivityDrawerLayout)
        val navigation = findViewById<NavigationView>(R.id.settings_navigation)
        initUi(drawer, navigation)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()

    }

    override fun subscribeToViewModel(){}
    override fun subscribeListeners(){}
    override fun unsubscribe(){}

    override fun onBackPressed() {
        if(activityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            activityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            this.finish()
        }
    }

}
