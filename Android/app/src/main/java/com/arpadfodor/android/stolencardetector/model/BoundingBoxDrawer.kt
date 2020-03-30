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

    fun initialize(_boxRadius: Float, _lineWidth: Float, _bbTextSize: Float, colors: Array<String>){

        boxRadius = _boxRadius
        textBoxRadius = _boxRadius/2
        lineWidth = _lineWidth
        bbTextSize = _bbTextSize

        for(color in colors){
            colorsList.add(Color.parseColor(color))
        }

    }

    /**
     * Returns the resized image
     *
     * @param viewFinderBitmap      Image with required properties
     * @param deviceOrientation     Orientation of the device
     * @param modelInputSize        Size to scale from
     * @param recognitions          List of recognitions to draw
     *
     * @return Bitmap               Bitmap with bounding boxes
     */
    fun drawBoundingBoxes(viewFinderBitmap: Bitmap, deviceOrientation: Int, modelInputSize: Size, recognitions: List<Recognition>): Bitmap{

        val overlayBitmap = Bitmap.createBitmap(viewFinderBitmap.width, viewFinderBitmap.height, Bitmap.Config.ARGB_8888)

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

            val toDraw = Canvas(overlayBitmap)

            val boxColor = colorsMap[recognition.title] ?: fallbackColor
            boxPaint.color = boxColor
            textBackgroundPaint.color = boxColor

            val horizontalScale = overlayBitmap.width.toFloat() / modelInputSize.width.toFloat()
            val verticalScale = overlayBitmap.height.toFloat() / modelInputSize.height.toFloat()
            val scale = min(horizontalScale, verticalScale)
            val padding = (max(overlayBitmap.width, overlayBitmap.height).toFloat() - min(overlayBitmap.width, overlayBitmap.height).toFloat()) / 2f
            val smallerDimension = min(overlayBitmap.width, overlayBitmap.height)

            val rect = recognition.location
            val scaledRect = getScaledRect(rect, scale, padding, smallerDimension, deviceOrientation)

            toDraw.drawRoundRect(scaledRect, boxRadius, boxRadius, boxPaint)

            val textToShow = "${recognition.title} ${"%.2f".format(recognition.confidence*100)}%"
            val textWidth: Float = textPaint.measureText(textToShow)
            val textHeight: Float = textPaint.textSize

            val textRect = RectF(scaledRect.left + (lineWidth/2), scaledRect.top + (lineWidth/2),
                scaledRect.left + lineWidth + textWidth, scaledRect.top + lineWidth + textHeight)

            toDraw.drawRoundRect(textRect, textBoxRadius, textBoxRadius, textBackgroundPaint)
            toDraw.drawText(textToShow, scaledRect.left + (textWidth/2), scaledRect.top + textHeight ,textPaint)

            overlayBitmap?.let { Canvas(it) }?.apply {
                toDraw
            }

        }

        return overlayBitmap

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

        when (deviceOrientation) {
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