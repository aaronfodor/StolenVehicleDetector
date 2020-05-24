package com.arpadfodor.android.stolencardetector.model.ai

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Size

class ObjectDetectionService {

    companion object{

        var model: ObjectDetector? = null

        fun initialize(assets: AssetManager, numThreads: Int){
            model = MobileNetV3Coco(assets, numThreads)
        }

        fun close(){
            model?.close()
        }

    }

    fun recognizeImage(image: Bitmap, maximumRecognitionsToShow: Int, minimumPredictionCertainty: Float): List<RecognizedObject>{
        val results = model?.recognizeImage(image, maximumRecognitionsToShow, minimumPredictionCertainty)
        return results ?: emptyList<RecognizedObject>()
    }

    fun getModelInputSize(): Size {
        val width = model?.IMAGE_SIZE_X ?: 0
        val height = model?.IMAGE_SIZE_Y ?: 0
        return Size(width, height)
    }

}