package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.stolenvehicledetector.android.app.model.MediaHandler
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Recognition
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraViewModel : ViewModel(){

    companion object{

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        var KEY_EVENT_ACTION = ""
        var KEY_EVENT_EXTRA = ""

        var numRecognitionsToShow = 10
        var minimumPredictionCertaintyToShow = 0.5f
            set(value) {
                field = value/100f
            }
        var settingsShowReceptiveField = true

        var deviceOrientation: Int = 0
            // clustered device orientations - value can be 0, 90, 180, 270
            get() {
                var roundedOrientation = 0

                if(315 < field || field <= 45){
                    roundedOrientation = 0
                }
                else if(field in 46..135){
                    roundedOrientation = 90
                }
                else if(field in 136..225){
                    roundedOrientation = 180
                }
                else if(field in 226..315){
                    roundedOrientation = 270
                }

                return roundedOrientation
            }

        var screenDimensions = Size(0, 0)

    }

    var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    /**
     * Whether the permissions has been granted
     **/
    val hasPermissionsGranted: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    /**
     * List of recognitions from the last inference
     **/
    val recognitions: MutableLiveData<Array<Recognition>> by lazy {
        MutableLiveData<Array<Recognition>>()
    }

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

    fun setScreenProperties(width: Int, height: Int){
        screenDimensions = Size(width, height)
    }

    fun createFile(): File{
        return MediaHandler.createTimestampedImageFile(MediaHandler.getPublicDirectory())
    }

    fun setAlertActivityParams(){
        AlertViewModel.setParameter(recognitions.value?.toList() ?: listOf())
    }

}