package com.arpadfodor.stolenvehicledetector.android.app.model.ml

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.SystemClock
import android.os.Trace
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

enum class Normalization{
    NORMALIZE, STANDARDIZE, NOTHING
}

/**
 * Abstract class for interacting with different tflite models the same way
 **/
abstract class TfliteModel(
        assets: AssetManager,
        threads: Int,
        // Model and label paths
        val BASE_PATH: String,
        val MODEL_PATH: String,
        val LABEL_PATH: String,

        // Whether the model quantized or not
        val GPU_INFERENCE_SUPPORT: Boolean,

        // input properties
        // image properties
        val NORMALIZATION: Normalization,
        val IMAGE_MEAN: Float = 127.5f,
        val IMAGE_STD: Float = 127.5f,
        // Input image size required by the model
        val IMAGE_SIZE_X: Int,
        val IMAGE_SIZE_Y: Int,
        // Input image channels (RGB)
        val NUM_CHANNELS: Int,
        // Number of bytes of a channel in a pixel
        // 1 means Int input, 4 means floating point
        val NUM_BYTES_PER_CHANNEL: Int,
        // batch size dimension
        val BATCH_SIZE: Int,
        // type tag
        val TAG: String
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
        }.start()

        log("Model initialized")
    }

    /**
     * Load the model file from Assets
     */
    private fun loadModelFile(assets: AssetManager): MappedByteBuffer {
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

    private fun buildModel(mappedByteBuffer: MappedByteBuffer, numThreads: Int): Interpreter {

        val compatibility = CompatibilityList()

        val options = Interpreter.Options().apply {

            if(compatibility.isDelegateSupportedOnThisDevice && GPU_INFERENCE_SUPPORT){
                // GPU inference
                val delegateOptions = compatibility.bestOptionsForThisDevice
                this.addDelegate(GpuDelegate(delegateOptions))
            }
            else{
                // CPU inference
                this.setNumThreads(numThreads)
            }

        }

        val model = Interpreter(mappedByteBuffer, options)

        log("Model built")
        return model
    }

    /**
     * This byte buffer is sized to contain the image data once converted to float
     * The interpreter can accept float arrays directly as input, but the ByteBuffer is more efficient as it avoids extra copies in the interpreter
     */
    private fun initImgData(): ByteBuffer {
        val imgData = ByteBuffer.allocateDirect(
                BATCH_SIZE
                        * IMAGE_SIZE_X
                        * IMAGE_SIZE_Y
                        * NUM_CHANNELS
                        * NUM_BYTES_PER_CHANNEL)
        imgData.order(ByteOrder.nativeOrder())

        return imgData
    }

    fun prepareImage(image: Bitmap): ByteBuffer {
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
                        val red = normalize((pixelValue shr 16 and 0xFF).toFloat())
                        val green = normalize((pixelValue shr 8 and 0xFF).toFloat())
                        val blue = normalize((pixelValue and 0xFF).toFloat())
                        // grayscale conversion: the same as during model training (like TensorFlow rgb_to_grayscale)
                        // reference: https://en.wikipedia.org/wiki/Luma_%28video%29
                        val luminance = (red*0.2989f) + (green*0.5870f) + (blue*0.1140f)
                        imgData.putFloat(luminance)
                    }
                    // RGB image needed
                    else{
                        val red = normalize((pixelValue shr 16 and 0xFF).toFloat())
                        val green = normalize((pixelValue shr 8 and 0xFF).toFloat())
                        val blue = normalize((pixelValue and 0xFF).toFloat())
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

    private fun normalize(rawValue: Float) : Float{
        return when(NORMALIZATION){

            Normalization.NORMALIZE -> {
                // normalization [0,1)
                rawValue / 255f
            }
            Normalization.STANDARDIZE -> {
                // standardization (-1,1)
                ((rawValue - IMAGE_MEAN) / IMAGE_STD)
            }
            Normalization.NOTHING -> {
                // do nothing with the value
                rawValue
            }

        }
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
            Log.println(Log.VERBOSE, TAG, message)
        }
    }

    open fun getStatsString(): String{
        return "Base folder: $BASE_PATH/n" +
                "Model file name: $MODEL_PATH/n" +
                "Label file name: $LABEL_PATH/n/n" +
                "Img mean: $IMAGE_MEAN/n" +
                "Img std: $IMAGE_STD/n/n" +
                "Input img size X: $IMAGE_SIZE_X/n" +
                "Input img size Y: $IMAGE_SIZE_Y/n/n" +
                "Img channels: $NUM_CHANNELS/n" +
                "Bytes per channel: $NUM_BYTES_PER_CHANNEL/n" +
                "Batch size: $BATCH_SIZE/n" +
                "# threads to use: $numThreads/n"
    }

    fun close(){
        model?.close()
        gpuDelegate?.close()

        model = null
        gpuDelegate = null
    }

}