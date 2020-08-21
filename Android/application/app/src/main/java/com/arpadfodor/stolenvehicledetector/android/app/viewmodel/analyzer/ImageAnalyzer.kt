package com.arpadfodor.stolenvehicledetector.android.app.viewmodel.analyzer

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.arpadfodor.stolenvehicledetector.android.app.model.AuthenticationService
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.Recognition
import com.arpadfodor.stolenvehicledetector.android.app.model.ImageConverter
import com.arpadfodor.stolenvehicledetector.android.app.model.MetaProvider
import com.arpadfodor.stolenvehicledetector.android.app.model.ai.VehicleRecognizerService
import com.arpadfodor.stolenvehicledetector.android.app.view.DetectionListener
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.CameraViewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

class ImageAnalyzer(listener: DetectionListener? = null, viewModel_: CameraViewModel) : ImageAnalysis.Analyzer{

    private val frameRateWindow = 8
    private val frameTimestamps = ArrayDeque<Long>(5)
    private val listeners = ArrayList<DetectionListener>().apply { listener?.let { add(it) } }
    private var lastAnalyzedTimestamp = 0L
    private var framesPerSecond: Double = -1.0

    private val viewModel: CameraViewModel = viewModel_
    private val licensePlateReaderService = VehicleRecognizerService()

    /**
     * Used to add listeners that will be called with each detection computed
     */
    fun onFrameAnalyzed(listener: DetectionListener) = listeners.add(listener)

    /**
     * Analyzes an image to produce a result.
     *
     * The caller is responsible for ensuring this analysis method can be executed quickly
     * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
     * images will not be acquired and analyzed.
     *
     * The image passed to this method becomes invalid after this method returns. The caller
     * should not store external references to this image, as these references will become
     * invalid.
     *
     * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
     * call image.close() on received images when finished using them. Otherwise, new images
     * may not be received or the camera may stall, depending on back pressure setting.
     */
    override fun analyze(image: ImageProxy) {

        // If there are no listeners attached, we don't need to perform analysis
        if (listeners.isEmpty()) {
            image.close()
            return
        }

        // Keep track of frames analyzed
        val currentTime = System.currentTimeMillis()
        frameTimestamps.push(currentTime)

        // Compute the FPS using a moving average
        while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
        val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
        val timestampLast = frameTimestamps.peekLast() ?: currentTime
        framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
                frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

        // Analysis could take an arbitrarily long amount of time
        // Since it is running in a different thread, it won't stall other use cases

        lastAnalyzedTimestamp = frameTimestamps.first

        val cameraOrientation = image.imageInfo.rotationDegrees
        val deviceOrientation = CameraViewModel.deviceOrientation

        val inputImage = ImageConverter.imageProxyToBitmap(image)
        val rotatedInputImage = ImageConverter.rotateBitmap(inputImage, cameraOrientation)

        // if front camera provided the image, it needs to be mirrored before inference
        if(viewModel.lensFacing == CameraSelector.LENS_FACING_FRONT){
            ImageConverter.mirrorHorizontallyBitmap(rotatedInputImage)
        }

        val screenSize = CameraViewModel.screenDimensions
        val smallerScreenDimension = min(screenSize.width, screenSize.height)
        val requiredOutputImageSize = Size(smallerScreenDimension, smallerScreenDimension)

        val imageMeta = MetaProvider.getDeviceMetaData()

        // Use the service to produce the image to show
        val resultImage = licensePlateReaderService.recognize(rotatedInputImage,
            requiredOutputImageSize, deviceOrientation, CameraViewModel.numRecognitionsToShow,
            CameraViewModel.minimumPredictionCertaintyToShow) { arrayOfIdImagePairs ->

            val recognitions = arrayListOf<Recognition>()
            val user = AuthenticationService.userName

            var i = 1
            for(pair in arrayOfIdImagePairs){
                recognitions.add(
                    Recognition(i, false, pair.first, pair.second,
                        imageMeta[0], imageMeta[1], imageMeta[2], user)
                )
                i++
            }
            viewModel.recognitions.postValue(recognitions.toTypedArray())

        }
        
        // Call all listeners with new image with bounding boxes
        listeners.forEach { it(resultImage) }

        image.close()

    }

}