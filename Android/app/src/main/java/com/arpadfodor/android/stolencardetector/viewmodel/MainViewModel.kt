package com.arpadfodor.android.stolencardetector.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.arpadfodor.android.stolencardetector.R
import com.arpadfodor.android.stolencardetector.model.BoundingBoxDrawer
import com.arpadfodor.android.stolencardetector.model.ImageConverter
import com.arpadfodor.android.stolencardetector.model.ai.MobileNetV1Coco
import com.arpadfodor.android.stolencardetector.model.ai.ObjectDetector
import com.arpadfodor.android.stolencardetector.model.ai.Recognition
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MainViewModel(application: Application) : AndroidViewModel(application){

    companion object{

        private const val MINIMUM_PREDICTION_CERTAINTY = 0.3f
        private const val MAXIMUM_RECOGNITIONS_TO_SHOW = 10
        private const val NUM_THREADS = 4

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"

        var deviceOrientation: Int = 0

    }

    var app: Application = application

    /*
     * The model
     */
    private val model: ObjectDetector

    init {

        model = MobileNetV1Coco(app.assets, NUM_THREADS)

        val radius = app.resources.getDimension(R.dimen.bounding_box_radius)
        val width = app.resources.getDimension(R.dimen.bounding_box_line_width)
        val colors = app.resources.getStringArray(R.array.bounding_box_colors)

        BoundingBoxDrawer.initialize(radius, width, colors)

    }

    /**
     * Executes recognition
     *
     * @param image                 Bitmap to evaluate
     *
     * @return List<Recognition>    Recognition results in a list
     */
    fun detectImage(image: Bitmap) : List<Recognition>{
        val results = model.recognizeImage(image,
            MAXIMUM_RECOGNITIONS_TO_SHOW, MINIMUM_PREDICTION_CERTAINTY)
        return results
    }

    /**
     *  [androidx.camera.core.ImageAnalysisConfig] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *
     *  @return suitable aspect ratio
     */
    fun aspectRatio(width: Int, height: Int): Int {

        val previewRatio = max(width, height).toDouble() / min(width, height)

        return if(abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)){
            AspectRatio.RATIO_4_3
        }
        else{
            AspectRatio.RATIO_16_9
        }

    }

    /**
     * Creates a timestamped file
     * */
    fun createFile(baseFolder: File) : File{
        return File(baseFolder, SimpleDateFormat(FILENAME, Locale.US).format(System.currentTimeMillis()) + PHOTO_EXTENSION)
    }

    /**
     * Use external media if available, otherwise the app's file directory
     */
    fun getOutputDirectory(): File {

        val appContext = app.applicationContext.applicationContext
        val mediaDir = app.applicationContext.externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return if(mediaDir != null && mediaDir.exists()){
            mediaDir
        }
        else{
            appContext.filesDir
        }

    }

    fun getModelInputSize(): Size {
        return Size(model.IMAGE_SIZE_X, model.IMAGE_SIZE_Y)
    }

    fun drawBoundingBoxes(viewFinderBitmap: Bitmap, recognition: List<Recognition>): Bitmap{
        return BoundingBoxDrawer.drawBoundingBoxes(viewFinderBitmap, deviceOrientation, getModelInputSize(), recognition)
    }

    fun closeResources(){
        model.close()
    }

}