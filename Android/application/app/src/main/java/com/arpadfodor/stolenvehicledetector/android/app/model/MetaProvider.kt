package com.arpadfodor.stolenvehicledetector.android.app.model

import android.net.Uri

object MetaProvider {

    /**
     * Metadata
     * [0]: date of image taking / device date
     * [1]: latitude of the photo origin / device latitude
     * [2]: longitude of the photo origin / device longitude
     **/

    fun getImageMetaData(photoUri: Uri): Array<String> {
        return MediaHandler.getImageMeta(photoUri)
    }

    fun getDeviceMetaData(): Array<String> {
        val date = DateHandler.currentDateString()
        val location = LocationService.getLocation()
        return arrayOf(date, location[0].toString(), location[1].toString())
    }

}