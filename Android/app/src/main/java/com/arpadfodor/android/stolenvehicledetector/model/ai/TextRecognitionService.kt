package com.arpadfodor.android.stolenvehicledetector.model.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.SparseArray
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer

class TextRecognitionService {

    companion object{

        var model: TextRecognizer? = null

        fun initialize(context: Context){
            model = TextRecognizer.Builder(context).build()
        }

        fun close(){
            model?.release()
        }

    }

    fun recognizeText(image: Bitmap) : RecognizedText{

        val input = Frame.Builder().setBitmap(image).build()
        val rawRecognition = model?.detect(input) ?: SparseArray()

        val recognitions = mutableListOf<RecognizedText>()

        for(i in 0 until rawRecognition.size()){
            val rawData = rawRecognition.valueAt(i)
            val recognition = RecognizedText(rawData.value, rawData.language, RectF(rawData.boundingBox))
            recognitions.add(recognition)
        }

        if(recognitions.size > 0){
            return recognitions[0]
        }

        return RecognizedText("","")

    }

}