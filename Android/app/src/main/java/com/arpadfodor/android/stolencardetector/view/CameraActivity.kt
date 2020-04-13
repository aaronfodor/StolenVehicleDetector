package com.arpadfodor.android.stolencardetector.view

import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.OrientationEventListener
import android.view.View.*
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.arpadfodor.android.stolencardetector.ApplicationRoot
import com.arpadfodor.android.stolencardetector.R
import com.arpadfodor.android.stolencardetector.view.utils.AppDialog
import com.arpadfodor.android.stolencardetector.viewmodel.CameraViewModel
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var viewModel: CameraViewModel

    private lateinit var container: FrameLayout

    lateinit var deviceOrientationListener: OrientationEventListener

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        container = findViewById(R.id.camera_container)

        viewModel = ViewModelProviders.of(this).get(CameraViewModel::class.java)

        deviceOrientationListener = object : OrientationEventListener(this,
            SensorManager.SENSOR_DELAY_NORMAL) {

            override fun onOrientationChanged(orientation: Int) {
                CameraViewModel.deviceOrientation = orientation
            }

        }

        val toolbar = findViewById<Toolbar>(R.id.camera_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val drawerToggle = ActionBarDrawerToggle(this, mainActivityDrawerLayout, toolbar, R.string.menu_open, R.string.menu_close)
        mainActivityDrawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        camera_navigation.setNavigationItemSelectedListener(this)
        val navigationMenuView = findViewById<NavigationView>(R.id.camera_navigation)
        val header = navigationMenuView?.getHeaderView(0)

        camera_navigation.bringToFront()
        camera_navigation.parent.requestLayout()

        // Request camera permissions
        if (allPermissionsGranted()) {
            activateCamera()
        }
        else{
            requestPermission()
        }

    }

    private fun activateCamera(){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.camera_container, CameraFragment())
            .commit()
    }

    override fun onResume() {

        super.onResume()
        /**
         * Before hiding the status bar, a wait is needed to let the UI settle.
         * Trying to set app to immersive mode before it's ready causes the flags not sticking.
         */
        container.postDelayed({
            container.systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN and SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }, ApplicationRoot.IMMERSIVE_FLAG_TIMEOUT)

        deviceOrientationListener.enable()

        // read settings from preferences
        val settings = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val settingsMaximumRecognitions = getString(R.string.SETTINGS_MAXIMUM_RECOGNITIONS)
        val settingsMinimumPredictionCertainty = getString(R.string.SETTINGS_MINIMUM_PREDICTION_CERTAINTY)
        val maximumRecognitionsToShow = settings.getInt(settingsMaximumRecognitions, resources.getInteger(R.integer.settings_maximum_recognitions_default))
        val minimumPredictionCertaintyToShow = settings.getInt(settingsMinimumPredictionCertainty, resources.getInteger(R.integer.settings_minimum_prediction_certainty_default))
        CameraViewModel.maximumRecognitionsToShow = maximumRecognitionsToShow
        CameraViewModel.minimumPredictionCertaintyToShow = minimumPredictionCertaintyToShow.toFloat()

    }

    override fun onPause() {
        deviceOrientationListener.disable()
        super.onPause()
    }

    /**
     * When key down event is triggered, relay it via local broadcast so fragments can handle it
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        return when (keyCode){

            KeyEvent.KEYCODE_VOLUME_DOWN -> {

                val intent = Intent(CameraViewModel.KEY_EVENT_ACTION).apply {
                    putExtra(CameraViewModel.KEY_EVENT_EXTRA, keyCode)
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                true

            }
            else -> {
                super.onKeyDown(keyCode, event)
            }

        }

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

        if(mainActivityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mainActivityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        return true

    }

    override fun onBackPressed() {

        if(mainActivityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mainActivityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            exitDialog()
        }

    }

    /**
     * Asks for exit confirmation
     */
    private fun exitDialog(){

        val exitDialog = AppDialog(this, getString(R.string.exit_title), getString(R.string.exit_dialog), resources.getDrawable(R.drawable.warning))
        exitDialog.setPositiveButton {
            //showing the home screen - app is not visible but running
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
        exitDialog.show()

    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = ApplicationRoot.requiredPermissions.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, ApplicationRoot.requiredPermissions, CameraViewModel.REQUEST_CODE_PERMISSIONS)
    }

}
