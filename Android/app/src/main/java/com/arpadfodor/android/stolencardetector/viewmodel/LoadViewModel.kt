package com.arpadfodor.android.stolencardetector.viewmodel

import android.graphics.Bitmap
import android.util.Size
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.android.stolencardetector.model.ImageConverter
import com.arpadfodor.android.stolencardetector.model.ai.StolenVehicleRecognizerService
import kotlin.math.min

class LoadViewModel : ViewModel(){

    companion object{

        const val GALLERY_REQUEST_CODE = 2

        var numRecognitionsToShow = 10
        var minimumPredictionCertaintyToShow = 0.5f
            set(value) {
                field = value/100f
            }

        var screenDimensions = Size(0, 0)

    }

    private val licensePlateReaderService = StolenVehicleRecognizerService()

    val imageMimeTypes = arrayListOf("image/jpeg", "image/png")

    /**
     * The loaded image to feed
     **/
    val loadedImage: MutableLiveData<Bitmap> by lazy {
        MutableLiveData<Bitmap>()
    }

    /**
     * The bounding box image
     **/
    val boundingBoxImage: MutableLiveData<Bitmap> by lazy {
        MutableLiveData<Bitmap>()
    }

    fun setLoadedImage(bitmap: Bitmap, imageOrientation: Int){

        // remove the bounding boxes of the previous image
        boundingBoxImage.value = null

        Thread(Runnable {

            val rotatedBitmap = ImageConverter.rotateBitmap(bitmap, imageOrientation)
            loadedImage.postValue(rotatedBitmap)

            val smallerScreenDimension = min(screenDimensions.width, screenDimensions.height)
            val requiredOutputImageSize = Size(smallerScreenDimension, smallerScreenDimension)

            val boundingBoxBitmap = licensePlateReaderService.recognize(rotatedBitmap,
                requiredOutputImageSize, 0, numRecognitionsToShow, minimumPredictionCertaintyToShow)

            boundingBoxImage.postValue(boundingBoxBitmap)

        }).start()

    }

    fun setScreenProperties(width: Int, height: Int){
        screenDimensions = Size(width, height)
    }

}