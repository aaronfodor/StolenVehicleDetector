package com.arpadfodor.android.stolencardetector.viewmodel

import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import com.arpadfodor.android.stolencardetector.model.MediaHandler
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraViewModel : ViewModel(){

    companion object{

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        /**
         * An arbitrary number to keep track of the permission request
         * Where an app has multiple context for requesting permission, this can help differentiate the different contexts
         **/
        const val REQUEST_CODE_PERMISSIONS = 1

        var KEY_EVENT_ACTION = ""
        var KEY_EVENT_EXTRA = ""

        var maximumRecognitionsToShow = 10
        var minimumPredictionCertaintyToShow = 0.5f
            set(value) {
                field = value/100f
            }

    }

    var screenDimensions = Size(0, 0)
    var imageRatio = 0f

    var deviceOrientation: Int = 0

    var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    /**
     *  [androidx.camera.core.ImageAnalysisConfig] requires enum value of [androidx.camera.core.AspectRatio].
     *  Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *
     *  @return suitable aspect ratio
     **/
    fun aspectRatio(width: Int, height: Int): Int {

        val previewRatio = max(width, height).toDouble() / min(width, height)

        return if(abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)){
            AspectRatio.RATIO_4_3
        }
        else{
            AspectRatio.RATIO_16_9
        }

    }

    private fun aspectRatioInFloat(width: Int, height: Int) : Float {

        val aspectRatio = aspectRatio(width, height)
        var aspectRatioFloat = 0.0

        when (aspectRatio) {
            AspectRatio.RATIO_4_3 -> {
                aspectRatioFloat = RATIO_4_3_VALUE
            }
            AspectRatio.RATIO_16_9 -> {
                aspectRatioFloat = RATIO_16_9_VALUE
            }
        }

        return aspectRatioFloat.toFloat()

    }

    fun setScreenProperties(width: Int, height: Int){
        screenDimensions = Size(width, height)
        imageRatio = aspectRatioInFloat(width, height)
    }

    fun getOutputDirectory(): File{
        return MediaHandler.getOutputDirectory()
    }

    fun createFile(baseFolder: File) : File{
        return MediaHandler.createFile(baseFolder)
    }

}