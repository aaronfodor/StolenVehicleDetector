package com.arpadfodor.stolenvehicledetector.android.app.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.DbUser

@Dao
interface UserDAO {

    @Query("SELECT * FROM ${ApplicationDB.USER_TABLE_NAME}")
    fun getAll(): List<DbUser>?

    @Query("DELETE FROM ${ApplicationDB.USER_TABLE_NAME}")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg user: DbUser)

}