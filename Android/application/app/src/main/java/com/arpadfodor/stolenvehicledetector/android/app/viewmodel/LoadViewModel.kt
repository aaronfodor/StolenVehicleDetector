package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.stolenvehicledetector.android.app.model.ImageConverter
import com.arpadfodor.stolenvehicledetector.android.app.model.ai.StolenVehicleRecognizerService
import com.arpadfodor.stolenvehicledetector.android.app.model.MediaHandler
import com.arpadfodor.stolenvehicledetector.android.app.model.MetaProvider
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Recognition
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
     * List of recognitions from the last inference
     **/
    val recognitions: MutableLiveData<Array<Recognition>> by lazy {
        MutableLiveData<Array<Recognition>>()
    }

    fun loadImage(selectedImageUri: Uri){

        Thread(Runnable {

            val sourceBitmap = MediaHandler.getImage(selectedImageUri)
            val imageOrientation = MediaHandler.getPhotoOrientation(selectedImageUri)
            val imageMeta = MetaProvider.getImageMetaData(selectedImageUri)

            // remove the bounding boxes of the previous image
            boundingBoxImage.postValue(null)

            val rotatedBitmap = ImageConverter.rotateBitmap(sourceBitmap, imageOrientation)
            loadedImage.postValue(rotatedBitmap)
            imageMetaData.postValue(imageMeta)

            val smallerScreenDimension = min(screenDimensions.width, screenDimensions.height)
            val requiredOutputImageSize = Size(smallerScreenDimension, smallerScreenDimension)

            val boundingBoxBitmap = licensePlateReaderService.recognize(rotatedBitmap,
                requiredOutputImageSize, 0,
                numRecognitionsToShow, minimumPredictionCertaintyToShow) {arrayOfIdImagePairs ->

                val recognitions = arrayListOf<Recognition>()
                var i = 1
                for(pair in arrayOfIdImagePairs){
                    recognitions.add(
                        Recognition(i, pair.first, pair.second,
                            imageMeta[0], imageMeta[1], imageMeta[2])
                    )
                    i++
                }
                this.recognitions.postValue(recognitions.toTypedArray())
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
            val imageMeta = imageMetaData.value ?: arrayOf("", "", "")

            val smallerScreenDimension = min(screenDimensions.width, screenDimensions.height)
            val requiredOutputImageSize = Size(smallerScreenDimension, smallerScreenDimension)

            val boundingBoxBitmap = licensePlateReaderService.recognize(rotatedBitmap,
                requiredOutputImageSize, 0,
                numRecognitionsToShow, minimumPredictionCertaintyToShow) {arrayOfIdImagePairs ->

                val recognitions = arrayListOf<Recognition>()
                var i = 1
                for(pair in arrayOfIdImagePairs){
                    recognitions.add(
                        Recognition(i, pair.first, pair.second,
                            imageMeta[0], imageMeta[1], imageMeta[2])
                    )
                    i++
                }
                this.recognitions.postValue(recognitions.toTypedArray())

            }

            boundingBoxImage.postValue(boundingBoxBitmap)

        }).start()

    }

    fun setScreenProperties(width: Int, height: Int){
        screenDimensions = Size(width, height)
    }

    fun setAlertActivityParams(){
        AlertViewModel.setParameter(recognitions.value?.toList() ?: listOf())
    }

}