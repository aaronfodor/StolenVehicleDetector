package com.arpadfodor.stolenvehicledetector.android.app.model.repository

import android.graphics.Bitmap
import com.arpadfodor.stolenvehicledetector.android.app.model.MediaHandler
import com.arpadfodor.stolenvehicledetector.android.app.model.db.ApplicationDB
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.UserReport


object UserReportRepository {

    /**
     * User reports
     **/
    fun postUserReport(report: UserReport, reportImage: Bitmap?, callback: (Boolean) -> Unit){

        Thread {

            val database = ApplicationDB.getDatabase(GeneralRepository.context)
            var isSuccess = false

            try {

                reportImage?.let {image ->
                    MediaHandler.getExternalDbImagesDirOfUser(report.Reporter)?.let { dir ->
                        report.imagePath = MediaHandler.saveImage(dir, image)
                    }
                }

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    database.userReportTable().insert(report)
                }

                //update the local flag
                isSuccess = true

            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(isSuccess)
            }

        }.start()

    }

    fun getByUser(userId: String, callback: (List<UserReport>, List<Bitmap?>) -> Unit) {

        Thread {

            val reports = mutableListOf<UserReport>()
            val reportImages = mutableListOf<Bitmap?>()

            val database = ApplicationDB.getDatabase(GeneralRepository.context)

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {

                    val dbContent = database.userReportTable().getByReporter(userId) ?: listOf()

                    for (element in dbContent) {
                        reports.add(element)
                        reportImages.add(MediaHandler.getImageByPath(element.imagePath))
                    }

                }

            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(reports, reportImages)
            }

        }.start()

    }

    fun updateSentFlagByIdAndUser(id: Int, userId: String, sentFlag: Boolean,
                                  callback: (Boolean) -> Unit) {

        Thread {

            val database = ApplicationDB.getDatabase(GeneralRepository.context)
            var isSuccess = false

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    database.userReportTable()
                        .updateSentFlagByIdAndReporter(id, userId, sentFlag)
                }
                isSuccess = true

            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(isSuccess)
            }

        }.start()

    }

    fun updateMessageByIdAndUser(id: Int, userId: String, message: String,
        callback: (Boolean) -> Unit) {

        Thread {

            val database = ApplicationDB.getDatabase(GeneralRepository.context)
            var isSuccess = false

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    database.userReportTable().updateMessageByIdAndReporter(id, userId, message)
                }
                isSuccess = true

            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(isSuccess)
            }


        }.start()

    }

    fun deleteByIdAndUser(id: Int, userId: String, callback: (Boolean) -> Unit) {

        Thread {

            val database = ApplicationDB.getDatabase(GeneralRepository.context)
            var isSuccess = false

            try {

                var userReportToDelete: UserReport? = null

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    userReportToDelete = database.userReportTable().getByIdAndReporter(id, userId)
                }

                userReportToDelete?.imagePath?.let {
                    MediaHandler.deleteImage(it)
                }

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    database.userReportTable().deleteByIdAndReporter(id, userId)
                }

                isSuccess = true

            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(isSuccess)
            }

        }.start()

    }

    fun deleteAllFromUser(userId: String, callback: (Boolean) -> Unit) {

        Thread {

            val database = ApplicationDB.getDatabase(GeneralRepository.context)
            var isSuccess = false

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    database.userReportTable().deleteAllByReporter(userId)
                }

                MediaHandler.deleteExternalDbImagesOfUser(userId)

                isSuccess = true

            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(isSuccess)
            }

        }.start()

    }

}