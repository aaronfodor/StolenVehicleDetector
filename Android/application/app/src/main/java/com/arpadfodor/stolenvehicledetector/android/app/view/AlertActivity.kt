package com.arpadfodor.stolenvehicledetector.android.app.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Report
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.AlertViewModel
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_alert.*

class AlertActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object{

        /**
         * Use it to pass list parameter to an instance of this activity before starting it.
         * Used because passing custom objects between activities can be problematic via intents.
         **/
        fun setActivityParameter(list: Array<Report>){
            listParam = list
        }

        var listParam = arrayOf<Report>()

    }

    private lateinit var viewModel: AlertViewModel
    private lateinit var adapter: ReportListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)

        viewModel = ViewModelProvider(this).get(AlertViewModel::class.java)

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

        adapter = ReportListAdapter(this, null)
        alert_list.adapter = adapter
        alert_list.layoutManager = LinearLayoutManager(this)

        subscribeToViewModel()
        val alerts = listParam
        viewModel.alerts.postValue(alerts.toList())

    }

    override fun onResume() {
        super.onResume()
        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {

        // Create the observer
        val listObserver = Observer<List<Report>> { list ->
            adapter.submitList(list)
        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.alerts.observe(this, listObserver)

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
