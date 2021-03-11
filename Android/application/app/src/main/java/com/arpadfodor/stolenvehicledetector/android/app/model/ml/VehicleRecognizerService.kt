package com.arpadfodor.stolenvehicledetector.android.app.model.ml

import android.graphics.Bitmap
import android.graphics.RectF
import android.os.SystemClock
import android.os.Trace
import android.util.Log
import android.util.Size
import com.arpadfodor.stolenvehicledetector.android.app.model.BoundingBoxDrawer
import com.arpadfodor.stolenvehicledetector.android.app.model.ImageConverter
import com.arpadfodor.stolenvehicledetector.android.app.model.ml.detector.ObjectDetectionService
import com.arpadfodor.stolenvehicledetector.android.app.model.ml.ocr.OCRService
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.VehicleRepository
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.Vehicle
import java.util.*

class VehicleRecognizerService {

    companion object{

        private var stolenVehicles = listOf<Vehicle>()

        fun initialize(){

            VehicleRepository.getVehicles {vehicleList ->
                val normalizedVehicleList = mutableListOf<Vehicle>()
                for(element in vehicleList){
                    normalizedVehicleList.add(Vehicle(normalizeLicenseId(element.licenseId),
                        element.type, element.manufacturer, element.color))
                }
                stolenVehicles = normalizedVehicleList
            }

        }

        private fun normalizeLicenseId(licenseId: String) : String{
            return licenseId
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "")
                .toUpperCase(Locale.ROOT)
        }

        private const val OBJECT_TO_RECOGNIZE_TEXT_TITLE = "License plate"

    }

    // enable logging for debugging purposes
    var enableLogging = true

    private val objectDetectionService = ObjectDetectionService()
    //private val textRecognitionService = TextRecognitionService()
    private val ocrService = OCRService()

    fun recognize(inputImage: Bitmap, outputImageSize: Size, deviceOrientation: Int,
                  maximumRecognitionsToShow: Int, minimumPredictionCertaintyToShow: Float,
                  callback: (Array<Pair<String, Bitmap>>) -> Unit) : Bitmap{

        val startRecognitionTime = SystemClock.uptimeMillis()

        // Pre-process image
        Trace.beginSection("Pre-process image")
        val startPreprocessingTime = SystemClock.uptimeMillis()

        val bitmapNxN = ImageConverter.bitmapToCroppedNxNImage(inputImage)
        val requiredInputImage = ImageConverter.resizeBitmap(bitmapNxN, objectDetectionService.getModelInputSize())

        val preprocessingDuration = SystemClock.uptimeMillis() - startPreprocessingTime
        log("Image pre-processing duration: $preprocessingDuration")
        Trace.endSection()

        // Compute results
        val recognizedObjects = objectDetectionService.processImage(requiredInputImage,
            maximumRecognitionsToShow, minimumPredictionCertaintyToShow)

        val recognitions = arrayListOf<Pair<String, Bitmap>>()

        for(recognizedObject in recognizedObjects){

            if(recognizedObject.title == OBJECT_TO_RECOGNIZE_TEXT_TITLE){

                val ratio = (bitmapNxN.height.toFloat())/(requiredInputImage.height.toFloat())
                val scaledRectF = RectF(recognizedObject.location.left*ratio, recognizedObject.location.top*ratio,
                    recognizedObject.location.right*ratio, recognizedObject.location.bottom*ratio)

                // Run image cut
                Trace.beginSection("Image snippet")
                val startCutTime = SystemClock.uptimeMillis()

                val textImageSnippet = ImageConverter.cutPieceFromImage(bitmapNxN, scaledRectF)

                val cutDuration = SystemClock.uptimeMillis() - startCutTime
                log("Image cut duration: $cutDuration")
                Trace.endSection()

                val recognizedTexts = ocrService.processImage(textImageSnippet, 20, 0f)

                if(recognizedTexts.isNotEmpty()){

                    recognizedObject.extra = recognizedTexts[0].getShortStringWithExtra()

                    // Check if any of the texts are suspicious
                    Trace.beginSection("Check if text suspicious")
                    val startCheckIfSuspiciousTime = SystemClock.uptimeMillis()

                    for(recognizedText in recognizedTexts){

                        if(isIdSuspicious(recognizedText.text){suspiciousTo -> recognizedText.text = suspiciousTo}){
                            recognizedObject.extra = recognizedText.getShortStringWithExtra()
                            recognizedObject.alertNeeded = true
                            recognitions.add(Pair(recognizedText.text, textImageSnippet))
                            break
                        }

                    }

                    val checkIfSuspiciousDuration = SystemClock.uptimeMillis() - startCheckIfSuspiciousTime
                    log("Check if text suspicious: $checkIfSuspiciousDuration")
                    Trace.endSection()

                }

            }

        }

        // Create mask image
        Trace.beginSection("Mask image")
        val startMaskTime = SystemClock.uptimeMillis()

        val optimalBoundingBoxImageSize = ImageConverter.resizeBitmap(requiredInputImage, outputImageSize)
        val boundingBoxBitmap = BoundingBoxDrawer.drawBoundingBoxes(optimalBoundingBoxImageSize,
            deviceOrientation, objectDetectionService.getModelInputSize(), recognizedObjects)

        val maskDuration = SystemClock.uptimeMillis() - startMaskTime
        log("Create mask image duration: $maskDuration")
        Trace.endSection()

        val recognitionDuration = SystemClock.uptimeMillis() - startRecognitionTime
        log("** Whole recognition duration: $recognitionDuration **")

        callback(recognitions.toTypedArray())

        return boundingBoxBitmap

    }

    private fun isIdSuspicious(licenseId: String, similarTo: (String) -> Unit) : Boolean{

        val normalizedLicenseId = normalizeLicenseId(licenseId)

        for(vehicle in stolenVehicles){
            if(normalizedLicenseId.contains(vehicle.licenseId, ignoreCase = true)){
                similarTo(vehicle.licenseId)
                return true
            }
        }

        return false

    }

    private fun log(message: String){
        if(enableLogging){
            Log.println(Log.VERBOSE, "[Vehicle recognizer service]", message)
        }
    }

}