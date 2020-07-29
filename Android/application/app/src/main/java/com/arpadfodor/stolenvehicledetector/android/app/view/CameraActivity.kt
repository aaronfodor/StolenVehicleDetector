package com.arpadfodor.stolenvehicledetector.android.app.view

import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.OrientationEventListener
import android.view.View.*
import android.widget.FrameLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.arpadfodor.stolenvehicledetector.android.app.ApplicationRoot
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppActivity
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppSnackBarBuilder
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.CameraViewModel
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class CameraActivity : AppActivity() {

    private lateinit var viewModel: CameraViewModel
    private lateinit var container: FrameLayout
    lateinit var deviceOrientationListener: OrientationEventListener

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_camera)
        container = findViewById(R.id.camera_container)
        val drawer = findViewById<DrawerLayout>(R.id.cameraActivityDrawerLayout)
        val navigation = findViewById<NavigationView>(R.id.camera_navigation)
        initUi(drawer, navigation)

        viewModel = ViewModelProvider(this).get(CameraViewModel::class.java)

        activatePermissionFragment()

        deviceOrientationListener = object : OrientationEventListener(this,
            SensorManager.SENSOR_DELAY_NORMAL) {

            override fun onOrientationChanged(orientation: Int) {
                CameraViewModel.deviceOrientation = orientation
            }

        }

    }

    override fun subscribeToViewModel() {

        // read settings from preferences
        val settings = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        val settingsNumRecognitionsKey = getString(R.string.SETTINGS_NUM_RECOGNITIONS)
        val settingsMinimumPredictionCertaintyKey = getString(R.string.SETTINGS_MINIMUM_PREDICTION_CERTAINTY)
        val settingsShowReceptiveFieldKey = getString(R.string.SETTINGS_SHOW_RECEPTIVE_FIELD)

        val numRecognitionsToShow = settings.getInt(settingsNumRecognitionsKey, resources.getInteger(R.integer.settings_num_recognitions_default))
        val minimumPredictionCertaintyToShow = settings.getInt(settingsMinimumPredictionCertaintyKey, resources.getInteger(R.integer.settings_minimum_prediction_certainty_default))
        val settingsShowReceptiveField = settings.getBoolean(settingsShowReceptiveFieldKey, resources.getBoolean(R.bool.settings_receptive_field_default))

        CameraViewModel.numRecognitionsToShow = numRecognitionsToShow
        CameraViewModel.minimumPredictionCertaintyToShow = minimumPredictionCertaintyToShow.toFloat()
        CameraViewModel.settingsShowReceptiveField = settingsShowReceptiveField

        // Create the observer which updates the UI in case of value change
        val hasPermissionsGranted = Observer<Boolean> { permissionGranted ->

            if(permissionGranted == true){
                //AppSnackBarBuilder.buildSuccessSnackBar(resources, container, getString(R.string.permission_granted), Snackbar.LENGTH_SHORT).show()
            }
            else{
                AppSnackBarBuilder.buildAlertSnackBar(this.applicationContext, container, getString(R.string.permission_denied), Snackbar.LENGTH_SHORT).show()
            }

        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.hasPermissionsGranted.observe(this, hasPermissionsGranted)

    }

    override fun subscribeListeners() {

        deviceOrientationListener.enable()

        /**
         * Before hiding the status bar, a wait is needed to let the UI settle.
         * Trying to set app to immersive mode before it's ready causes the flags not sticking.
         */
        container.postDelayed({
            container.systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN and SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }, ApplicationRoot.IMMERSIVE_FLAG_TIMEOUT)

    }

    override fun unsubscribeListeners() {
        deviceOrientationListener.disable()
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

}
