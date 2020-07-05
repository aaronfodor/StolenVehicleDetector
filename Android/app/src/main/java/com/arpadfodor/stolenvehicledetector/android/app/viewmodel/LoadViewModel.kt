package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import android.graphics.Bitmap
import android.util.Size
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.stolenvehicledetector.android.app.model.ImageConverter
import com.arpadfodor.stolenvehicledetector.android.app.model.ai.StolenVehicleRecognizerService
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
     * Metadata of the loaded image
     * [0]: date of image taking
     * [1]: latitude of the photo origin
     * [2]: longitude of the photo origin
     **/
    val imageMetaData: MutableLiveData<Array<String>> by lazy {
        MutableLiveData<Array<String>>()
    }

    /**
     * The bounding box image
     **/
    val boundingBoxImage: MutableLiveData<Bitmap> by lazy {
        MutableLiveData<Bitmap>()
    }

    /**
     * List of suspicious element Ids from the last inference
     **/
    val suspiciousElementIds: MutableLiveData<Array<String>> by lazy {
        MutableLiveData<Array<String>>()
    }

    fun setLoadedImage(bitmap: Bitmap, imageOrientation: Int, imageMetaData_: Array<String>){

        // set image metadata
        imageMetaData.value = imageMetaData_

        // remove the bounding boxes of the previous image
        boundingBoxImage.value = null

        Thread(Runnable {

            val rotatedBitmap = ImageConverter.rotateBitmap(bitmap, imageOrientation)
            loadedImage.postValue(rotatedBitmap)

            val smallerScreenDimension = min(screenDimensions.width, screenDimensions.height)
            val requiredOutputImageSize = Size(smallerScreenDimension, smallerScreenDimension)

            val boundingBoxBitmap = licensePlateReaderService.recognize(rotatedBitmap,
                requiredOutputImageSize, 0, numRecognitionsToShow, minimumPredictionCertaintyToShow) {suspiciousIds ->
                suspiciousElementIds.postValue(suspiciousIds)
            }

            boundingBoxImage.postValue(boundingBoxBitmap)

        }).start()

    }

    fun rotateImage(){

        // remove the bounding boxes of the previous image
        boundingBoxImage.value = null

        val image = loadedImage.value ?: return

        Thread(Runnable {

            val rotatedBitmap = ImageConverter.rotateBitmap(image, 90)
            loadedImage.postValue(rotatedBitmap)

            val smallerScreenDimension = min(screenDimensions.width, screenDimensions.height)
            val requiredOutputImageSize = Size(smallerScreenDimension, smallerScreenDimension)

            val boundingBoxBitmap = licensePlateReaderService.recognize(rotatedBitmap,
                requiredOutputImageSize, 0, numRecognitionsToShow, minimumPredictionCertaintyToShow) {suspiciousIds ->
                suspiciousElementIds.postValue(suspiciousIds)
            }

            boundingBoxImage.postValue(boundingBoxBitmap)

        }).start()

    }

    fun setScreenProperties(width: Int, height: Int){
        screenDimensions = Size(width, height)
    }

}