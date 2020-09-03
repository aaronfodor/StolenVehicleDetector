package com.arpadfodor.stolenvehicledetector.android.app.model.ai

import android.graphics.Bitmap
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TextRecognitionService {

    companion object{

        var model: TextRecognizer = TextRecognition.getClient()

        fun initialize(){
            model = TextRecognition.getClient()
        }

    }

    /**
     * Suspend function - as the caller already runs on a background thread, this is not a problem.
     * The model inference method retrieves results in an async way, thus waiting is needed before returning.
     **/
    private suspend fun process(input: InputImage) : RecognizedText{

        var recognizedText = RecognizedText("", "")

        return suspendCoroutine { continuation ->

            model.process(input)
                .addOnSuccessListener {

                    val rawResult = it
                    val recognitions = mutableListOf<RecognizedText>()

                    for(block in rawResult.textBlocks){
                        for(line in block.lines){
                            for(element in line.elements){
                                val recognition = RecognizedText(element.text, element.recognizedLanguage, RectF(element.boundingBox))
                                recognitions.add(recognition)
                            }
                        }
                    }

                    if(recognitions.isNotEmpty()){
                        recognizedText = recognitions[0]
                    }

                    continuation.resume(recognizedText)

                }
                .addOnFailureListener {
                    continuation.resume(recognizedText)
                }

        }

    }

    fun recognizeText(image: Bitmap) : RecognizedText{

        val input = InputImage.fromBitmap(image, 0)
        var recognizedText = RecognizedText("", "")

        runBlocking {
            recognizedText = process(input)
        }

        return recognizedText

    }

}