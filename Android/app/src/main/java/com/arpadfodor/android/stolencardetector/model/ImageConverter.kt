package com.arpadfodor.android.stolencardetector.model

import android.graphics.*
import android.util.Size
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import kotlin.math.max

object ImageConverter {

    fun imageProxyToBitmap(image: ImageProxy, rotation: Int, desiredSize: Size): Bitmap {

        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)

        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()

        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val croppedBitmap = bitmapToCroppedNxNImage(bitmap)
        val inputBitmap = rotateAndResizeBitmap(croppedBitmap, rotation, desiredSize)

        return inputBitmap

    }

    private fun bitmapToCroppedNxNImage(sourceBitmap: Bitmap): Bitmap{

        val croppedBitmap: Bitmap?

        val matrix = Matrix()

        if (sourceBitmap.width >= sourceBitmap.height) {

            croppedBitmap = Bitmap.createBitmap(
                sourceBitmap,
                sourceBitmap.width / 2 - sourceBitmap.height / 2,
                0,
                sourceBitmap.height,
                sourceBitmap.height,
                matrix,
                false
            )

        } else {

            croppedBitmap = Bitmap.createBitmap(
                sourceBitmap,
                0,
                sourceBitmap.height / 2 - sourceBitmap.width / 2,
                sourceBitmap.width,
                sourceBitmap.width,
                matrix,
                false
            )
        }

        return croppedBitmap

    }

    /**
     * Returns the resized image
     *
     * @param croppedBitmap     The input image which has NxN dimensions
     * @param rotationDegrees   Value of desired rotation in degrees
     * @param desiredSize       The desired output image dimensions
     *
     * @return Bitmap           The resized Bitmap
     */
    private fun rotateAndResizeBitmap(croppedBitmap: Bitmap, rotationDegrees: Int, desiredSize: Size): Bitmap{

        val cropToFrameTransform = Matrix()

        val resizedBitmap: Bitmap = Bitmap.createBitmap(
            desiredSize.width,
            desiredSize.height,
            Bitmap.Config.ARGB_8888
        )

        val frameToReScaleTransform = getTransformationMatrix(
            croppedBitmap.width,
            croppedBitmap.height,
            desiredSize.width,
            desiredSize.height,
            rotationDegrees,
            //maintain aspect ratio
            true
        )

        frameToReScaleTransform.invert(cropToFrameTransform)

        val canvas = Canvas(resizedBitmap)
        canvas.drawBitmap(croppedBitmap, frameToReScaleTransform, null)

        return resizedBitmap

    }

    /**
     * Returns a transformation matrix from one reference frame into another
     * Handles cropping (if maintaining aspect ratio is desired) and rotation
     *
     * @param srcWidth Width of source frame
     * @param srcHeight Height of source frame
     * @param dstWidth Width of destination frame
     * @param dstHeight Height of destination frame
     * @param applyRotation Amount of rotation to apply from one frame to another. Must be a multiple of 90
     * @param maintainAspectRatio If true, will ensure that scaling in x and y remains constant, cropping the image if necessary
     *
     * @return The transformation fulfilling the desired requirements
     */
    private fun getTransformationMatrix(srcWidth: Int, srcHeight: Int, dstWidth: Int, dstHeight: Int, applyRotation: Int, maintainAspectRatio: Boolean): Matrix {

        val matrix = Matrix()

        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
            }

            // Translate so center of image is at origin
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f)

            // Rotate around origin
            matrix.postRotate(applyRotation.toFloat())
        }

        // Account for the already applied rotation, if any, and then determine how much scaling is needed for each axis
        val transpose = (abs(applyRotation) + 90) % 180 == 0

        val inWidth = if (transpose) srcHeight else srcWidth
        val inHeight = if (transpose) srcWidth else srcHeight

        // Apply scaling if necessary
        if (inWidth != dstWidth || inHeight != dstHeight) {

            val scaleFactorX = dstWidth / inWidth.toFloat()
            val scaleFactorY = dstHeight / inHeight.toFloat()

            if (maintainAspectRatio) {
                // Scale by minimum factor so that dst is filled completely while maintaining the aspect ratio
                // Some image may fall off the edge
                val scaleFactor = max(scaleFactorX, scaleFactorY)
                matrix.postScale(scaleFactor, scaleFactor)
            } else {
                // Scale exactly to fill dst from src
                matrix.postScale(scaleFactorX, scaleFactorY)
            }

        }

        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f)
        }

        return matrix

    }

}