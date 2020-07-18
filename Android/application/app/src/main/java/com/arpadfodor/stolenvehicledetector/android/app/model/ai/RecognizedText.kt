package com.arpadfodor.stolenvehicledetector.android.app.model.ai

import android.graphics.RectF

/**
 * A result returned by the TextRecognitionService containing text and describing what was recognized.
 */
data class RecognizedText(
    var text: String,
    val language: String,
    val location: RectF = RectF(),
    var extra: String = ""
){

    fun getShortString(): String {
        return text
    }

    fun getNormalString(): String{
        val resultString = getShortString() + " [$language]"
        return resultString
    }

    fun getDetailedString(): String{
        val resultString = getNormalString() + " $location"
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