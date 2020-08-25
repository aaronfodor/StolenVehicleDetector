package com.arpadfodor.stolenvehicledetector.android.app.model

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object MediaHandler {

    private const val TAG = "Media handler"

    private const val TIMESTAMP_FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private const val PHOTO_EXTENSION = ".jpg"
    private const val DB_IMAGES_ALBUM = "dbImages"

    private lateinit var appContext: Context
    private lateinit var appName: String

    private val exifFormatter = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.ENGLISH)

    fun initialize(appContext_: Context, appName_: String){
        appContext = appContext_
        appName = appName_
    }

    /**
     * Returns the image output stream to the shared storage.
     * An image placed there will be available even after app deletion.
     */
    fun getImagePublicDirOutputStream() : OutputStream? {

        val relativeImagePath = File.separator + appName

        var fos: OutputStream? = null

        try{

            val fileName = SimpleDateFormat(TIMESTAMP_FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val resolver = appContext.contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/$PHOTO_EXTENSION")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/$appName")
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                imageUri?.let{
                    fos = resolver.openOutputStream(it)
                }

            }
            else {

                val imageDirPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + relativeImagePath

                val imageDir = File(imageDirPath).apply {
                    mkdirs()
                }

                imageDirPath.let {
                    File(it, fileName + PHOTO_EXTENSION).apply {
                        mkdirs()
                        fos = FileOutputStream(it)
                    }
                }

            }

        }
        catch (e: Exception){
            e.printStackTrace()
        }
        finally {
            return fos
        }

    }

    /**
     * App's private external directory (used for Db images).
     * No need to request any storage-related permissions to access.
     * Verification is needed to decide whether the storage is available.
     * Tends to be small- before writing app-specific files, query the free space on the device.
     */
    fun getExternalDbImagesDirOfUser(userName: String): File? {

        val userAlbumPath = "${DB_IMAGES_ALBUM}${File.separator}${userName}"

        val externalPictureDirs = appContext.getExternalFilesDirs(Environment.DIRECTORY_PICTURES)

        val dbPicturesDir = externalPictureDirs.firstOrNull()?.let {
            File(it, userAlbumPath).apply {
                mkdirs()
            }
        }

        return if(dbPicturesDir != null && dbPicturesDir.exists()){
            dbPicturesDir
        }
        else{
            Log.e(TAG, "Directory not available/not created")
            null
        }

    }

    fun deleteExternalDbImagesOfUser(userName: String) : Boolean{
        return getExternalDbImagesDirOfUser(userName)?.deleteRecursively() ?: true
    }

    /**
     * App's encrypted, private internal directory.
     * No need to request any storage-related permissions to access.
     * Always available.
     * Tends to be small - before writing app-specific files, query the free space on the device.
     */
    fun getInternalFileStorage(): File {
        return appContext.filesDir
    }

    /**
     * Checks if a volume containing external storage is available for read and write.
     **/
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * Checks if a volume containing external storage is available to at least read.
     **/
    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
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

        val newFile = createImageFile(
            targetDir,
            "img_" + SimpleDateFormat(
                TIMESTAMP_FILENAME_FORMAT,
                Locale.US
            ).format(System.currentTimeMillis())
        )

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

            return newFile.absolutePath

        }

    }

    /**
     * Deletes the image file
     **/
    fun deleteImage(imagePath: String) : Boolean{
        val fileToDelete = File(imagePath)
        return fileToDelete.delete()
    }

    /**
     * Returns the loaded image from path
     *
     * @param imagePath     Path of the image
     * @return Bitmap       The loaded image
     **/
    fun getImageByPath(imagePath: String?): Bitmap? {

        var image: Bitmap? = null
        imagePath ?: return image

        try {
            image = BitmapFactory.decodeFile(imagePath)
        }
        catch (e: Exception){
            e.printStackTrace()
        }
        finally {
            return image
        }

    }

    /**
     * Returns the loaded image
     *
     * @param imageUri      URI of the image
     * @return Bitmap?      The loaded image
     **/
    fun getImageByUri(imageUri: Uri): Bitmap? {

        var image: Bitmap? = null

        try{

            // Open a specific media item using ParcelFileDescriptor.
            val resolver = appContext.contentResolver

            // "r" for read
            // "rw" for read-and-write
            // "rwt" for truncating or overwriting existing file contents
            val readOnlyMode = "r"

            resolver.openFileDescriptor(imageUri, readOnlyMode).use { pfd ->

                pfd?.let {
                    val fileDescriptor = it.fileDescriptor
                    image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                }

            }

        }
        catch (e: Exception){
            e.printStackTrace()
        }
        finally {
            return image
        }

    }

    /**
     * Returns the orientation of the inspected image from MediaStore
     *
     * @param photoUri      URI of the image to get the orientation information for
     * @return Int          Orientation of the image
     **/
    fun getPhotoOrientation(photoUri: Uri): Int {

        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appContext.contentResolver.query(
                photoUri,
                arrayOf(MediaStore.Images.ImageColumns.ORIENTATION), null, null, null
            )
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

        try {

            contentResolver.openInputStream(photoUri)?.use { stream ->

                ExifInterface(stream).run {

                    // coordinates from Exif - ACCESS_MEDIA_LOCATION permission needed
                    // If lat/long is null, fall back to the coordinates (0, 0).
                    val latLong = latLong ?: doubleArrayOf(0.0, 0.0)
                    if(latLong.isNotEmpty()){
                        latitudeString = latLong[0].toString()
                        longitudeString = latLong[1].toString()
                    }

                    val dateStringRaw = getAttribute(ExifInterface.TAG_DATETIME) ?: ""
                    val date = exifFormatter.parse(dateStringRaw) ?: Date(0)

                    dateString = DateHandler.dateToString(date)


                }

            }

        }
        catch (e: Exception){
            e.printStackTrace()
        }

        return arrayOf(dateString, latitudeString, longitudeString)

    }

}