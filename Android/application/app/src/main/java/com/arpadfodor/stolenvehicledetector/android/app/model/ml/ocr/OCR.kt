package com.arpadfodor.stolenvehicledetector.android.app.model.ml.ocr

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.SystemClock
import android.os.Trace
import com.arpadfodor.stolenvehicledetector.android.app.model.ml.Normalization
import com.arpadfodor.stolenvehicledetector.android.app.model.ml.TfliteModel
import kotlin.math.min

/**
 * Abstract class for interacting with different OCR models the same way
 **/
abstract class OCR(
        assets: AssetManager,
        threads: Int,
        BASE_PATH: String,
        MODEL_PATH: String,
        LABEL_PATH: String,
        GPU_INFERENCE_SUPPORT: Boolean,
        NORMALIZATION: Normalization,
        IMAGE_MEAN: Float,
        IMAGE_STD: Float,
        IMAGE_SIZE_X: Int,
        IMAGE_SIZE_Y: Int,
        NUM_CHANNELS: Int,
        NUM_BYTES_PER_CHANNEL: Int,
        BATCH_SIZE: Int,
        TAG: String = "[OCR]",

        // output properties
        // returns this many text blocks
        val NUM_BLOCKS: Int,
        // maximum text block length
        val MAX_BLOCK_LENGTH: Int,
        // returns this many char probabilities
        val NUM_CHARACTERS: Int

) : TfliteModel(assets, threads, BASE_PATH, MODEL_PATH, LABEL_PATH, GPU_INFERENCE_SUPPORT,
        NORMALIZATION, IMAGE_MEAN, IMAGE_STD, IMAGE_SIZE_X, IMAGE_SIZE_Y, NUM_CHANNELS,
        NUM_BYTES_PER_CHANNEL, BATCH_SIZE, TAG){

    // contains the recognized text sequence - array of shape [DIM_BATCH_SIZE, MAX_RESULTS, NUM_CHARACTERS]
    var output: Array<Array<FloatArray>>

    init {
        output = Array(BATCH_SIZE) { Array(MAX_BLOCK_LENGTH) { FloatArray(NUM_CHARACTERS) } }
    }

    fun inference(image: Bitmap, maximumBlocksToShow: Int, minimumCertainty: Float): List<RecognizedText>{
        // Recognize image
        Trace.beginSection("Process image")

        // image pre-processing
        val imgData = prepareImage(image)

        // Feed input & output to TensorFlow
        Trace.beginSection("Feed data")
        val startFeedingTime = SystemClock.uptimeMillis()

        // required input format
        val inputArray: Array<Any> = arrayOf(imgData)

        // output values will appear in this HashMap
        val outputMap = HashMap<Int, Any>()
        outputMap[0] = output

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

        val numBlocks = min(NUM_BLOCKS, maximumBlocksToShow)

        // Show the top recognitions after scaling them back to the input size
        val texts: ArrayList<RecognizedText> = ArrayList<RecognizedText>(numBlocks)

        for (i in 0 until numBlocks){
            val resultText = greedySearchBlockDecode(output[i], MAX_BLOCK_LENGTH)
            texts.add(RecognizedText(resultText.first, resultText.second, "", RectF(), ""))
        }

        Trace.endSection()
        log("inference results: $texts")

        return texts
    }

    /**
     * Greedy search decoding: remove duplicates next to each other & no_char characters to generate output text
     *
     * @param blockWithCharProbabilities    Array of FloatArrays with char probabilities
     * @param maxBlockLength                The maximum length the output text is
     *
     * @return String                       Decoded text
     */
    private fun greedySearchBlockDecode(blockWithCharProbabilities: Array<FloatArray>, maxBlockLength: Int): Pair<String, Float> {
        // max value indices
        val indices = mutableListOf<Int>()
        val numMaxCharacters = min(maxBlockLength, blockWithCharProbabilities.size)

        // record last index to remove duplicates
        var lastMaxIdx = -1

        // record average probability
        var probability = 0f

        for(i in 0 until numMaxCharacters){

            val charProbabilities = blockWithCharProbabilities[i]
            val maxValue = charProbabilities.maxOrNull()
            probability += maxValue ?: 0f

            val maxIdx = charProbabilities.indexOfFirst { it == maxValue }

            if(maxIdx != lastMaxIdx){
                indices.add(maxIdx)
            }
            lastMaxIdx = maxIdx

        }

        probability /= numMaxCharacters

        // decoded text
        var text = ""
        // decode numbers to text with labels
        for(index in indices){
            // -1 as the last label is the non-character token which should be removed
            if(index < labels.size -1){
                text += labels[index]
            }
        }

        return Pair(text, probability)
    }

    override fun getStatsString(): String{
        return super.getStatsString() + "Max block length: $MAX_BLOCK_LENGTH/n" +
                "Max blocks per inference: $NUM_BLOCKS/n"
    }

}