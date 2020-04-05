package com.arpadfodor.android.stolencardetector

import android.app.Application
import android.util.Log
import com.arpadfodor.android.stolencardetector.model.BoundingBoxDrawer
import com.arpadfodor.android.stolencardetector.model.MediaHandler
import com.arpadfodor.android.stolencardetector.model.ai.ObjectDetectionService
import com.arpadfodor.android.stolencardetector.viewmodel.MainViewModel

class ApplicationRoot : Application() {

    companion object{
        private const val TAG = "Application Root"
        private const val NUM_THREADS = 4
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

        ObjectDetectionService.initialize(assets, NUM_THREADS)

        val radius = resources.getDimension(R.dimen.bounding_box_radius)
        val width = resources.getDimension(R.dimen.bounding_box_line_width)
        val textSize = resources.getDimension(R.dimen.bounding_box_text_size)
        val colors = resources.getStringArray(R.array.bounding_box_colors)

        BoundingBoxDrawer.initialize(radius, width, textSize, colors)

        val appName = getString(R.string.app_name)

        MediaHandler.initialize(applicationContext, appName)

        MainViewModel.KEY_EVENT_ACTION = getString(R.string.KEY_EVENT_ACTION)
        MainViewModel.KEY_EVENT_EXTRA = getString(R.string.KEY_EVENT_EXTRA)

    }

    override fun onTerminate() {
        ObjectDetectionService.close()
        super.onTerminate()
    }

}