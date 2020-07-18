package com.arpadfodor.stolenvehicledetector.android.app.model.ai

import android.graphics.RectF

/**
 * A result returned by an ObjectDetector describing what was recognized.
 */
data class RecognizedObject(
    val id: String,
    val title: String,
    val confidence: Float,
    val location: RectF = RectF(),
    var extra: String = "",
    var alertNeeded: Boolean = false
){

    fun getShortString(): String {
        val confidencePercentage = "${"%.2f".format(confidence*100)}%"
        val resultString = "${title} ${confidencePercentage}"
        return resultString
    }

    fun getNormalString(): String{
        val resultString = "[$id] " + getShortString()
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