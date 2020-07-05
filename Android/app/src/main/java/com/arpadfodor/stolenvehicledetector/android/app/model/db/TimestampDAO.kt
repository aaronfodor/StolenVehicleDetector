package com.arpadfodor.stolenvehicledetector.android.app.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TimestampDAO {

    @Query("SELECT * FROM ${ApplicationDB.TIMESTAMPS_TABLE_NAME}")
    fun getAll(): List<Timestamp>

    @Query("SELECT * FROM ${ApplicationDB.TIMESTAMPS_TABLE_NAME} WHERE name=:name ")
    fun getByName(name: String): Timestamp

    @Query("DELETE FROM ${ApplicationDB.TIMESTAMPS_TABLE_NAME} WHERE name=:name ")
    fun deleteByName(name: String)

    @Query("DELETE FROM ${ApplicationDB.TIMESTAMPS_TABLE_NAME}")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg timestamp: Timestamp)

}