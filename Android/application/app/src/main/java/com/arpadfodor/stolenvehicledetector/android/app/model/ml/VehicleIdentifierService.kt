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
import com.arpadfodor.stolenvehicledetector.android.app.model.ml.ocr.TextRecognitionService
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.VehicleRepository
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.Vehicle
import java.util.*

class VehicleIdentifierService {

    companion object{

        private var stolenVehicles = listOf<Vehicle>()
        private const val OBJECT_TO_PASS_TO_OCR = "License"

        fun initialize(){
            VehicleRepository.getVehicles {vehicleList ->
                val normalizedVehicleList = mutableListOf<Vehicle>()
                for(element in vehicleList){
                    normalizedVehicleList.add(
                        Vehicle(normalizeLicenseId(element.licenseId), element.type, element.manufacturer, element.color))
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

    }

    // enable logging for debugging purposes
    var enableLogging = true

    private val objectDetectionService = ObjectDetectionService()
    private val ocrService = OCRService()
    //private val ocrService = TextRecognitionService()

    fun recognize(inputImage: Bitmap, outputImageSize: Size, deviceOrientation: Int,
                  maximumRecognitionsToShow: Int, minimumPredictionCertaintyToShow: Float,
                  callback: (Array<Pair<String, Bitmap>>) -> Unit) : Bitmap{
        val startRecognitionTime = SystemClock.uptimeMillis()

        // Detector image pre-processing
        Trace.beginSection("Detector prepare image")
        val startPrepareTime = SystemClock.uptimeMillis()

        val bitmapNxN = ImageConverter.bitmapToCroppedNxNImage(inputImage)
        val requiredInputImage = ImageConverter.transformBitmapWithCrop(bitmapNxN, objectDetectionService.getModelInputSize())

        val preparingDuration = SystemClock.uptimeMillis() - startPrepareTime
        log("Detector image preparing duration: $preparingDuration")
        Trace.endSection()

        // Compute results
        val recognizedObjects = objectDetectionService.processImage(requiredInputImage,
            maximumRecognitionsToShow, minimumPredictionCertaintyToShow)

        val recognitions = arrayListOf<Pair<String, Bitmap>>()

        for(recognizedObject in recognizedObjects){

            if(recognizedObject.title == OBJECT_TO_PASS_TO_OCR){

                val ratio = (bitmapNxN.height.toFloat())/(requiredInputImage.height.toFloat())
                val scaledRectF = RectF(recognizedObject.location.left*ratio, recognizedObject.location.top*ratio,
                    recognizedObject.location.right*ratio, recognizedObject.location.bottom*ratio)

                // OCR image preparing
                Trace.beginSection("OCR prepare image")
                val startPrepareTime = SystemClock.uptimeMillis()

                val textImageSnippet = ImageConverter.cutPieceFromImage(bitmapNxN, scaledRectF)
                val resizedOcrImage = ImageConverter.transformBitmapWithPad(textImageSnippet, ocrService.getInputSize())

                val preparingDuration = SystemClock.uptimeMillis() - startPrepareTime
                log("OCR image preparing duration: $preparingDuration")
                Trace.endSection()

                val recognizedTexts = ocrService.processImage(resizedOcrImage, 20, 0f)

                if(recognizedTexts.isNotEmpty()){

                    recognizedObject.extra = recognizedTexts[0].getShortStringWithExtra()

                    // Check if any of the texts are suspicious
                    Trace.beginSection("Check if text is suspicious")
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

        val optimalBoundingBoxImageSize = ImageConverter.transformBitmapWithCrop(requiredInputImage, outputImageSize)
        val boundingBoxBitmap = BoundingBoxDrawer.drawBoundingBoxes(optimalBoundingBoxImageSize,
            deviceOrientation, objectDetectionService.getModelInputSize(), recognizedObjects)

        val maskDuration = SystemClock.uptimeMillis() - startMaskTime
        log("Create mask image duration: $maskDuration")
        Trace.endSection()

        val recognitionDuration = SystemClock.uptimeMillis() - startRecognitionTime
        log("** Whole inference duration: $recognitionDuration **")

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
            Log.println(Log.VERBOSE, "[Vehicle identifier service]", message)
        }
    }

}