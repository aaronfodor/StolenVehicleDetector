package com.arpadfodor.stolenvehicledetector.android.app.model.ml.ocr

import android.graphics.Bitmap
import android.graphics.RectF
import android.os.SystemClock
import android.os.Trace
import android.util.Log
import android.util.Size
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

class TextRecognitionService {

    companion object{
        var model: TextRecognizer = TextRecognition.getClient()

        fun initialize(){
            model = TextRecognition.getClient()
        }

        private const val MAX_RECOGNITIONS_PER_IMAGE = 4
    }

    // enable logging for debugging purposes
    var enableLogging = true

    /**
     * Suspend function - as the caller already runs on a background thread, this is not a problem.
     * The model inference method retrieves results in an async way, thus waiting is needed before returning.
     **/
    private suspend fun process(input: InputImage) : List<RecognizedText>{
        var recognizedTexts = mutableListOf<RecognizedText>()

        return suspendCoroutine { continuation ->

            // Run model inference
            Trace.beginSection("Inference")
            val startInferenceTime = SystemClock.uptimeMillis()

            model.process(input)
                .addOnSuccessListener {

                    val rawResult = it

                    for(block in rawResult.textBlocks){
                        for(line in block.lines){
                            val recognition = RecognizedText(line.text, 1f, line.recognizedLanguage, RectF(line.boundingBox))
                            recognizedTexts.add(recognition)
                        }
                    }

                    if(recognizedTexts.isNotEmpty()){
                        val takeFirstN = min(recognizedTexts.size, MAX_RECOGNITIONS_PER_IMAGE)
                        recognizedTexts = recognizedTexts.take(takeFirstN).toMutableList()
                    }

                    continuation.resume(recognizedTexts)

                    val inferenceDuration = SystemClock.uptimeMillis() - startInferenceTime
                    log("Inference duration: $inferenceDuration")
                    Trace.endSection()

                    log("Text recognition results: ${recognizedTexts}")


                }
                .addOnFailureListener {
                    continuation.resume(recognizedTexts)

                    log("Inference error")
                    Trace.endSection()
                }

        }
    }

    fun processImage(image: Bitmap, maximumBlocks: Int, minimumCertainty: Float) : List<RecognizedText>{
        val input = InputImage.fromBitmap(image, 0)
        var recognizedTexts = listOf<RecognizedText>()

        runBlocking {
            recognizedTexts = process(input)
        }

        return recognizedTexts
    }

    private fun log(message: String){
        if(enableLogging){
            Log.println(Log.VERBOSE, "[OCR]", message)
        }
    }

    fun getInputSize(): Size {
        val width = 200
        val height = 200
        return Size(width, height)
    }

}