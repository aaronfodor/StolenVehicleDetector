package com.arpadfodor.android.stolencardetector.model.ai

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Size
import com.arpadfodor.android.stolencardetector.model.BoundingBoxDrawer
import com.arpadfodor.android.stolencardetector.model.ImageConverter
import com.arpadfodor.android.stolencardetector.model.db.DatabaseService
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
                  callback: (Array<String>) -> Unit) : Bitmap{

        val bitmapNxN = ImageConverter.bitmapToCroppedNxNImage(inputImage)
        val requiredInputImage = ImageConverter.resizeBitmap(bitmapNxN, objectDetectionService.getModelInputSize())

        // Compute results
        val recognitions = objectDetectionService.recognizeImage(requiredInputImage,
            maximumRecognitionsToShow, minimumPredictionCertaintyToShow)

        val suspiciousTexts = arrayListOf<String>()

        for(recognition in recognitions){

            if(recognition.title == "License plate"){

                val ratio = (bitmapNxN.height.toFloat())/(requiredInputImage.height.toFloat())
                val scaledRectF = RectF(recognition.location.left*ratio, recognition.location.top*ratio,
                    recognition.location.right*ratio, recognition.location.bottom*ratio)

                val textImage = ImageConverter.cutPieceFromImage(bitmapNxN, scaledRectF)
                val recognizedText = textRecognitionService.recognizeText(textImage)
                recognition.extra = recognizedText.getShortStringWithExtra()

                if(isIdSuspicious(recognizedText.text)){
                    recognition.alertNeeded = true
                    suspiciousTexts.add(recognizedText.text)
                }

            }

        }

        val optimalBoundingBoxImageSize = ImageConverter.resizeBitmap(requiredInputImage, outputImageSize)
        val boundingBoxBitmap = BoundingBoxDrawer.drawBoundingBoxes(optimalBoundingBoxImageSize,
            deviceOrientation, objectDetectionService.getModelInputSize(), recognitions)

        callback(suspiciousTexts.toTypedArray())

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