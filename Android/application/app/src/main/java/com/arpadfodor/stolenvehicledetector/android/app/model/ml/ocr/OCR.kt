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
        val MODEL_PATH: String = "ocr_model.tflite",
        val LABEL_PATH: String = "charactermap.txt",

        // Whether the model quantized or not
        val IS_QUANTIZED: Boolean = false,
        // Number of bytes of a channel in a pixel
        // 1 means the model is quantized (Int), 4 means non-quantized (floating point)
        val NUM_BYTES_PER_CHANNEL: Int = 4,
        // image properties
        val IMAGE_MEAN: Float = 127.5f,
        val IMAGE_STD: Float = 127.5f,
        // batch size dimension
        val DIM_BATCH_SIZE: Int = 1,
        // Input image size required by the model
        val IMAGE_SIZE_X: Int = 200,
        val IMAGE_SIZE_Y: Int = 50,
        // Input image channels (grayscale)
        val DIM_CHANNEL_SIZE: Int = 1,

        // returns this many text blocks
        val NUM_BLOCKS: Int = 1,
        // maximum text block length
        val MAX_BLOCK_LENGTH: Int = 50,
        // returns this many char probabilities
        val NUM_CHARACTERS: Int = 68
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
    val numThreads: Int

    init {

        numThreads = threads

        imgData = initImgData()
        intValues = IntArray(IMAGE_SIZE_X * IMAGE_SIZE_Y)

        // Time consuming loading on a different thread
        Thread{
            val mappedByteBuffer = loadModelFile(assets)
            model = buildModel(mappedByteBuffer, numThreads)
            labels = loadLabels(assets)
            output = Array(DIM_BATCH_SIZE) { Array(MAX_BLOCK_LENGTH) { FloatArray(NUM_CHARACTERS) } }
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

        if(IS_QUANTIZED){
            gpuDelegate = GpuDelegate()
            options.addDelegate(gpuDelegate)
        }
        options.setNumThreads(numThreads)
        val model = Interpreter(mappedByteBuffer, options)
        model.resizeInput(0, intArrayOf(1, 50, 200, 1) )

        log("Model built")
        return model

    }

    /**
     * This byte buffer is sized to contain the image data once converted to float
     * The interpreter can accept float arrays directly as input, but the ByteBuffer is more efficient as it avoids extra copies in the interpreter
     */
    private fun initImgData(): ByteBuffer{

        val imgData = ByteBuffer.allocateDirect(
            DIM_BATCH_SIZE
                    * IMAGE_SIZE_X
                    * IMAGE_SIZE_Y
                    * DIM_CHANNEL_SIZE
                    * NUM_BYTES_PER_CHANNEL)
        imgData.order(ByteOrder.nativeOrder())

        return imgData

    }

    fun processImage(image: Bitmap, maximumBlocksToShow: Int, minimumCertainty: Float): List<RecognizedText>{

        // Recognize image
        Trace.beginSection("Process image")

        // image pre-processing
        val imgData = preProcessImage(image)

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
            val text = greedySearchBlockDecode(output[i], MAX_BLOCK_LENGTH)
            texts.add(RecognizedText(text, "", RectF(), ""))
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
    fun greedySearchBlockDecode(blockWithCharProbabilities: Array<FloatArray>, maxBlockLength: Int): String{

        // max value indices
        val indices = mutableListOf<Int>()
        val numMaxCharacters = min(maxBlockLength, blockWithCharProbabilities.size)

        // record last index to remove duplicates
        var lastMaxIdx = -1

        for(i in 0 until numMaxCharacters){

            val charProbabilities = blockWithCharProbabilities[i]
            val maxValue = charProbabilities.maxOrNull()
            val maxIdx = charProbabilities.indexOfFirst { it == maxValue }

            if(maxIdx != lastMaxIdx){
                indices.add(maxIdx)
            }
            lastMaxIdx = maxIdx

        }

        // decoded text
        var text = ""
        // decode numbers to text with labels
        for(index in indices){
            if(index < labels.size){
                text += labels[index]
            }
        }

        return text

    }

    fun preProcessImage(image: Bitmap): ByteBuffer{

        // Pre-process image
        Trace.beginSection("create byte buffer image")
        val startByteBufferTime = SystemClock.uptimeMillis()

        image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)

        imgData.rewind()

        for (i in 0 until IMAGE_SIZE_Y) {
            for (j in 0 until IMAGE_SIZE_X) {

                val pixelValue = intValues[i * IMAGE_SIZE_X + j]

                // Quantized model
                if (NUM_BYTES_PER_CHANNEL == 1) {
                    imgData.put((pixelValue shr 16 and 0xFF).toByte())
                    imgData.put((pixelValue shr 8 and 0xFF).toByte())
                    imgData.put((pixelValue and 0xFF).toByte())
                }
                // Float model
                else {

                    val red = (((pixelValue and 0xFF).toFloat()- IMAGE_MEAN) / IMAGE_STD)
                    val green = (((pixelValue shr 8 and 0xFF).toFloat()- IMAGE_MEAN) / IMAGE_STD)
                    val blue = (((pixelValue shr 16 and 0xFF).toFloat()- IMAGE_MEAN) / IMAGE_STD)

                    // grayscale conversion: the same as during model training (like Tensorflow rgb_to_grayscale)
                    // reference: https://en.wikipedia.org/wiki/Luma_%28video%29
                    val luminance = (red*0.2989f) + (green*0.5870f) + (blue*0.1140f)
                    imgData.putFloat(luminance)

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

    fun log(message: String){
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
            "Img channels: $DIM_CHANNEL_SIZE/n" +
            "Bytes per channel: $NUM_BYTES_PER_CHANNEL/n" +
            "[1 means the model is quantized (Int), 4 means non-quantized (floating point)]/n/n" +
            "Batch size: $DIM_BATCH_SIZE/n" +
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