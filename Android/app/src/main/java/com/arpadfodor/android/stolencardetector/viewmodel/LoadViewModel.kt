package com.arpadfodor.android.stolencardetector.viewmodel

import android.graphics.Bitmap
import android.util.Size
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.android.stolencardetector.model.BoundingBoxDrawer
import com.arpadfodor.android.stolencardetector.model.ImageConverter
import com.arpadfodor.android.stolencardetector.model.ai.ObjectDetectionService
import kotlin.math.min

class LoadViewModel : ViewModel(){

    companion object{

        const val GALLERY_REQUEST_CODE = 2

        var maximumRecognitionsToShow = 10
        var minimumPredictionCertaintyToShow = 0.5f
            set(value) {
                field = value/100f
            }

        var screenDimensions = Size(0, 0)

    }

    private val objectDetectionService = ObjectDetectionService()

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

            val bitmapNxN = ImageConverter.bitmapToCroppedNxNImage(bitmap)
            val rotatedBitmap = ImageConverter.rotateBitmap(bitmapNxN, imageOrientation)
            loadedImage.postValue(rotatedBitmap)

            val requiredInputImage = ImageConverter.resizeBitmap(rotatedBitmap, objectDetectionService.getModelInputSize())

            val recognitions = objectDetectionService.recognizeImage(requiredInputImage,
                maximumRecognitionsToShow, minimumPredictionCertaintyToShow)

            val smallerScreenDimension = min(screenDimensions.width, screenDimensions.height)
            val optimalBoundingBoxImageSize = ImageConverter.resizeBitmap(rotatedBitmap, Size(smallerScreenDimension, smallerScreenDimension))

            val boundingBoxBitmap = BoundingBoxDrawer.drawBoundingBoxes(optimalBoundingBoxImageSize,
                0, objectDetectionService.getModelInputSize(), recognitions)

            boundingBoxImage.postValue(boundingBoxBitmap)

        }).start()

    }

    fun setScreenProperties(width: Int, height: Int){
        screenDimensions = Size(width, height)
    }

}