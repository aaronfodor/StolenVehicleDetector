package com.arpadfodor.stolenvehicledetector.android.app.model.db

import androidx.room.*
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.DbUserRecognition

@Dao
interface UserRecognitionDAO {

    @Query("SELECT * FROM ${ApplicationDB.USER_RECOGNITION_TABLE_NAME} WHERE id=:id AND reporter=:reporter ")
    fun getByIdAndReporter(id: Int, reporter: String): DbUserRecognition?

    @Query("SELECT * FROM ${ApplicationDB.USER_RECOGNITION_TABLE_NAME} WHERE reporter=:reporter ")
    fun getByReporter(reporter: String): List<DbUserRecognition>?

    @Query("UPDATE ${ApplicationDB.USER_RECOGNITION_TABLE_NAME} SET  is_sent=:sentFlag WHERE id=:id AND reporter=:reporter ")
    fun updateSentFlagByIdAndReporter(id: Int, reporter: String, sentFlag: Boolean)

    @Query("UPDATE ${ApplicationDB.USER_RECOGNITION_TABLE_NAME} SET  message=:message WHERE id=:id AND reporter=:reporter ")
    fun updateMessageByIdAndReporter(id: Int, reporter: String, message: String)

    @Query("DELETE FROM ${ApplicationDB.USER_RECOGNITION_TABLE_NAME} WHERE id=:id AND reporter=:reporter ")
    fun deleteByIdAndReporter(id: Int, reporter: String)

    @Query("DELETE FROM ${ApplicationDB.USER_RECOGNITION_TABLE_NAME} WHERE reporter=:reporter ")
    fun deleteAllByReporter(reporter: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg report: DbUserRecognition)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(report_list: List<DbUserRecognition>)

}