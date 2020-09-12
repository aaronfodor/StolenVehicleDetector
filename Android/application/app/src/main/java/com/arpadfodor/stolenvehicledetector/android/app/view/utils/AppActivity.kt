package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.arpadfodor.stolenvehicledetector.android.app.ApplicationRoot
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_account.*

abstract class AppActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var activityDrawerLayout: DrawerLayout
    lateinit var navigation: NavigationView

    fun initUi(activityDrawerLayout: DrawerLayout, navigation: NavigationView){

        this.activityDrawerLayout = activityDrawerLayout
        this.navigation = navigation

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val toolbar = findViewById<Toolbar>(R.id.custom_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val drawerToggle = ActionBarDrawerToggle(this, activityDrawerLayout, toolbar, R.string.menu_open, R.string.menu_close)
        activityDrawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navigation.setNavigationItemSelectedListener(this)
        navigation.bringToFront()
        navigation.parent.requestLayout()
        val header = navigation.getHeaderView(0)

    }

    override fun onResume() {
        super.onResume()
        subscribeToViewModel()
        subscribeListeners()
        setKeepScreenOnFlag()
        appearingAnimations()
    }

    override fun onPause() {
        unsubscribe()
        super.onPause()
    }

    abstract fun appearingAnimations()
    abstract fun subscribeToViewModel()
    abstract fun subscribeListeners()
    abstract fun unsubscribe()

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item
     **/
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.navigation_live -> {
                val toStartActivity = CameraActivity::class.java
                if(toStartActivity == this::class.java){
                    return false
                }
                val intent = Intent(this, toStartActivity)
                startActivity(intent)
            }

            R.id.navigation_load -> {
                val toStartActivity = LoadActivity::class.java
                if(toStartActivity == this::class.java){
                    return false
                }
                val intent = Intent(this, toStartActivity)
                startActivity(intent)
            }

            R.id.navigation_reports -> {
            }

            R.id.navigation_user_reports -> {
                val toStartActivity = UserRecognitionActivity::class.java
                if(toStartActivity == this::class.java){
                    return false
                }
                val intent = Intent(this, toStartActivity)
                startActivity(intent)
            }

            R.id.navigation_settings -> {
                val toStartActivity = SettingsActivity::class.java
                if(toStartActivity == this::class.java){
                    return false
                }
                val intent = Intent(this, toStartActivity)
                startActivity(intent)
            }

            R.id.navigation_account -> {
                val toStartActivity = AccountActivity::class.java
                if(toStartActivity == this::class.java){
                    return false
                }
                val intent = Intent(this, toStartActivity)
                intent.putExtra("show account manage", true)
                startActivity(intent)
            }

            R.id.navigation_how_to_use -> {
                val toStartActivity = HowToUseActivity::class.java
                if(toStartActivity == this::class.java){
                    return false
                }
                val intent = Intent(this, toStartActivity)
                startActivity(intent)
            }

            R.id.navigation_about -> {
                val toStartActivity = AboutActivity::class.java
                if(toStartActivity == this::class.java){
                    return false
                }
                val intent = Intent(this, toStartActivity)
                startActivity(intent)
            }

            else ->{
                return false
            }

        }

        if(activityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            activityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        return true

    }

    override fun onBackPressed() {
        if(activityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            activityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            exitDialog()
        }
    }

    /**
     * Asks for exit confirmation
     **/
    private fun exitDialog(){

        val exitDialog = AppDialog(this, getString(R.string.exit_title),
            getString(R.string.exit_dialog), R.drawable.warning)
        exitDialog.setPositiveButton {
            //showing the home screen - app is not visible but running
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
        exitDialog.show()

    }

    private fun setKeepScreenOnFlag(){
        if(ApplicationRoot.keepScreenAlive){
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        else{
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    open fun showPermissionFragment(actionFinished: () -> Unit){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.account_container, PermissionsFragment(actionFinished))
            .commit()
    }

    fun showMissingPermissionNotification(){
        AppSnackBarBuilder.buildInfoSnackBar(this.applicationContext, start_activity_layout,
            getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show()
    }

}
