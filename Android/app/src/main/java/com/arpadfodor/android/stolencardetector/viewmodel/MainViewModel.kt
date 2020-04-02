package com.arpadfodor.android.stolencardetector.viewmodel

import android.graphics.Bitmap
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.arpadfodor.android.stolencardetector.model.BoundingBoxDrawer
import com.arpadfodor.android.stolencardetector.model.ImageConverter
import com.arpadfodor.android.stolencardetector.model.MediaHandler
import com.arpadfodor.android.stolencardetector.model.ai.ObjectDetectionService
import com.arpadfodor.android.stolencardetector.model.ai.Recognition
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MainViewModel() : ViewModel(){

    companion object{

        private const val MINIMUM_PREDICTION_CERTAINTY_TO_SHOW = 0.5f
        private const val MAXIMUM_RECOGNITIONS_TO_SHOW = 10

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        var deviceOrientation: Int = 0

    }

    private val objectDetectionService = ObjectDetectionService()

    /**
     * Executes recognition
     *
     * @param image                 Bitmap to evaluate
     *
     * @return List<Recognition>    Recognition results in a list
     */
    fun detectImage(image: Bitmap) : List<Recognition>{
        return objectDetectionService.recognizeImage(image,
            MAXIMUM_RECOGNITIONS_TO_SHOW, MINIMUM_PREDICTION_CERTAINTY_TO_SHOW
        )
    }

    fun drawBoundingBoxes(inputBitmap: Bitmap, recognition: List<Recognition>): Bitmap{
        return BoundingBoxDrawer.drawBoundingBoxes(inputBitmap, deviceOrientation, objectDetectionService.getModelInputSize(), recognition)
    }

    fun imageProxyToBitmap(image: ImageProxy, cameraOrientation: Int): Bitmap{
        return ImageConverter.imageProxyToBitmap(image, cameraOrientation, objectDetectionService.getModelInputSize())
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

    fun getOutputDirectory(): File{
        return MediaHandler.getOutputDirectory()
    }

    fun createFile(baseFolder: File) : File{
        return MediaHandler.createFile(baseFolder)
    }

}