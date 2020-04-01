package com.arpadfodor.android.stolencardetector.model.ai

import android.graphics.Bitmap
import android.util.Size

object ObjectDetectionService {

    var model: ObjectDetector? = null

    fun initialize(_model: ObjectDetector){
        model = _model
    }

    fun recognizeImage(image: Bitmap, maximumRecognitionsToShow: Int, minimumPredictionCertainty: Float): List<Recognition>{
        val results = model?.recognizeImage(image, maximumRecognitionsToShow, minimumPredictionCertainty)
        return results ?: emptyList<Recognition>()
    }

    fun getModelInputSize(): Size {
        val width = model?.IMAGE_SIZE_X ?: 0
        val height = model?.IMAGE_SIZE_Y ?: 0
        return Size(width, height)
    }

    fun close(){
        model?.close()
    }

}