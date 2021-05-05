package com.arpadfodor.stolenvehicledetector.android.app.model.ml.detector

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Size

class ObjectDetectionService {

    companion object{

        var model: ObjectDetector? = null

        fun initialize(assets: AssetManager, numThreads: Int){
            model = MobileNetV2(assets, numThreads)
        }

        fun close(){
            model?.close()
        }

    }

    fun processImage(image: Bitmap, maximumRecognitionsToShow: Int, minimumPredictionCertainty: Float): List<RecognizedObject>{
        val results = model?.inference(image, maximumRecognitionsToShow, minimumPredictionCertainty)
        return results ?: emptyList<RecognizedObject>()
    }

    fun getModelInputSize(): Size {
        val width = model?.IMAGE_SIZE_Y ?: 0
        val height = model?.IMAGE_SIZE_X ?: 0
        return Size(width, height)
    }

}