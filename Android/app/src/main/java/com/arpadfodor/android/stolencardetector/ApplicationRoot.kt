package com.arpadfodor.android.stolencardetector

import android.app.Application
import android.util.Log
import com.arpadfodor.android.stolencardetector.model.BoundingBoxDrawer
import com.arpadfodor.android.stolencardetector.model.ai.MobileNetV1Coco
import com.arpadfodor.android.stolencardetector.model.ai.ObjectDetectionService

class ApplicationRoot : Application() {

    companion object{
        private const val TAG = "Application Root"
        private const val NUM_THREADS = 4
        private const val SP_BOUNDING_BOX_TEXT_SIZE = 15
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

        val model = MobileNetV1Coco(assets, NUM_THREADS)
        ObjectDetectionService.initialize(model)

        val radius = resources.getDimension(R.dimen.bounding_box_radius)
        val width = resources.getDimension(R.dimen.bounding_box_line_width)
        val textSize = resources.getDimension(R.dimen.bounding_box_text_size)
        val colors = resources.getStringArray(R.array.bounding_box_colors)

        BoundingBoxDrawer.initialize(radius, width, textSize, colors)

    }

    override fun onTerminate() {
        ObjectDetectionService.close()
        super.onTerminate()
    }

}