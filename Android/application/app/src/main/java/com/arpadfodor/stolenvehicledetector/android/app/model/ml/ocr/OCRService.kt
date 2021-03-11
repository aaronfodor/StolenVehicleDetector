package com.arpadfodor.stolenvehicledetector.android.app.model.ml.ocr

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log

class OCRService {

    companion object{

        var model: OCR? = null

        fun initialize(assets: AssetManager, numThreads: Int){
            model = TestOCR(assets, numThreads)
        }

        fun close(){
            model?.close()
        }

    }

    // enable logging for debugging purposes
    var enableLogging = true

    fun processImage(image: Bitmap, maximumRecognitionsToShow: Int, minimumPredictionCertainty: Float): List<RecognizedText>{
        val results = model?.processImage(image, maximumRecognitionsToShow, minimumPredictionCertainty)
        return results ?: emptyList<RecognizedText>()
    }

    private fun log(message: String){
        if(enableLogging){
            Log.println(Log.VERBOSE, "[OCR]", message)
        }
    }

}