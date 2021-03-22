package com.arpadfodor.stolenvehicledetector.android.app.model.ml.ocr

import android.graphics.RectF

/**
 * A result returned by the TextRecognitionService containing text and describing what was recognized.
 */
data class RecognizedText(
    var text: String,
    val confidence: Float,
    val language: String,
    val location: RectF = RectF(),
    var extra: String = ""
){

    fun getShortString(): String {
        return text
    }

    fun getNormalString(): String{
        val resultString = getShortString() + " [$confidence]"
        return resultString
    }

    fun getDetailedString(): String{
        val resultString = getNormalString() + " $language"  + " $location"
        return resultString
    }

    fun getShortStringWithExtra(): String{
        var resultString = getShortString()
        if(extra.isNotEmpty()){
            resultString += " | $extra"
        }
        return resultString
    }

}