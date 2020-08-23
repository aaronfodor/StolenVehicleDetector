package com.arpadfodor.stolenvehicledetector.android.app.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object MediaHandler {

    private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
    private const val PHOTO_EXTENSION = ".jpg"

    private lateinit var appContext: Context
    private var appName = ""

    private val exifFormatter = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.ENGLISH)

    fun initialize(appContext_: Context, appName_: String){
        appContext = appContext_
        appName = appName_
    }

    /**
     * Use external media if available, otherwise the app's file directory
     */
    fun getPublicDirectory(): File {

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
     * App's private file directory
     */
    fun getPrivateDirectory(): File {
        return appContext.filesDir
    }

    /**
     * Creates a timestamped image file
     **/
    fun createTimestampedImageFile(targetDir: File) : File{
        return createImageFile(targetDir, SimpleDateFormat(FILENAME, Locale.US).format(System.currentTimeMillis()))
    }

    /**
     * Creates an image file
     **/
    private fun createImageFile(targetDir: File, fileName: String) : File{
        return File(targetDir, fileName + PHOTO_EXTENSION)
    }

    /**
     * Saves the image to the target directory
     *
     * @param targetDir     Where to save the image
     * @param image         Image content
     * @return String       Absolute path of the saved image
     **/
    fun saveImage(targetDir: File, image: Bitmap) : String{

        val newFile = createImageFile(targetDir,
            "img_" + SimpleDateFormat(FILENAME, Locale.US).format(System.currentTimeMillis()))

        var fileOutputStream: FileOutputStream? = null

        try{
            fileOutputStream = FileOutputStream(newFile)
            fileOutputStream.let {
                image.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            }
        }
        catch (e: Exception){
            e.printStackTrace()
        }
        finally {
            try {
                fileOutputStream?.flush()
                fileOutputStream?.close()
            }
            catch (e: Exception){
                e.printStackTrace()
            }
        }

        return newFile.absolutePath

    }

    /**
     * Deletes the image file
     **/
    fun deleteImage(imagePath: String) : Boolean{
        val fileToDelete = File(imagePath)
        return fileToDelete.delete()
    }

    /**
     * Returns the loaded image from MediaStore
     *
     * @param imagePath     Path of the image
     * @return Bitmap       The loaded image
     **/
    fun getImageByPath(imagePath: String): Bitmap? {

        var image: Bitmap? = null

        try {
            image = BitmapFactory.decodeFile(imagePath)
        }
        catch (e: Exception){
            e.printStackTrace()
        }

        return image
    }

    /**
     * Returns the loaded image from MediaStore
     *
     * @param imageUri      URI of the image
     * @return Bitmap       The loaded image
     **/
    fun getImageByUri(imageUri: Uri): Bitmap {

        /*val image = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            val source = ImageDecoder.createSource(appContext.contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        }
        else{
            MediaStore.Images.Media.getBitmap(appContext.contentResolver, imageUri)
        }*/

        val image = MediaStore.Images.Media.getBitmap(appContext.contentResolver, imageUri)

        return image

    }

    /**
     * Returns the orientation of the inspected image from MediaStore
     *
     * @param photoUri      URI of the image to get the orientation information for
     * @return Int          Orientation of the image
     **/
    fun getPhotoOrientation(photoUri: Uri): Int {

        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appContext.contentResolver.query(photoUri,
                arrayOf(MediaStore.Images.ImageColumns.ORIENTATION), null, null, null)
        }
        else {
            appContext.contentResolver.query(photoUri, null, null, null, null)
        }

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

    fun getImageMeta(rawPhotoUri: Uri): Array<String> {

        val contentResolver = appContext.contentResolver

        // Location from Exif works this way above Android Q
        val photoUri = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            MediaStore.setRequireOriginal(rawPhotoUri)
        }
        else{
            rawPhotoUri
        }

        var dateString = ""
        var latitudeString = "0.0"
        var longitudeString = "0.0"

        contentResolver.openInputStream(photoUri)?.use { stream ->

            ExifInterface(stream).run {

                // coordinates from Exif - ACCESS_MEDIA_LOCATION permission needed
                try {
                    // If lat/long is null, fall back to the coordinates (0, 0).
                    val latLong = latLong ?: doubleArrayOf(0.0, 0.0)
                    if(latLong.isNotEmpty()){
                        latitudeString = latLong[0].toString()
                        longitudeString = latLong[1].toString()
                    }
                }
                catch (e: Exception){
                    e.printStackTrace()
                }

                val dateStringRaw = getAttribute(ExifInterface.TAG_DATETIME) ?: ""
                var date = Date(0)
                try{
                    date = exifFormatter.parse(dateStringRaw) ?: Date(0)
                }
                catch (e: Exception){
                    e.printStackTrace()
                }
                dateString = DateHandler.dateToString(date)


            }

        }

        return arrayOf(dateString, latitudeString, longitudeString)

    }

}