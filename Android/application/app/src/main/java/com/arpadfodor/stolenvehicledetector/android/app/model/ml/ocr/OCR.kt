package com.arpadfodor.stolenvehicledetector.android.app.model.ml.ocr

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.SystemClock
import android.os.Trace
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.min

/**
 * Abstract class for interacting with different OCR models the same way
 **/
class OCR(
        assets: AssetManager,
        threads: Int,
        // Model and label paths
        val BASE_PATH: String = "OCR/",
        val MODEL_PATH: String = "model.tflite",
        val LABEL_PATH: String = "characters.txt",

        // Whether the model can run on GPU
        val GPU_INFERENCE_SUPPORT: Boolean = true,

        // input properties
        // image properties
        val IMAGE_MEAN: Float = 127.5f,
        val IMAGE_STD: Float = 127.5f,
        // input image size required
        val IMAGE_SIZE_X: Int = 500,
        val IMAGE_SIZE_Y: Int = 50,
        // input image channels (1: grayscale, 3: RGB)
        val NUM_CHANNELS: Int = 3,
        // number of bytes of a channel in a pixel
        // 1 means the model is quantized (Int), 4 means non-quantized (floating point)
        val NUM_BYTES_PER_CHANNEL: Int = 4,

        // output properties
        // returns this many text blocks
        val NUM_BLOCKS: Int = 1,
        // maximum text block length
        val MAX_BLOCK_LENGTH: Int = 125,
        // returns this many char probabilities
        val NUM_CHARACTERS: Int = 38,

        // batch size
        val BATCH_SIZE: Int = 1
    ) {

    // the inference model
    var model: Interpreter? = null

    // pre-allocated buffer to the labels
    var labels: ArrayList<String> = arrayListOf()

    // GPU delegate to run the model on device GPU
    var gpuDelegate: GpuDelegate? = null

    // A ByteBuffer to hold image data to be feed into TensorFlow Lite as inputs
    val imgData: ByteBuffer

    // pre-allocated buffer to the input image
    val intValues: IntArray

    // contains the recognized text sequence - array of shape [DIM_BATCH_SIZE, MAX_RESULTS, NUM_CHARACTERS]
    lateinit var output: Array<Array<FloatArray>>

    // enable logging for debugging purposes
    var enableLogging = true

    // number of threads to use
    private val numThreads: Int = threads

    init {
        imgData = initImgData()
        intValues = IntArray(IMAGE_SIZE_X * IMAGE_SIZE_Y)

        // Time consuming loading on a different thread
        Thread{
            val mappedByteBuffer = loadModelFile(assets)
            model = buildModel(mappedByteBuffer, numThreads)
            labels = loadLabels(assets)
            output = Array(BATCH_SIZE) { Array(MAX_BLOCK_LENGTH) { FloatArray(NUM_CHARACTERS) } }
        }.start()

        log("Model initialized")
    }

    /**
     * Load the model file from Assets
     */
    private fun loadModelFile(assets: AssetManager): MappedByteBuffer{
        val fileDescriptor = assets.openFd(BASE_PATH + MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        inputStream.close()

        log("Model file loaded")
        return  byteBuffer
    }

    private fun loadLabels(assets: AssetManager): ArrayList<String>{
        val inputStream = assets.open(BASE_PATH + LABEL_PATH)

        val labels = arrayListOf<String>()

        inputStream.apply {
            bufferedReader().useLines {
                    lines -> lines.forEach { labels.add(it) }
            }
        }.close()

        log("Labels loaded")
        return labels
    }

    private fun buildModel(mappedByteBuffer: MappedByteBuffer, numThreads: Int): Interpreter{
        val options = Interpreter.Options()

        if(GPU_INFERENCE_SUPPORT){
            gpuDelegate = GpuDelegate()
            options.addDelegate(gpuDelegate)
        }
        options.setNumThreads(numThreads)
        val model = Interpreter(mappedByteBuffer, options)

        log("Model built")
        return model
    }

    /**
     * This byte buffer is sized to contain the image data once converted to float
     * The interpreter can accept float arrays directly as input, but the ByteBuffer is more efficient as it avoids extra copies in the interpreter
     */
    private fun initImgData(): ByteBuffer{
        val imgData = ByteBuffer.allocateDirect(
            BATCH_SIZE
                    * IMAGE_SIZE_X
                    * IMAGE_SIZE_Y
                    * NUM_CHANNELS
                    * NUM_BYTES_PER_CHANNEL)
        imgData.order(ByteOrder.nativeOrder())
        return imgData
    }

    fun processImage(image: Bitmap, maximumBlocksToShow: Int, minimumCertainty: Float): List<RecognizedText>{
        // Recognize image
        Trace.beginSection("Process image")

        // image pre-processing
        val imgData = prepareImage(image)

        // Feed input & output to TensorFlow
        Trace.beginSection("Feed data")
        val startFeedingTime = SystemClock.uptimeMillis()

        // required input format
        val inputArray: Array<Any> = arrayOf<Any>(imgData)

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
        log("inference results: ${texts}")

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

    fun prepareImage(image: Bitmap): ByteBuffer{
        // prepare image
        Trace.beginSection("create byte buffer image")
        val startByteBufferTime = SystemClock.uptimeMillis()

        image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)

        imgData.rewind()

        for (i in 0 until IMAGE_SIZE_Y) {
            for (j in 0 until IMAGE_SIZE_X) {

                val pixelValue = intValues[i * IMAGE_SIZE_X + j]

                // integer input
                if (NUM_BYTES_PER_CHANNEL == 1) {
                    val red = (pixelValue shr 16 and 0xFF).toByte()
                    val green = (pixelValue shr 8 and 0xFF).toByte()
                    val blue = (pixelValue and 0xFF).toByte()
                    imgData.put(red)
                    imgData.put(green)
                    imgData.put(blue)
                }
                // float input
                else {
                    // grayscale image needed
                    if(NUM_CHANNELS == 1){
                        // grayscale conversion
                        val red = (((pixelValue shr 16 and 0xFF).toFloat()- IMAGE_MEAN) / IMAGE_STD)
                        val green = (((pixelValue shr 8 and 0xFF).toFloat()- IMAGE_MEAN) / IMAGE_STD)
                        val blue = (((pixelValue and 0xFF).toFloat()- IMAGE_MEAN) / IMAGE_STD)
                        // grayscale conversion: the same as during model training (like TensorFlow rgb_to_grayscale)
                        // reference: https://en.wikipedia.org/wiki/Luma_%28video%29
                        val luminance = (red*0.2989f) + (green*0.5870f) + (blue*0.1140f)
                        imgData.putFloat(luminance)
                    }
                    // RGB image needed
                    else{
                        //val red = ((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD
                        //val green = ((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) /IMAGE_STD
                        //val blue = ((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD
                        // normalization is not needed here
                        val red = (pixelValue shr 16 and 0xFF).toFloat()
                        val green = (pixelValue shr 8 and 0xFF).toFloat()
                        val blue = (pixelValue and 0xFF).toFloat()
                        imgData.putFloat(red)
                        imgData.putFloat(green)
                        imgData.putFloat(blue)
                    }
                }

            }
        }

        val byteBufferDuration = SystemClock.uptimeMillis() - startByteBufferTime
        log("Create byte buffer duration: $byteBufferDuration")
        Trace.endSection()

        return imgData
    }

    fun changeLoggingState(){
        if(enableLogging){
            enableLogging = false
        }
        else if(!enableLogging){
            enableLogging = true
        }
    }

    private fun log(message: String){
        if(enableLogging){
            Log.println(Log.VERBOSE, "[OCR]", message)
        }
    }

    fun getStatsString(): String{
        val stats =
            "Base folder: $BASE_PATH/n" +
            "Model file name: $MODEL_PATH/n" +
            "Label file name: $LABEL_PATH/n/n" +
            "Img mean: $IMAGE_MEAN/n" +
            "Img std: $IMAGE_STD/n/n" +
            "Input img size X: $IMAGE_SIZE_X/n" +
            "Input img size Y: $IMAGE_SIZE_Y/n/n" +
            "Img channels: $NUM_CHANNELS/n" +
            "Bytes per channel: $NUM_BYTES_PER_CHANNEL/n" +
            "Batch size: $BATCH_SIZE/n" +
            "Max block length: $MAX_BLOCK_LENGTH/n" +
            "Max blocks per inference: $NUM_BLOCKS/n" +
            "# threads to use: $numThreads/n"
        return stats
    }

    fun close(){
        model?.close()
        gpuDelegate?.close()
        model = null
        gpuDelegate = null
    }

}