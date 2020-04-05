package com.arpadfodor.android.stolencardetector.viewmodel.analyzer

import android.graphics.Bitmap
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.arpadfodor.android.stolencardetector.model.BoundingBoxDrawer
import com.arpadfodor.android.stolencardetector.model.ImageConverter
import com.arpadfodor.android.stolencardetector.model.ai.ObjectDetectionService
import com.arpadfodor.android.stolencardetector.view.DetectionListener
import com.arpadfodor.android.stolencardetector.viewmodel.MainViewModel
import java.util.*

class ObjectDetectionAnalyzerProcessor(listener: DetectionListener? = null) : ImageAnalysis.Analyzer{

    private val frameRateWindow = 8
    private val frameTimestamps = ArrayDeque<Long>(5)
    private val listeners = ArrayList<DetectionListener>().apply { listener?.let { add(it) } }
    private var lastAnalyzedTimestamp = 0L
    private var framesPerSecond: Double = -1.0

    private val objectDetectionService = ObjectDetectionService()

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
        val deviceOrientation = MainViewModel.deviceOrientation

        val inputImage = ImageConverter.imageProxyToBitmap(image)
        val rotatedInputImage = ImageConverter.rotateBitmap(inputImage, cameraOrientation)

        // if front camera provided the image, it needs to be mirrored before inference
        if(MainViewModel.lensFacing == CameraSelector.LENS_FACING_FRONT){
            ImageConverter.mirrorHorizontallyBitmap(rotatedInputImage)
        }

        val bitmapNxN = ImageConverter.bitmapToCroppedNxNImage(rotatedInputImage)
        val requiredInputImage = ImageConverter.resizeBitmap(bitmapNxN, objectDetectionService.getModelInputSize())

        // Compute results
        val recognitions = objectDetectionService.recognizeImage(requiredInputImage,
            MainViewModel.MAXIMUM_RECOGNITIONS_TO_SHOW, MainViewModel.MINIMUM_PREDICTION_CERTAINTY_TO_SHOW)

        val templateBoundingBoxBitmap =
            ImageConverter.createSpecifiedBitmap(Size(rotatedInputImage.width, rotatedInputImage.height), Bitmap.Config.ARGB_8888)

        val boundingBoxBitmap = BoundingBoxDrawer.drawBoundingBoxes(templateBoundingBoxBitmap,
            deviceOrientation, objectDetectionService.getModelInputSize(), recognitions)

        // Call all listeners with new image with bounding boxes
        listeners.forEach { it(boundingBoxBitmap) }

        image.close()

    }

}