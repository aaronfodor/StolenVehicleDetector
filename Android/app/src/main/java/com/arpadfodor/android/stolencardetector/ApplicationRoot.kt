package com.arpadfodor.android.stolencardetector

import android.Manifest
import android.app.Application
import android.util.Log
import androidx.preference.PreferenceManager
import com.arpadfodor.android.stolencardetector.model.BoundingBoxDrawer
import com.arpadfodor.android.stolencardetector.model.LocationService
import com.arpadfodor.android.stolencardetector.model.MediaHandler
import com.arpadfodor.android.stolencardetector.model.ai.ObjectDetectionService
import com.arpadfodor.android.stolencardetector.model.ai.StolenVehicleRecognizerService
import com.arpadfodor.android.stolencardetector.model.ai.TextRecognitionService
import com.arpadfodor.android.stolencardetector.model.api.ApiService
import com.arpadfodor.android.stolencardetector.model.db.DatabaseService
import com.arpadfodor.android.stolencardetector.viewmodel.CameraViewModel
import java.util.*

class ApplicationRoot : Application() {

    companion object{

        private const val TAG = "Application Root"
        private const val NUM_THREADS = 4

        const val IMMERSIVE_FLAG_TIMEOUT = 500L

        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)

    }

    /**
     * This method fires only once per application start, getApplicationContext returns null here
     **/
    init {
        Log.i(TAG, "Constructor fired")
    }

    /**
     * This method fires once as well as the constructor, but also application has context here
     **/
    override fun onCreate() {

        super.onCreate()
        Log.i(TAG, "onCreate fired")

        DatabaseService.initialize(applicationContext)
        ApiService.initialize()

        ObjectDetectionService.initialize(assets, NUM_THREADS)
        TextRecognitionService.initialize(applicationContext)

        val radius = resources.getDimension(R.dimen.bounding_box_radius)
        val width = resources.getDimension(R.dimen.bounding_box_line_width)
        val textSize = resources.getDimension(R.dimen.bounding_box_text_size)
        val colors = resources.getStringArray(R.array.bounding_box_colors)
        val alertColor = resources.getColor(R.color.colorAlertBoundingBox, null)
        BoundingBoxDrawer.initialize(radius, width, textSize, colors, alertColor)

        val appName = getString(R.string.app_name)
        MediaHandler.initialize(applicationContext, appName)
        LocationService.initialize(applicationContext)

        CameraViewModel.KEY_EVENT_ACTION = getString(R.string.KEY_EVENT_ACTION)
        CameraViewModel.KEY_EVENT_EXTRA = getString(R.string.KEY_EVENT_EXTRA)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isAutoSyncEnabled = preferences.getBoolean(getString(R.string.SETTINGS_AUTO_SYNC),
            resources.getBoolean(R.bool.settings_auto_sync_default))

        if(isAutoSyncEnabled){
            DatabaseService.updateFromApi{isSuccess ->
                if(isSuccess){
                    val currentTime = Calendar.getInstance().time.toString()
                    preferences.edit().putString(getString(R.string.LAST_SYNCED_DB), currentTime)
                        .apply()
                }
                StolenVehicleRecognizerService.initialize()
            }
        }

    }

}