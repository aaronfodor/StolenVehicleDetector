package com.arpadfodor.stolenvehicledetector.android.app.model.ml.ocr

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.SystemClock
import android.os.Trace
import kotlin.math.max
import kotlin.math.min

class TestOCR(assets: AssetManager, threads: Int) : OCR(
    assets,
    threads,

    /**
     * Not overriding variables due to super constructor inconsistency: singleton's class is loaded with its static backing fields already initialized with consts
     * More info: https://youtrack.jetbrains.com/issue/KT-21764
     */

    // Model and label paths
    BASE_PATH = "OCR/",
    MODEL_PATH = "OCR_model.tflite",
    LABEL_PATH = "OCR_labelmap.txt",

    // Whether the model quantized or not
    IS_QUANTIZED = true,

    // image properties
    IMAGE_MEAN = 127.5f,
    IMAGE_STD = 127.5f,

    // Input image size required by the model
    IMAGE_SIZE_X = 200,
    IMAGE_SIZE_Y = 50,
    // Input image channels (grayscale)
    DIM_CHANNEL_SIZE = 1,
    // Number of bytes of a channel in a pixel
    // 1 means the model is quantized (Int), 4 means non-quantized (floating point)
    NUM_BYTES_PER_CHANNEL = 4,

    // batch size dimension
    DIM_BATCH_SIZE = 1,
    // returns this many results
    NUM_DETECTIONS = 100
    ) {

    override fun processImage(image: Bitmap, maximumRecognitionsToShow: Int, minimumPredictionCertainty: Float): List<RecognizedText>{

        // Recognize image
        Trace.beginSection("Recognize image")

        // image pre-processing
        val imgData = preProcessImage(image)

        // Feed input & output to TensorFlow
        Trace.beginSection("Feed data")
        val startFeedingTime = SystemClock.uptimeMillis()

        // required input format
        val inputArray: Array<Any> = arrayOf<Any>(imgData)

        // output values will appear in this HashMap
        val outputMap = HashMap<Int, Any>()
        outputMap[0] = outputLocations
        outputMap[1] = outputClasses
        outputMap[2] = outputScores
        outputMap[3] = numDetections

        val feedingDuration = SystemClock.uptimeMillis() - startFeedingTime
        log("Feeding duration: $feedingDuration")
        Trace.endSection()

        // Run model inference
        Trace.beginSection("Inference")
        val startInferenceTime = SystemClock.uptimeMillis()

        // inference call on the model
        model?.runForMultipleInputsOutputs(inputArray, outputMap)
        // return an empty list if the model is not ready
            ?: run {
                log("INFERENCE FAILURE: Model has not been loaded")
                return ArrayList<RecognizedText>(0)
            }

        val inferenceDuration = SystemClock.uptimeMillis() - startInferenceTime
        log("Inference duration: $inferenceDuration")
        Trace.endSection()

        val recognitionsSize = min(NUM_DETECTIONS, maximumRecognitionsToShow)

        // Show the top recognitions after scaling them back to the input size
        val texts: ArrayList<RecognizedText> = ArrayList<RecognizedText>(recognitionsSize)

        for (i in 0 until recognitionsSize){

            val certainty = outputScores[0][i]

            if(certainty >= minimumPredictionCertainty){

                //if one coordinate is out of the image size range, adjust it
                val left = max(0f, outputLocations[0][i][1] * IMAGE_SIZE_X)
                val top = max(0f, outputLocations[0][i][0] * IMAGE_SIZE_Y)
                val right = min(image.width.toFloat(), outputLocations[0][i][3] * IMAGE_SIZE_X)
                val bottom = min(image.height.toFloat(), outputLocations[0][i][2] * IMAGE_SIZE_Y)

                // one bounding box coordinates
                val detection = RectF(left, top, right, bottom)

                /**
                 * The detector assumes class 0 is the background class in label file and class
                 * labels start from 1 to number_of_classes+1
                 * outputClasses correspond to class index from 0 to number_of_classes
                 */
                val labelOffset = 1

                // title from labels list
                val title = labels?.get(outputClasses[0][i].toInt() + labelOffset)
                // return an empty list if the labels are not ready
                    ?: run{
                        log("INFERENCE FAILURE: Labels list is empty")
                        return ArrayList<RecognizedText>(0)
                    }

                texts.add(
                    RecognizedText("" + i, title, RectF(), "")
                )

            }

        }

        Trace.endSection()
        log("detection results: ${texts}")

        return texts

    }

}