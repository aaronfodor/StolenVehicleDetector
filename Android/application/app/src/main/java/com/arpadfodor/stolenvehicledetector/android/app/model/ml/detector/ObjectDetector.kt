package com.arpadfodor.stolenvehicledetector.android.app.model.ml.detector

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
import kotlin.math.max
import kotlin.math.min

/**
 * Abstract class for interacting with different object detector models the same way
 **/
abstract class ObjectDetector(
        assets: AssetManager,
        threads: Int,
        // Model and label paths
        val BASE_PATH: String,
        val MODEL_PATH: String,
        val LABEL_PATH: String,

        // Whether the model quantized or not
        val GPU_INFERENCE_SUPPORT: Boolean,

        // image properties
        val IMAGE_MEAN: Float,
        val IMAGE_STD: Float,
        // Input image size required by the model
        val IMAGE_SIZE_X: Int,
        val IMAGE_SIZE_Y: Int,
        // Input image channels (RGB)
        val NUM_CHANNELS: Int,
        // Number of bytes of a channel in a pixel
        // 1 means the model is quantized (Int), 4 means non-quantized (floating point)
        val NUM_BYTES_PER_CHANNEL: Int,

        // returns this many results
        val NUM_DETECTIONS: Int,
        // batch size dimension
        val BATCH_SIZE: Int
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
    private val numThreads: Int = threads

    init {

        numDetections = FloatArray(BATCH_SIZE)
        outputLocations = Array(BATCH_SIZE) { Array(NUM_DETECTIONS) { FloatArray(4) } }
        outputClasses = Array(BATCH_SIZE) { FloatArray(NUM_DETECTIONS) }
        outputScores = Array(BATCH_SIZE) { FloatArray(NUM_DETECTIONS) }

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

    fun processImage(image: Bitmap, maximumRecognitionsToShow: Int, minimumPredictionCertainty: Float): List<RecognizedObject>{
        // Recognize image
        Trace.beginSection("Recognize image")

        // image pre-processing
        val imgData = prepareImage(image)

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
            return ArrayList<RecognizedObject>(0)
            }

        val inferenceDuration = SystemClock.uptimeMillis() - startInferenceTime
        log("Inference duration: $inferenceDuration")
        Trace.endSection()

        val recognitionsSize = min(NUM_DETECTIONS, maximumRecognitionsToShow)

        // Show the top detections after scaling them back to the input size
        val detections: ArrayList<RecognizedObject> = ArrayList<RecognizedObject>(recognitionsSize)

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
                        return ArrayList<RecognizedObject>(0)
                    }

                detections.add(
                    RecognizedObject("" + i, title, certainty, detection)
                )

            }

        }

        Trace.endSection()
        log("detection results: $detections")

        return detections
    }

    fun prepareImage(image: Bitmap): ByteBuffer{
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
                    // grayscale image needed
                    if(NUM_CHANNELS == 1){
                        // grayscale conversion
                        val red = (((pixelValue and 0xFF).toFloat()- IMAGE_MEAN) / IMAGE_STD)
                        val green = (((pixelValue shr 8 and 0xFF).toFloat()- IMAGE_MEAN) / IMAGE_STD)
                        val blue = (((pixelValue shr 16 and 0xFF).toFloat()- IMAGE_MEAN) / IMAGE_STD)

                        // grayscale conversion: the same as during model training (like TensorFlow rgb_to_grayscale)
                        // reference: https://en.wikipedia.org/wiki/Luma_%28video%29
                        val luminance = (red*0.2989f) + (green*0.5870f) + (blue*0.1140f)
                        imgData.putFloat(luminance)
                    }
                    // RGB image needed
                    else{
                        imgData.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                        imgData.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) /IMAGE_STD)
                        imgData.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    }
                }

            }
        }

        val byteBufferDuration = SystemClock.uptimeMillis() - startByteBufferTime
        log("Create byte buffer duration: $byteBufferDuration")
        Trace.endSection()

        return imgData
    }

    private fun log(message: String){
        if(enableLogging){
            Log.println(Log.VERBOSE, "[Detector]", message)
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