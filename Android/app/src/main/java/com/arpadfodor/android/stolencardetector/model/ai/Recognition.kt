package com.arpadfodor.android.stolencardetector.model.ai

import android.graphics.RectF

/**
 * A result returned by an IObjectDetector describing what was recognized.
 */
data class Recognition(
    val id: String,
    val title: String,
    val confidence: Float,
    val location: RectF
){

    fun getStringShortData(): String {
        val confidencePercentage = "${"%.2f".format(confidence*100)}%"
        val resultString = "${title} ${confidencePercentage}"
        return resultString
    }

    fun getStringNormalData(): String{
        val resultString = "[$id] " + getStringShortData()
        return resultString
    }

    fun getStringDetailedData(): String{
        val resultString = getStringNormalData() + " $location"
        return resultString
    }

}