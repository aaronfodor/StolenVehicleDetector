package com.arpadfodor.stolenvehicledetector.android.app.model.repository

import com.arpadfodor.stolenvehicledetector.android.app.model.db.ApplicationDB
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.UserReport


object UserReportRepository {

    /**
     * User reports
     **/
    fun postUserReport(report: UserReport, callback: (Boolean) -> Unit){

        val database = ApplicationDB.getDatabase(GeneralRepository.context)

        Thread {

            var isDbUpdated = false

            // run delete, insert, etc. in an atomic transaction
            database.runInTransaction{

                try {
                    database.userReportTable().insert(report)
                    //update the local flag
                    isDbUpdated = true
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
                finally {
                    callback(isDbUpdated)
                }

            }

        }.start()

    }

    fun getByUser(userId: String, callback: (List<UserReport>) -> Unit) {

        val reports = mutableListOf<UserReport>()
        val database = ApplicationDB.getDatabase(GeneralRepository.context)

        Thread {

            try {
                val dbContent = database.userReportTable().getByReporter(userId) ?: listOf()
                for(element in dbContent){
                    reports.add(element)
                }
                callback(reports)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }

        }.start()

    }

    fun updateSentFlagByIdAndUser(
        id: Int,
        userId: String,
        sentFlag: Boolean,
        callback: (Boolean) -> Unit
    ) {

        val database = ApplicationDB.getDatabase(GeneralRepository.context)

        Thread {

            try {
                database.userReportTable().updateSentFlagByIdAndReporter(id, userId, sentFlag)
            }
            catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            }

            callback(true)

        }.start()

    }

    fun updateMessageByIdAndUser(
        id: Int,
        userId: String,
        message: String,
        callback: (Boolean) -> Unit
    ) {

        val database = ApplicationDB.getDatabase(GeneralRepository.context)

        Thread {

            try {
                database.userReportTable().updateMessageByIdAndReporter(id, userId, message)
            }
            catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            }

            callback(true)

        }.start()

    }

    fun deleteByIdAndUser(id: Int, userId: String, callback: (Boolean) -> Unit) {

        val database = ApplicationDB.getDatabase(GeneralRepository.context)

        Thread {

            try {
                database.userReportTable().deleteByIdAndReporter(id, userId)
            }
            catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            }

            callback(true)

        }.start()

    }

    fun deleteAllFromUser(userId: String, callback: (Boolean) -> Unit) {

        val database = ApplicationDB.getDatabase(GeneralRepository.context)

        Thread {

            try {
                database.userReportTable().deleteAllByReporter(userId)
            }
            catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            }

            callback(true)

        }.start()

    }

}