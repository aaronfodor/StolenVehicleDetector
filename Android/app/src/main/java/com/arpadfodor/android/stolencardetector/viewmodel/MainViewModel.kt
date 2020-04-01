package com.arpadfodor.android.stolencardetector.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import com.arpadfodor.android.stolencardetector.R
import com.arpadfodor.android.stolencardetector.model.BoundingBoxDrawer
import com.arpadfodor.android.stolencardetector.model.ImageConverter
import com.arpadfodor.android.stolencardetector.model.ai.ObjectDetectionService
import com.arpadfodor.android.stolencardetector.model.ai.Recognition
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MainViewModel(application: Application) : AndroidViewModel(application){

    companion object{

        private const val MINIMUM_PREDICTION_CERTAINTY = 0.5f
        private const val MAXIMUM_RECOGNITIONS_TO_SHOW = 10

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"

        var deviceOrientation: Int = 0

    }

    var app: Application = application

    /**
     * Executes recognition
     *
     * @param image                 Bitmap to evaluate
     *
     * @return List<Recognition>    Recognition results in a list
     */
    fun detectImage(image: Bitmap) : List<Recognition>{
        return ObjectDetectionService.recognizeImage(image,
            MAXIMUM_RECOGNITIONS_TO_SHOW, MINIMUM_PREDICTION_CERTAINTY
        )
    }

    fun drawBoundingBoxes(inputBitmap: Bitmap, recognition: List<Recognition>): Bitmap{
        return BoundingBoxDrawer.drawBoundingBoxes(inputBitmap, deviceOrientation, ObjectDetectionService.getModelInputSize(), recognition)
    }

    fun imageProxyToBitmap(image: ImageProxy, cameraOrientation: Int): Bitmap{
        return ImageConverter.imageProxyToBitmap(image, cameraOrientation, ObjectDetectionService.getModelInputSize())
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

}