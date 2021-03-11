package com.arpadfodor.stolenvehicledetector.android.app.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import androidx.lifecycle.MutableLiveData
import com.arpadfodor.stolenvehicledetector.android.app.model.AccountService
import com.arpadfodor.stolenvehicledetector.android.app.model.ImageConverter
import com.arpadfodor.stolenvehicledetector.android.app.model.ml.VehicleRecognizerService
import com.arpadfodor.stolenvehicledetector.android.app.model.MediaHandler
import com.arpadfodor.stolenvehicledetector.android.app.model.MetaProvider
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.UserRecognition
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.AppViewModel
import kotlin.math.min

class LoadViewModel : AppViewModel(){

    companion object{

        const val GALLERY_REQUEST_CODE = 2

        var numRecognitionsToShow = 10
        var minimumPredictionCertaintyToShow = 0.5f
            set(value) {
                field = value/100f
            }

        var screenDimensions = Size(0, 0)

    }

    private val vehicleRecognizerService = VehicleRecognizerService()

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
    val recognitions: MutableLiveData<Array<UserRecognition>> by lazy {
        MutableLiveData<Array<UserRecognition>>()
    }

    fun loadImage(selectedImageUri: Uri, callback: (Boolean) -> Unit){

        Thread {

            val sourceBitmap = MediaHandler.getImageByUri(selectedImageUri)

            sourceBitmap ?: callback(false)

            sourceBitmap?.let {

                val imageOrientation = MediaHandler.getPhotoOrientation(selectedImageUri)
                val imageMetaInfo = MetaProvider.getImageMetaData(selectedImageUri)

                // remove the bounding boxes of the previous image
                boundingBoxImage.postValue(null)
                imageMetaData.postValue(imageMetaInfo)

                val rotatedBitmap = ImageConverter.rotateBitmap(it, imageOrientation)
                loadedImage.postValue(rotatedBitmap)

                recognizeImage(rotatedBitmap, imageMetaInfo)

            }

        }.start()

    }

    fun rotateImage(){

        // remove the bounding boxes of the previous image
        boundingBoxImage.postValue(null)

        val sourceBitmap = loadedImage.value ?: return

        Thread {

            val rotatedBitmap = ImageConverter.rotateBitmap(sourceBitmap, 90)
            loadedImage.postValue(rotatedBitmap)

            recognizeImage(rotatedBitmap, imageMetaData.value ?: arrayOf("", "", ""))

        }.start()

    }

    private fun recognizeImage(rotatedBitmap: Bitmap, imageMeta: Array<String>){

        val smallerScreenDimension = min(screenDimensions.width, screenDimensions.height)
        val requiredOutputImageSize = Size(smallerScreenDimension, smallerScreenDimension)

        val boundingBoxBitmap = vehicleRecognizerService.recognize(rotatedBitmap,
            requiredOutputImageSize, 0,
            numRecognitionsToShow, minimumPredictionCertaintyToShow) {arrayOfIdImagePairs ->

            val recognitions = arrayListOf<UserRecognition>()
            val user = AccountService.userId

            var i = 1
            for(pair in arrayOfIdImagePairs){

                recognitions.add(
                    UserRecognition(
                        artificialId = i,
                        isSelected = false,
                        isSent = false,
                        isAlert = true,
                        licenseId = pair.first,
                        image = pair.second,
                        date = imageMeta[0],
                        latitude = imageMeta[1],
                        longitude = imageMeta[2],
                        reporter = user)
                )

                i++
            }
            this.recognitions.postValue(recognitions.toTypedArray())
        }

        boundingBoxImage.postValue(boundingBoxBitmap)

    }

    fun setScreenProperties(width: Int, height: Int){
        screenDimensions = Size(width, height)
    }

    fun setAlertActivityParams(){
        AlertViewModel.setParameter(recognitions.value?.toList() ?: listOf())
    }

}