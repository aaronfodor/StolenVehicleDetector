package com.arpadfodor.stolenvehicledetector.android.app.model.ml.detector

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.SystemClock
import android.os.Trace
import com.arpadfodor.stolenvehicledetector.android.app.model.ml.Preprocessing
import com.arpadfodor.stolenvehicledetector.android.app.model.ml.TfliteModel
import kotlin.math.max
import kotlin.math.min

/**
 * Abstract class for interacting with different object detector models the same way
 **/
abstract class ObjectDetector(
    assets: AssetManager,
    threads: Int,
    BASE_PATH: String,
    MODEL_PATH: String,
    LABEL_PATH: String,
    GPU_INFERENCE_SUPPORT: Boolean,
    PREPROCESSING: Preprocessing,
    IMAGE_MEAN: Float,
    IMAGE_STD: Float,
    IMAGE_SIZE_X: Int,
    IMAGE_SIZE_Y: Int,
    NUM_CHANNELS: Int,
    NUM_BYTES_PER_CHANNEL: Int,
    BATCH_SIZE: Int,
    TAG: String = "[Detector]",

    // returns this many results
    val NUM_DETECTIONS: Int

) : TfliteModel(assets, threads, BASE_PATH, MODEL_PATH, LABEL_PATH, GPU_INFERENCE_SUPPORT,
        PREPROCESSING, IMAGE_MEAN, IMAGE_STD, IMAGE_SIZE_X, IMAGE_SIZE_Y, NUM_CHANNELS,
        NUM_BYTES_PER_CHANNEL, BATCH_SIZE, TAG){

    // contains the number of detected boxes - array of shape [DIM_BATCH_SIZE]
    val numDetections: FloatArray //IntArray
    // contains the location of detected boxes - array of shape [DIM_BATCH_SIZE, NUM_DETECTIONS, 4]
    val outputLocations: Array<Array<FloatArray>>
    // contains the classes of detected boxes - array of shape [DIM_BATCH_SIZE, NUM_DETECTIONS]
    val outputClasses: Array<FloatArray>
    // contains the scores of detected boxes - array of shape [DIM_BATCH_SIZE, NUM_DETECTIONS]
    val outputScores: Array<FloatArray>

    init{
        numDetections = FloatArray(BATCH_SIZE) //IntArray(BATCH_SIZE)
        outputLocations = Array(BATCH_SIZE) { Array(NUM_DETECTIONS) { FloatArray(4) } }
        outputClasses = Array(BATCH_SIZE) { FloatArray(NUM_DETECTIONS) }
        outputScores = Array(BATCH_SIZE) { FloatArray(NUM_DETECTIONS) }
    }

    fun inference(image: Bitmap, maximumRecognitionsToShow: Int, minimumPredictionCertainty: Float): List<RecognizedObject>{
        // Recognize image
        Trace.beginSection("Recognize image")

        // image pre-processing
        val imgData = prepareImage(image)

        // Feed input & output to TensorFlow
        Trace.beginSection("Feed data")
        val startFeedingTime = SystemClock.uptimeMillis()

        // required input format
        val inputArray: Array<Any> = arrayOf(imgData)

        // output values will appear in this HashMap
        val outputMap = HashMap<Int, Any>()
        outputMap[0] = outputLocations
        outputMap[1] = outputClasses
        outputMap[2] = outputScores
        outputMap[3] = numDetections
        /* for the custom object detector
        outputMap[2] = outputLocations
        outputMap[1] = outputClasses
        outputMap[3] = outputScores
        outputMap[0] = numDetections
        * */

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
            return ArrayList(0)
            }

        val inferenceDuration = SystemClock.uptimeMillis() - startInferenceTime
        log("Inference duration: $inferenceDuration")
        Trace.endSection()

        val recognitionsSize = min(NUM_DETECTIONS, maximumRecognitionsToShow)

        // Show the top detections after scaling them back to the input size
        val detections: ArrayList<RecognizedObject> = ArrayList(recognitionsSize)

        for (i in 0 until recognitionsSize){

            val certainty = outputScores[0][i]

            if(certainty >= minimumPredictionCertainty){

                //if one coordinate is out of the image size range, adjust it
                val left = max(0f, outputLocations[0][i][1] * IMAGE_SIZE_X)
                val top = max(0f, outputLocations[0][i][0] * IMAGE_SIZE_Y)
                val right = min(image.width.toFloat(), outputLocations[0][i][3] * IMAGE_SIZE_X)
                val bottom = min(image.height.toFloat(), outputLocations[0][i][2] * IMAGE_SIZE_Y)
                /* for custom object detector
                val left = max(0f, outputLocations[0][i][0] * IMAGE_SIZE_X)
                val top = max(0f, outputLocations[0][i][1] * IMAGE_SIZE_Y)
                val right = min(image.width.toFloat(), outputLocations[0][i][2] * IMAGE_SIZE_X)
                val bottom = min(image.height.toFloat(), outputLocations[0][i][3] * IMAGE_SIZE_Y)
                * */

                // one bounding box coordinates
                val detection = RectF(left, top, right, bottom)

                /**
                 * The detector assumes class 0 is the background class in label file and class
                 * labels start from 1 to number_of_classes+1
                 * outputClasses correspond to class index from 0 to number_of_classes
                 */
                val labelOffset = 1

                // title from labels list
                val title = labels[outputClasses[0][i].toInt() + labelOffset]

                detections.add(
                    RecognizedObject("" + i, title, certainty, detection)
                )

            }

        }

        Trace.endSection()
        log("detection results: $detections")

        return detections
    }

    override fun getStatsString(): String{
        return super.getStatsString() + "Bounding boxes per inference: $NUM_DETECTIONS/n"
    }

}