package com.arpadfodor.android.stolencardetector.view

import android.app.ActivityOptions
import android.content.Intent
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
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.arpadfodor.android.stolencardetector.ApplicationRoot
import com.arpadfodor.android.stolencardetector.R
import com.arpadfodor.android.stolencardetector.view.utils.AppDialog
import com.arpadfodor.android.stolencardetector.view.utils.AppSnackBarBuilder
import com.arpadfodor.android.stolencardetector.viewmodel.CameraViewModel
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var viewModel: CameraViewModel

    private lateinit var container: FrameLayout

    lateinit var deviceOrientationListener: OrientationEventListener

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        container = findViewById(R.id.camera_container)

        viewModel = ViewModelProvider(this).get(CameraViewModel::class.java)

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

        activatePermissionFragment()

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

        val settingsMaximumRecognitionsKey = getString(R.string.SETTINGS_MAXIMUM_RECOGNITIONS)
        val settingsMinimumPredictionCertaintyKey = getString(R.string.SETTINGS_MINIMUM_PREDICTION_CERTAINTY)
        val settingsShowReceptiveFieldKey = getString(R.string.SETTINGS_SHOW_RECEPTIVE_FIELD)

        val maximumRecognitionsToShow = settings.getInt(settingsMaximumRecognitionsKey, resources.getInteger(R.integer.settings_maximum_recognitions_default))
        val minimumPredictionCertaintyToShow = settings.getInt(settingsMinimumPredictionCertaintyKey, resources.getInteger(R.integer.settings_minimum_prediction_certainty_default))
        val settingsShowReceptiveField = settings.getBoolean(settingsShowReceptiveFieldKey, resources.getBoolean(R.bool.settings_receptive_field_default))

        CameraViewModel.maximumRecognitionsToShow = maximumRecognitionsToShow
        CameraViewModel.minimumPredictionCertaintyToShow = minimumPredictionCertaintyToShow.toFloat()
        CameraViewModel.settingsShowReceptiveField = settingsShowReceptiveField

        subscribeToViewModel()

    }

    private fun subscribeToViewModel() {

        // Create the observer which updates the UI in case of value change
        val hasPermissionsGranted = Observer<Boolean> { permissionGranted ->

            if(permissionGranted == true){
                AppSnackBarBuilder.buildSuccessSnackBar(resources, container, getString(R.string.permission_granted), Snackbar.LENGTH_SHORT).show()
            }
            else{
                AppSnackBarBuilder.buildAlertSnackBar(resources, container, getString(R.string.permission_denied), Snackbar.LENGTH_SHORT).show()
            }

        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.hasPermissionsGranted.observe(this, hasPermissionsGranted)

    }

    override fun onPause() {
        deviceOrientationListener.disable()
        super.onPause()
    }

    private fun activatePermissionFragment(){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.camera_container, PermissionsFragment())
            .commit()
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

}
