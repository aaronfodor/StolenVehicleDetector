package com.arpadfodor.android.stolencardetector.model

import android.graphics.*
import android.util.Size
import androidx.core.graphics.ColorUtils
import com.arpadfodor.android.stolencardetector.model.ai.RecognizedObject
import kotlin.math.max
import kotlin.math.min

object BoundingBoxDrawer {

    private var boxRadius = 10f
    private var textBoxRadius = 5f
    private var lineWidth = 5f
    private var bbTextSize = 40f

    private val fallbackColor = Color.parseColor("#FF0000")
    private val colorsList = mutableListOf<Int>()
    private var colorsMap: MutableMap<String, Int> = HashMap()
    val lowTransparency = 179       //hex: B3
    val highTransparency = 77       //hex: 4D

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
     * @param inputBitmap           Input bitmap which dimensions are used to create the output bitmap
     * @param deviceOrientation     Used to scale bounding boxes on the image correctly
     * @param modelInputSize        Base resolution of the RectF relevant image to scale from
     * @param recognitions          List of RecognizedObjects to draw
     *
     * @return Bitmap               Bitmap with bounding boxes
     */
    fun drawBoundingBoxes(inputBitmap: Bitmap, deviceOrientation: Int, modelInputSize: Size, recognitions: List<RecognizedObject>): Bitmap{

        var bitmapToDrawOn = Bitmap.createBitmap(inputBitmap.width, inputBitmap.height, Bitmap.Config.ARGB_8888)

        val boxPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = fallbackColor
            strokeWidth = lineWidth
        }

        val backgroundPaint = Paint().apply {
            style = Paint.Style.FILL
            color = fallbackColor
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

        // update the color according to the recognition id
        updateColorsMap(names)

        for (recognition in recognitions) {
            bitmapToDrawOn = drawBoundingBox(bitmapToDrawOn, deviceOrientation, modelInputSize,
                recognition, boxPaint, backgroundPaint, textBackgroundPaint, textPaint)
        }

        return bitmapToDrawOn

    }

    /**
     * Does not return a value, draws on the input Bitmap
     *
     * @param image           Input bitmap on which the function draws
     */
    private fun drawBoundingBox(image: Bitmap, deviceOrientation: Int, modelInputSize: Size, recognition: RecognizedObject,
                                boxPaint: Paint, backgroundPaint: Paint, textBackgroundPaint: Paint, textPaint: Paint) : Bitmap{

        val toDrawOnCanvas = Canvas(image)

        // setting of color and transparency
        val boxColor = colorsMap[recognition.title] ?: fallbackColor

        boxPaint.color = ColorUtils.setAlphaComponent(boxColor,  lowTransparency)
        backgroundPaint.color = ColorUtils.setAlphaComponent(boxColor,  highTransparency)
        textBackgroundPaint.color = ColorUtils.setAlphaComponent(boxColor,  lowTransparency)

        val horizontalScale = image.width.toFloat() / modelInputSize.width.toFloat()
        val verticalScale = image.height.toFloat() / modelInputSize.height.toFloat()
        val scale = min(horizontalScale, verticalScale)
        val padding = (max(image.width, image.height).toFloat() - min(image.width, image.height).toFloat()) / 2f

        val rect = recognition.location
        val scaledRect = getScaledRect(rect, scale, padding, deviceOrientation)

        if(recognition.extra.isNotBlank()){
            toDrawOnCanvas.drawRoundRect(scaledRect, boxRadius, boxRadius, backgroundPaint)
        }

        toDrawOnCanvas.drawRoundRect(scaledRect, boxRadius, boxRadius, boxPaint)

        val textToShow = recognition.getShortStringWithExtra()
        val textWidth: Float = textPaint.measureText(textToShow)
        val textHeight: Float = textPaint.textSize

        val textRect = RectF(scaledRect.left + (lineWidth/2), scaledRect.top + (lineWidth/2),
            scaledRect.left + lineWidth + textWidth, scaledRect.top + lineWidth + textHeight)

        toDrawOnCanvas.drawRoundRect(textRect, textBoxRadius, textBoxRadius, textBackgroundPaint)
        toDrawOnCanvas.drawText(textToShow, scaledRect.left + (textWidth/2), scaledRect.top + textHeight ,textPaint)

        return image

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

    private fun getScaledRect(rect: RectF, scale: Float, padding: Float, deviceOrientation: Int): RectF{

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