package com.arpadfodor.android.stolencardetector.model

import android.content.Context
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
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

        val appContext = appContext
        val mediaDir = appContext.externalMediaDirs?.firstOrNull()?.let {
            File(it, appName).apply {
                mkdirs()
            }
        }

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

}