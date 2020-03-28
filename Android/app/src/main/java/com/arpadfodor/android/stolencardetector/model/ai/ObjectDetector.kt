package com.arpadfodor.android.stolencardetector.model.ai

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.SystemClock
import android.os.Trace
import android.util.Log
import android.util.Size
import com.arpadfodor.android.stolencardetector.model.ImageConverter
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Interface for interacting with different object detector models similarly.
 */
abstract class ObjectDetector(
    assets: AssetManager,
    threads: Int,

    // Model and label paths
    val MODEL_PATH: String,
    val LABEL_PATH: String,

    // float model
    val IMAGE_MEAN: Float,
    val IMAGE_STD: Float,
    // Input image size required by the model
    val IMAGE_SIZE_X: Int,
    val IMAGE_SIZE_Y: Int,
    // Input image channels (RGB)
    val DIM_CHANNEL_SIZE: Int,
    // Number of bytes of a channel in a pixel
    // 1 means the model is quantized (Int), 4 means non-quantized (floating point)
    val NUM_BYTES_PER_CHANNEL: Int,

    // batch size dimension
    val DIM_BATCH_SIZE: Int,
    // returns this many results
    val NUM_DETECTIONS: Int
    ) {


    // the inference model
    var model: Interpreter? = null

    // pre-allocated buffer to the labels
    var labels: ArrayList<String>? = null

    // GPU delegate to run the model on device GPU
    var gpuDelegate: GpuDelegate? = null

    // A ByteBuffer to hold image data to be feed into TensorFlow Lite as inputs
    val imgData: ByteBuffer

    // pre-allocated buffer to the input image
    val intValues: IntArray

    // contains the number of detected boxes - array of shape [DIM_BATCH_SIZE]
    val numDetections: FloatArray
    // contains the location of detected boxes - array of shape [DIM_BATCH_SIZE, NUM_DETECTIONS, 4]
    val outputLocations: Array<Array<FloatArray>>
    // contains the classes of detected boxes - array of shape [DIM_BATCH_SIZE, NUM_DETECTIONS]
    val outputClasses: Array<FloatArray>
    // contains the scores of detected boxes - array of shape [DIM_BATCH_SIZE, NUM_DETECTIONS]
    val outputScores: Array<FloatArray>

    // enable logging for debugging purposes
    var enableLogging = true

    // number of threads to use
    val numThreads: Int

    init {

        numThreads = threads

        numDetections = FloatArray(DIM_BATCH_SIZE)
        outputLocations = Array(DIM_BATCH_SIZE) { Array(NUM_DETECTIONS) { FloatArray(4) } }
        outputClasses = Array(DIM_BATCH_SIZE) { FloatArray(NUM_DETECTIONS) }
        outputScores = Array(DIM_BATCH_SIZE) { FloatArray(NUM_DETECTIONS) }

        imgData = initImgData()
        intValues = IntArray(IMAGE_SIZE_X * IMAGE_SIZE_Y)

        // Time consuming loading on a different thread
        Thread{
            val mappedByteBuffer = loadModelFile(assets)
            model = buildModel(mappedByteBuffer, numThreads)
            labels = loadLabels(assets)
        }.start()

        log("Model initialized")

    }

    /**
     * Load the model file from Assets
     */
    private fun loadModelFile(assets: AssetManager): MappedByteBuffer{

        val fileDescriptor = assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        log("Model file loaded")
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

    }

    private fun loadLabels(assets: AssetManager): ArrayList<String>{

        val inputStream = assets.open(LABEL_PATH)

        val labels = arrayListOf<String>()
        inputStream.bufferedReader().useLines {
                lines -> lines.forEach { labels.add(it) }
        }

        log("Labels loaded")
        return labels

    }

    private fun buildModel(mappedByteBuffer: MappedByteBuffer, numThreads: Int): Interpreter{

        gpuDelegate = GpuDelegate()
        val options = Interpreter.Options().addDelegate(gpuDelegate)
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
            DIM_BATCH_SIZE
                    * IMAGE_SIZE_X
                    * IMAGE_SIZE_Y
                    * DIM_CHANNEL_SIZE
                    * NUM_BYTES_PER_CHANNEL)
        imgData.order(ByteOrder.nativeOrder())

        return imgData

    }

    fun recognizeImage(image: Bitmap): List<Recognition>{

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
            return ArrayList<Recognition>(0)
            }

        val inferenceDuration = SystemClock.uptimeMillis() - startInferenceTime
        log("Inference duration: $inferenceDuration")
        Trace.endSection()

        // Show the top detections after scaling them back to the input size
        val recognitions: ArrayList<Recognition> = ArrayList<Recognition>(NUM_DETECTIONS)

        for (i in 0 until NUM_DETECTIONS){

            // one bounding box coordinates
            val detection = RectF(
                outputLocations[0][i][1] * IMAGE_SIZE_X,
                outputLocations[0][i][0] * IMAGE_SIZE_Y,
                outputLocations[0][i][3] * IMAGE_SIZE_X,
                outputLocations[0][i][2] * IMAGE_SIZE_Y
            )

            /**
             * SSD Mobilenet V1 assumes class 0 is the background class in label file and class
             * labels start from 1 to number_of_classes+1
             * outputClasses correspond to class index from 0 to number_of_classes
             */
            val labelOffset = 1

            // title from labels list
            val title = labels?.get(outputClasses[0][i].toInt() + labelOffset)
                                    // return an empty list if the labels are not ready
                                    ?: run{
                                        log("INFERENCE FAILURE: Labels list is empty")
                                        return ArrayList<Recognition>(0)
                                    }

            recognitions.add(
                Recognition(
                    "" + i,
                    title,
                    outputScores[0][i],
                    detection
                )
            )

        }

        Trace.endSection()

        return recognitions

    }

    private fun preProcessImage(image: Bitmap): ByteBuffer{

        // Pre-process image
        Trace.beginSection("preprocess image")
        val startPreprocessTime = SystemClock.uptimeMillis()

        image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)

        imgData.rewind()

        for (i in 0 until IMAGE_SIZE_X) {
            for (j in 0 until IMAGE_SIZE_Y) {

                val pixelValue = intValues[i * IMAGE_SIZE_X + j]

                // Quantized model
                if (NUM_BYTES_PER_CHANNEL == 1) {
                    imgData.put((pixelValue shr 16 and 0xFF).toByte())
                    imgData.put((pixelValue shr 8 and 0xFF).toByte())
                    imgData.put((pixelValue and 0xFF).toByte())
                }
                // Float model
                else {
                    imgData.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) /IMAGE_STD)
                    imgData.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                }

            }
        }

        val preprocessDuration = SystemClock.uptimeMillis() - startPreprocessTime
        log("Image preprocess duration: $preprocessDuration")
        Trace.endSection()

        return imgData

    }

    fun enableLogging(enable: Boolean){
        enableLogging = enable
    }

    fun log(message: String){
        if(enableLogging){
            Log.println(Log.VERBOSE, "[Obj. detector]", message)
        }
    }

    fun getStatsString(): String{
        val stats =
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
            "Bounding boxes per inference: $NUM_DETECTIONS/n" +
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