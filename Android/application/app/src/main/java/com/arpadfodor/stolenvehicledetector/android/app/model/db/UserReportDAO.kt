package com.arpadfodor.stolenvehicledetector.android.app.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.UserReport

@Dao
interface UserReportDAO {

    @Query("SELECT * FROM ${ApplicationDB.USER_REPORT_TABLE_NAME} WHERE reporter=:reporter ")
    fun getByReporter(reporter: String): List<UserReport>?

    @Query("DELETE FROM ${ApplicationDB.USER_REPORT_TABLE_NAME} WHERE id=:id AND reporter=:reporter ")
    fun deleteByIdAndReporter(id: Int, reporter: String)

    @Query("DELETE FROM ${ApplicationDB.USER_REPORT_TABLE_NAME} WHERE reporter=:reporter ")
    fun deleteAllByReporter(reporter: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg report: UserReport)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(report_list: List<UserReport>)

}