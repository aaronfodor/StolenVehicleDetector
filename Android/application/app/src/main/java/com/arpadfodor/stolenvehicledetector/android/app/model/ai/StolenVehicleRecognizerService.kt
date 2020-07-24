package com.arpadfodor.stolenvehicledetector.android.app.model.ai

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Size
import com.arpadfodor.stolenvehicledetector.android.app.model.BoundingBoxDrawer
import com.arpadfodor.stolenvehicledetector.android.app.model.ImageConverter
import com.arpadfodor.stolenvehicledetector.android.app.model.db.DatabaseService
import java.util.*

class StolenVehicleRecognizerService {

    companion object{

        private var stolenVehicleLicenses = listOf<String>()

        fun initialize(){
            DatabaseService.getStolenVehicleLicenses {
                stolenVehicleLicenses = it
            }
        }

    }

    private val objectDetectionService = ObjectDetectionService()
    private val textRecognitionService = TextRecognitionService()

    fun recognize(inputImage: Bitmap, outputImageSize: Size, deviceOrientation: Int,
                  maximumRecognitionsToShow: Int, minimumPredictionCertaintyToShow: Float,
                  callback: (Array<Pair<String, Bitmap>>) -> Unit) : Bitmap{

        val bitmapNxN = ImageConverter.bitmapToCroppedNxNImage(inputImage)
        val requiredInputImage = ImageConverter.resizeBitmap(bitmapNxN, objectDetectionService.getModelInputSize())

        // Compute results
        val recognizedObjects = objectDetectionService.recognizeImage(requiredInputImage,
            maximumRecognitionsToShow, minimumPredictionCertaintyToShow)

        val recognitions = arrayListOf<Pair<String, Bitmap>>()

        for(recognizedObject in recognizedObjects){

            if(recognizedObject.title == "License plate"){

                val ratio = (bitmapNxN.height.toFloat())/(requiredInputImage.height.toFloat())
                val scaledRectF = RectF(recognizedObject.location.left*ratio, recognizedObject.location.top*ratio,
                    recognizedObject.location.right*ratio, recognizedObject.location.bottom*ratio)

                val textImageSnippet = ImageConverter.cutPieceFromImage(bitmapNxN, scaledRectF)
                val recognizedText = textRecognitionService.recognizeText(textImageSnippet)
                recognizedObject.extra = recognizedText.getShortStringWithExtra()

                if(isIdSuspicious(recognizedText.text)){
                    recognizedObject.alertNeeded = true
                    recognitions.add(Pair(recognizedText.text, textImageSnippet))
                }

            }

        }

        val optimalBoundingBoxImageSize = ImageConverter.resizeBitmap(requiredInputImage, outputImageSize)
        val boundingBoxBitmap = BoundingBoxDrawer.drawBoundingBoxes(optimalBoundingBoxImageSize,
            deviceOrientation, objectDetectionService.getModelInputSize(), recognizedObjects)

        callback(recognitions.toTypedArray())

        return boundingBoxBitmap

    }

    private fun isIdSuspicious(licenseId: String) : Boolean{
        val clearedLicenseId = licenseId.replace("-", "")
            .replace("_", "").toUpperCase(Locale.ROOT)
        if(stolenVehicleLicenses.contains(clearedLicenseId)){
            return true
        }
        return false
    }

}