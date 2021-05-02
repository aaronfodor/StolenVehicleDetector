package com.arpadfodor.stolenvehicledetector.android.app.model.ml.ocr

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Size

class OCRService {

    companion object{
        var model: OCR? = null

        fun initialize(assets: AssetManager, numThreads: Int){
            model = OCR(assets, numThreads)
        }

        fun close(){
            model?.close()
        }
    }

    fun processImage(image: Bitmap, maximumBlocks: Int, minimumCertainty: Float): List<RecognizedText>{
        val results = model?.processImage(image, maximumBlocks, minimumCertainty)
        return results ?: emptyList<RecognizedText>()
    }

    fun getInputSize(): Size {
        val width = model?.IMAGE_SIZE_X ?: 0
        val height = model?.IMAGE_SIZE_Y ?: 0
        return Size(width, height)
    }

}