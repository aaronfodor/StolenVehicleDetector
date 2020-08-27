package com.arpadfodor.stolenvehicledetector.android.app.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.DbReport

@Dao
interface ReportDAO {

    @Query("SELECT * FROM ${ApplicationDB.REPORT_TABLE_NAME}")
    fun getAll(): List<DbReport>?

    @Query("DELETE FROM ${ApplicationDB.REPORT_TABLE_NAME}")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg report: DbReport)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(report_list: List<DbReport>)

}