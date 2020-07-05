package com.arpadfodor.stolenvehicledetector.android.app.model

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object MediaHandler {

    private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
    private const val PHOTO_EXTENSION = ".jpg"

    private lateinit var appContext: Context
    private var appName = ""

    fun initialize(appContext_: Context, appName_: String){
        appContext = appContext_
        appName = appName_
    }

    /**
     * Use external media if available, otherwise the app's file directory
     */
    fun getOutputDirectory(): File {

        val mediaDir = appContext.getExternalFilesDirs(DIRECTORY_PICTURES).firstOrNull()?.let {
            File(it, appName).apply {
                mkdirs()
            }
        }

        MediaScannerConnection.scanFile(appContext, arrayOf(mediaDir.toString()), null, null)

        return if(mediaDir != null && mediaDir.exists()){
            mediaDir
        }
        else{
            appContext.filesDir
        }

    }

    /**
     * Creates a timestamped file
     * */
    fun createFile(baseFolder: File) : File{
        return File(baseFolder, SimpleDateFormat(FILENAME, Locale.US).format(System.currentTimeMillis()) + PHOTO_EXTENSION)
    }

    /**
     * Returns the orientation of the inspected image from MediaStore
     *
     * @param photoUri      URI of the image to get the orientation information for
     * @return Int          Orientation of the image
     **/
    fun getPhotoOrientation(photoUri: Uri): Int {

        val cursor = appContext.contentResolver.query(photoUri,
            arrayOf(MediaStore.Images.ImageColumns.ORIENTATION), null, null, null
        )

        cursor?: return 0

        if (cursor.count != 1) {
            cursor.close()
            return 0
        }

        cursor.moveToFirst()
        val orientation = cursor.getInt(0)
        cursor.close()

        return orientation

    }

    fun getImageMetaData(photoUri: Uri): Array<String> {

        val contentResolver = appContext.contentResolver
        val inputStream = contentResolver.openInputStream(photoUri)

        var date = ""
        var latitude = "0.0"
        var longitude = "0.0"

        if (inputStream != null) {

            val exif = ExifInterface(inputStream)
            date = exif.getAttribute(ExifInterface.TAG_DATETIME) ?: ""

            val latLong = exif.latLong

            if (latLong != null) {
                if(latLong.isNotEmpty()){
                    latitude = latLong[0].toString()
                    longitude = latLong[1].toString()
                }
            }

        }

        return arrayOf(date, latitude, longitude)

    }

}