package com.arpadfodor.stolenvehicledetector.android.app.view

import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppActivity
import com.google.android.material.navigation.NavigationView

class HowToUseActivity : AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_how_to_use)
        val drawer = findViewById<DrawerLayout>(R.id.howToUseActivityDrawerLayout)
        val navigation = findViewById<NavigationView>(R.id.how_to_use_navigation)
        initUi(drawer, navigation)

    }

    override fun subscribeToViewModel(){}
    override fun subscribeListeners(){}
    override fun unsubscribeListeners(){}

    override fun onBackPressed() {
        if(activityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            activityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            this.finish()
        }
    }

}
