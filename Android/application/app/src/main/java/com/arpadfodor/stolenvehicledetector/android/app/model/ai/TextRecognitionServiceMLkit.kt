package com.arpadfodor.stolenvehicledetector.android.app.model.ai

import android.graphics.Bitmap
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer

//TODO
class TextRecognitionServiceMLkit {

    companion object{

        var model: TextRecognizer = TextRecognition.getClient()

        fun initialize(){
            model = TextRecognition.getClient()
        }

    }

    fun recognizeText(image: Bitmap) : RecognizedText{

        val input = InputImage.fromBitmap(image, 0)
        val recognitions = mutableListOf<RecognizedText>()

        model.process(input)
            .addOnSuccessListener {

                val rawResult = it

                for(block in rawResult.textBlocks){
                    for(line in block.lines){
                        for(element in line.elements){
                            val recognition = RecognizedText(element.text, element.recognizedLanguage, RectF(element.boundingBox))
                            recognitions.add(recognition)
                        }
                    }

                }

            }
            .addOnFailureListener {}

        if(recognitions.size > 0){
            return recognitions[0]
        }

        return RecognizedText("","")

    }

}