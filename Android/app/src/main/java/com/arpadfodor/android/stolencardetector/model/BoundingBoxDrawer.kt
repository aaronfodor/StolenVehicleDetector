package com.arpadfodor.android.stolencardetector.model

import android.graphics.*
import android.util.Size
import com.arpadfodor.android.stolencardetector.model.ai.Recognition
import kotlin.math.max
import kotlin.math.min

object BoundingBoxDrawer {

    private var boxRadius = 10f
    private var textBoxRadius = 5f
    private var lineWidth = 5f
    private var bbTextSize = 40f
    private val fallbackColor = Color.parseColor("#B3FF0000")
    private val colorsList = mutableListOf<Int>()
    private var colorsMap: MutableMap<String, Int> = HashMap()

    fun initialize(boxRadius_: Float, lineWidth_: Float, bbTextSize_: Float, colorsList_: Array<String>){

        boxRadius = boxRadius_
        textBoxRadius = boxRadius_/2
        lineWidth = lineWidth_
        bbTextSize = bbTextSize_

        for(color in colorsList_){
            colorsList.add(Color.parseColor(color))
        }

    }

    /**
     * Returns a bitmap with bounding boxes on it
     *
     * @param inputBitmap           Input bitmap with dimensions
     * @param deviceOrientation     Orientation of the device
     * @param modelInputSize        Size to scale from
     * @param recognitions          List of recognitions to draw
     *
     * @return Bitmap               Bitmap with bounding boxes
     */
    fun drawBoundingBoxes(inputBitmap: Bitmap, deviceOrientation: Int, modelInputSize: Size, recognitions: List<Recognition>): Bitmap{

        val bitmapToDrawOn = Bitmap.createBitmap(inputBitmap.width, inputBitmap.height, Bitmap.Config.ARGB_8888)

        val boxPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = fallbackColor
            strokeWidth = lineWidth
        }

        val textBackgroundPaint = Paint().apply {
            style = Paint.Style.FILL
            color = fallbackColor
        }

        val textPaint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.WHITE
            textSize = bbTextSize
            textAlign = Paint.Align.CENTER
        }

        val names = mutableListOf<String>()
        for(recognition in recognitions){
            names.add(recognition.title)
        }
        // update the color according to recognition id
        updateColorsMap(names)

        for (recognition in recognitions) {

            drawBoundingBox(bitmapToDrawOn, deviceOrientation, modelInputSize, recognition,
                boxPaint, textBackgroundPaint, textPaint)

        }

        return bitmapToDrawOn

    }

    /**
     * Does not return a value, but draws on the input Bitmap
     *
     * @param bitmapToDrawOn           Input bitmap which the function draws on
     */
    private fun drawBoundingBox(bitmapToDrawOn: Bitmap, deviceOrientation: Int, modelInputSize: Size, recognition: Recognition,
                                boxPaint: Paint, textBackgroundPaint: Paint, textPaint: Paint){

        val toDrawOnCanvas = Canvas(bitmapToDrawOn)

        val boxColor = colorsMap[recognition.title] ?: fallbackColor
        boxPaint.color = boxColor
        textBackgroundPaint.color = boxColor

        val horizontalScale = bitmapToDrawOn.width.toFloat() / modelInputSize.width.toFloat()
        val verticalScale = bitmapToDrawOn.height.toFloat() / modelInputSize.height.toFloat()
        val scale = min(horizontalScale, verticalScale)
        val padding = (max(bitmapToDrawOn.width, bitmapToDrawOn.height).toFloat() - min(bitmapToDrawOn.width, bitmapToDrawOn.height).toFloat()) / 2f
        val smallerDimension = min(bitmapToDrawOn.width, bitmapToDrawOn.height)

        val rect = recognition.location
        val scaledRect = getScaledRect(rect, scale, padding, smallerDimension, deviceOrientation)

        toDrawOnCanvas.drawRoundRect(scaledRect, boxRadius, boxRadius, boxPaint)

        val textToShow = recognition.getStringShortData()
        val textWidth: Float = textPaint.measureText(textToShow)
        val textHeight: Float = textPaint.textSize

        val textRect = RectF(scaledRect.left + (lineWidth/2), scaledRect.top + (lineWidth/2),
            scaledRect.left + lineWidth + textWidth, scaledRect.top + lineWidth + textHeight)

        toDrawOnCanvas.drawRoundRect(textRect, textBoxRadius, textBoxRadius, textBackgroundPaint)
        toDrawOnCanvas.drawText(textToShow, scaledRect.left + (textWidth/2), scaledRect.top + textHeight ,textPaint)

        bitmapToDrawOn?.let { Canvas(it) }.apply {
            toDrawOnCanvas
        }

    }

    private fun updateColorsMap(recognitionNames: List<String>){

        val toRemove = mutableListOf<String>()
        for(element in colorsMap){
            if(!recognitionNames.contains(element.key)){
                toRemove.add(element.key)
            }
        }

        for(current in toRemove){
            colorsMap.remove(current)
        }

        for(name in recognitionNames){

            if(!colorsMap.containsKey(name)){
                for(color in colorsList){
                    if(!colorsMap.containsValue(color)){
                        colorsMap[name] = color
                    }
                }
            }

        }

    }

    private fun getScaledRect(rect: RectF, scale: Float, padding: Float, smallerDimension: Int, deviceOrientation: Int): RectF{

        var left = 0f
        var right = 0f
        var top = 0f
        var bottom = 0f

        var currentOrientation = 0

        if(315 < deviceOrientation || deviceOrientation <= 45){
            currentOrientation = 0
        }
        else if(deviceOrientation in 46..135){
            currentOrientation = 90
        }
        else if(deviceOrientation in 136..225){
            currentOrientation = 180
        }
        else if(deviceOrientation in 226..315){
            currentOrientation = 270
        }

        when (currentOrientation) {
            0 -> {
                left = rect.left*scale
                right = rect.right*scale
                top = (rect.top*scale) + padding
                bottom = (rect.bottom*scale) + padding
            }
            90 -> {
                left = rect.left*scale  + padding
                right = rect.right*scale  + padding
                top = (rect.top*scale)
                bottom = (rect.bottom*scale)
            }
            180 -> {
                left = rect.left*scale
                right = rect.right*scale
                top = (rect.top*scale) + padding
                bottom = (rect.bottom*scale) + padding
            }
            270 -> {
                left = rect.left*scale  + padding
                right = rect.right*scale  + padding
                top = (rect.top*scale)
                bottom = (rect.bottom*scale)
            }
        }

        return RectF(left, top, right, bottom)

    }

}